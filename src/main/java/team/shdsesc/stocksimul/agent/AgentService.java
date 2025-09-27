package team.shdsesc.stocksimul.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import team.shdsesc.stocksimul.market.service.DbMarketService;
import team.shdsesc.stocksimul.market.dto.CandleResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentService {

    @Value("${fastApi.url}")
    private String fastApiUrl;

    private final DbMarketService dbMarketService;

    public Mono<PredictResponseDTO> predict(PredictRequestDTO requestDTO) {
        WebClient webClient = createWebClient();

        return webClient.post()
                .uri("/predict")
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(PredictResponseDTO.class)
                // FastAPI가 return_predictions를 비워 보낸 경우 보강
                .map(this::ensureReturnPredictions)
                // 프론트가 바로 사용할 수 있도록 시계열 정규화
                .map(this::enrichChartSeries)
                .timeout(Duration.ofMinutes(5))  // Mono 레벨 타임아웃도 5분으로 설정
                .onErrorResume(throwable -> {
//                    return Mono.just(new PredictResponseDTO());
                    return Mono.error(throwable);
                });
    }
    public Mono<PortfolioResponseDTO> getPortfolioCumulativeReturns(PortfolioRequestDTO requestDTO) {
        WebClient webClient = createWebClient();

        return webClient.post()
                .uri("/portfolio/cumulative-returns")
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(PortfolioResponseDTO.class)
                .timeout(Duration.ofMinutes(5))  // Mono 레벨 타임아웃도 5분으로 설정
                .onErrorResume(throwable -> {
                    return Mono.error(throwable);
                });
    }

    private WebClient createWebClient() {
        // HTTP 클라이언트 타임아웃 설정 (5분)
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMinutes(5))  // 응답 타임아웃
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000);  // 연결 타임아웃 30초

        return WebClient.builder()
                .baseUrl(fastApiUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    /**
     * FastAPI 응답에 return_predictions가 없거나 비어 있을 때,
     * last_price와 price_predictions로 일별 수익률을 계산해 채웁니다.
     * r_i = price[i] / prev - 1  (i=0에서 prev = last_price)
     */
    private PredictResponseDTO ensureReturnPredictions(PredictResponseDTO resp) {
        if (resp == null) return null;
        List<Double> returns = resp.getReturnPredictions();
        if (returns != null && !returns.isEmpty()) return resp;

        List<Double> prices = resp.getPricePredictions();
        if (prices == null || prices.isEmpty()) return resp;

        Double lastPrice = resp.getLastPrice();
        if (lastPrice == null || lastPrice == 0.0) return resp;

        List<Double> computed = new ArrayList<>(prices.size());
        double prev = lastPrice;
        for (Double p : prices) {
            if (p == null || prev == 0.0) break;
            computed.add(p / prev - 1.0); // 비율(예: 0.0123)
            prev = p; // 다음 루프에서 기준가로 사용
        }

        resp.setReturnPredictions(computed);
        return resp;
    }

    /**
     * historical/predictions 시계열을 표준화하여 DTO에 채워 넣습니다.
     * - historical: base_date - 50일 ~ pred_end_date까지의 종가를 UTC epoch 초로 정렬
     * - predictions: base_date 다음날부터 pred_end_date까지, base_date 직전 실제값 기준으로 리베이스
     */
    private PredictResponseDTO enrichChartSeries(PredictResponseDTO resp) {
        if (resp == null) return null;

        // 1) pred_end_date 계산
        String predEnd = null;
        List<String> predDates = resp.getPredictionDates();
        if (predDates != null && !predDates.isEmpty()) {
            predEnd = predDates.stream().filter(s -> s != null && !s.isBlank())
                    .sorted()
                    .reduce((a, b) -> b)
                    .orElse(null);
        }
        resp.setPredEndDate(predEnd);

        // 2) 과거 구간 조회: base_date - 50d ~ pred_end_date(or base_date)
        String baseDate = resp.getBaseDate();
        if (baseDate == null || baseDate.isBlank()) return resp;
        try {
            java.time.LocalDate base = java.time.LocalDate.parse(baseDate);
            java.time.LocalDate end = predEnd != null ? java.time.LocalDate.parse(predEnd) : base;
            long fromEpoch = base.minusDays(50).atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC);
            long toEpoch = end.atTime(23, 59, 59).toEpochSecond(java.time.ZoneOffset.UTC);
            long baseTs = base.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC);
            long baseEndTs = base.atTime(23, 59, 59).toEpochSecond(java.time.ZoneOffset.UTC);

            CandleResponse candles = dbMarketService.getCandles(resp.getTicker(), null, fromEpoch, toEpoch, null);
            List<ChartSeriesPoint> historical = new java.util.ArrayList<>();
            if (candles != null && "ok".equalsIgnoreCase(candles.getStatus())) {
                List<Long> ts = candles.getTimestamps();
                List<Double> closes = candles.getCloses();
                if (ts != null && closes != null) {
                    int n = Math.min(ts.size(), closes.size());
                    for (int i = 0; i < n; i++) {
                        Long t = ts.get(i);
                        Double v = closes.get(i);
                        if (t == null || v == null || !Double.isFinite(v)) continue;
                        historical.add(new ChartSeriesPoint(t, v));
                    }
                }
            }
            historical.sort(java.util.Comparator.comparingLong(ChartSeriesPoint::getTime));
            resp.setHistorical(historical);

            // 3) predictions 시리즈 생성 (리베이스)
            List<Double> predPrices = resp.getPricePredictions();
            double anchor = Double.NaN;
            if (!historical.isEmpty()) {
                // base_date 직전 마지막 값으로 앵커 설정
                for (int i = historical.size() - 1; i >= 0; i--) {
                    if (historical.get(i).getTime() < baseTs) { anchor = historical.get(i).getValue(); break; }
                }
            }
            if (Double.isNaN(anchor)) {
                Double last = resp.getLastPrice();
                if (last != null && Double.isFinite(last)) anchor = last;
            }

            List<ChartSeriesPoint> preds = new java.util.ArrayList<>();
            // base_date 시점의 가격(가능하면 그 날 실제 종가)을 구해 예측 시리즈의 시작점으로 추가하기 위해 보관
            double baseDayValue = Double.NaN;
            if (!historical.isEmpty()) {
                for (ChartSeriesPoint p : historical) {
                    long t = p.getTime();
                    if (t >= baseTs && t <= baseEndTs) { baseDayValue = p.getValue(); break; }
                }
            }
            if (Double.isNaN(baseDayValue)) {
                baseDayValue = !Double.isNaN(anchor) ? anchor : (resp.getLastPrice() != null ? resp.getLastPrice() : Double.NaN);
            }
            if (predDates != null && predPrices != null) {
                int usable = Math.min(predDates.size(), predPrices.size());
                if (usable > 0) {
                    double firstPred = predPrices.get(0) != null ? predPrices.get(0) : Double.NaN;
                    double k = (!Double.isNaN(anchor) && Double.isFinite(firstPred) && firstPred > 0.0)
                            ? (anchor / firstPred) : 1.0;
                    for (int i = 0; i < usable; i++) {
                        String ds = predDates.get(i);
                        Double pv = predPrices.get(i);
                        if (ds == null || pv == null || !Double.isFinite(pv)) continue;
                        long ts = java.time.LocalDate.parse(ds).atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC);
                        double val = pv * k;
                        preds.add(new ChartSeriesPoint(ts, val));
                    }
                }
            }
            // 예측 점선이 basedate에서 시작하고, 시작 가격이 실제와 동일하도록 base_date 포인트를 선행 추가
            boolean hasBasePoint = false;
            for (ChartSeriesPoint p : preds) { if (p.getTime() == baseTs) { hasBasePoint = true; break; } }
            if (!hasBasePoint && Double.isFinite(baseDayValue)) {
                preds.add(new ChartSeriesPoint(baseTs, baseDayValue));
            }
            preds.sort(java.util.Comparator.comparingLong(ChartSeriesPoint::getTime));
            resp.setPredictions(preds);

            return resp;
        } catch (Exception e) {
            return resp; // 파싱 실패 시 원본 유지
        }
    }

}
