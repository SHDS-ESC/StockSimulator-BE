package team.shdsesc.stocksimul.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
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
@Log4j2
public class AgentService {

    @Value("${fastApi.url}")
    private String fastApiUrl;

    private final DbMarketService dbMarketService;
    private final ChatModel chatModel;

    public Mono<PredictResponseDTO> predict(PredictRequestDTO requestDTO) {
        WebClient webClient = createWebClient();

        return webClient.post()
                .uri("/predict")
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(PredictResponseDTO.class)
                // FastAPI가 return_predictions를 비워 보낸 경우 보강
                .map(this::ensureReturnPredictions)
                // 프론트가 바로 사용할 수 있도록 시계열 정규화 (process_date 전달)
                .map(response -> enrichChartSeries(response, requestDTO.getProcessDate()))
                .timeout(Duration.ofMinutes(5))  // Mono 레벨 타임아웃도 5분으로 설정
                .onErrorResume(throwable -> {
//                    return Mono.just(new PredictResponseDTO());
                    return Mono.error(throwable);
                });
    }
    public Mono<PortfolioResponseDTO> getPortfolioAnalysis(PortfolioRequestDTO requestDTO) {
        WebClient webClient = createWebClient();

        return webClient.post()
                .uri("/portfolio/analysis")
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
     * - process_date가 있으면 해당 날짜 이후의 historical 데이터는 필터링하여 스포일러 방지
     */
    private PredictResponseDTO enrichChartSeries(PredictResponseDTO resp, java.time.LocalDate processDate) {
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
            
            // process_date가 있으면 해당 날짜 이후의 historical 데이터 필터링
            if (processDate != null) {
                long processTimestamp = processDate.atTime(23, 59, 59).toEpochSecond(java.time.ZoneOffset.UTC);
                historical = historical.stream()
                        .filter(point -> point.getTime() <= processTimestamp)
                        .collect(java.util.stream.Collectors.toList());
            }
            
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

    /**
     * 기존 호환성을 위한 오버로드 메서드 (process_date 없이)
     */
    private PredictResponseDTO enrichChartSeries(PredictResponseDTO resp) {
        return enrichChartSeries(resp, null);
    }

    /**
     * 주식 가격 예측 및 포트폴리오 분석 결과를 LLM에 전달하여 자연어 투자 점검 결과를 생성합니다.
     * 
     * @param requestDTO 예측 요청과 포트폴리오 분석 요청을 포함하는 DTO
     * @return LLM이 생성한 자연어 투자 점검 결과와 원본 분석 데이터를 포함하는 응답
     */
    public Mono<InvestmentReviewResponseDTO> getInvestmentReview(InvestmentReviewRequestDTO requestDTO) {
        log.info("Investment review request: predict={}, portfolio={}", 
                requestDTO.getPredictRequest(), requestDTO.getPortfolioRequest());

        // 1. 예측 API와 포트폴리오 분석 API를 병렬로 호출
        Mono<PredictResponseDTO> predictMono = requestDTO.getPredictRequest() != null
                ? predict(requestDTO.getPredictRequest())
                : Mono.just(new PredictResponseDTO());

        Mono<PortfolioResponseDTO> portfolioMono = requestDTO.getPortfolioRequest() != null
                ? getPortfolioAnalysis(requestDTO.getPortfolioRequest())
                : Mono.just(new PortfolioResponseDTO());

        // 2. 두 결과를 조합하여 LLM에 전달
        return Mono.zip(predictMono, portfolioMono)
                .flatMap(tuple -> {
                    PredictResponseDTO predictResponse = tuple.getT1();
                    PortfolioResponseDTO portfolioResponse = tuple.getT2();

                    // 3. LLM 프롬프트 생성
                    String prompt = buildInvestmentReviewPrompt(predictResponse, portfolioResponse);
                    log.info("Sending prompt to LLM: {}", prompt.substring(0, Math.min(200, prompt.length())));

                    // 4. LLM 호출 (동기적으로 처리)
                    try {
                        Prompt promptObj = new Prompt(new UserMessage(prompt));
                        ChatResponse chatResponse = chatModel.call(promptObj);
                        String review = chatResponse.getResult().getOutput().getText();

                        log.info("LLM review generated successfully");

                        // 5. 응답 생성
                        return Mono.just(InvestmentReviewResponseDTO.builder()
                                .review(review)
                                .predictResponse(predictResponse)
                                .portfolioResponse(portfolioResponse)
                                .build());
                    } catch (Exception e) {
                        log.error("LLM 호출 중 오류 발생: {}", e.getMessage(), e);
                        return Mono.error(new RuntimeException("LLM 호출 실패: " + e.getMessage(), e));
                    }
                })
                .timeout(Duration.ofMinutes(5))
                .onErrorResume(throwable -> {
                    log.error("Investment review API 오류: {}", throwable.getMessage(), throwable);
                    return Mono.error(throwable);
                });
    }

    /**
     * 예측 결과와 포트폴리오 분석 결과를 바탕으로 LLM 프롬프트를 생성합니다.
     */
    private String buildInvestmentReviewPrompt(PredictResponseDTO predictResponse, PortfolioResponseDTO portfolioResponse) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 전문 투자 분석가입니다. 주어진 주식 예측 데이터와 포트폴리오 분석 데이터를 바탕으로 투자 점검 리포트를 작성해주세요.\n\n");

        // 예측 데이터 요약
        if (predictResponse != null && predictResponse.getTicker() != null) {
            prompt.append("=== 주식 예측 분석 ===\n");
            prompt.append("종목 코드: ").append(predictResponse.getTicker()).append("\n");
            prompt.append("기준일: ").append(predictResponse.getBaseDate()).append("\n");
            prompt.append("현재 가격: ").append(predictResponse.getLastPrice()).append("\n");

            InvestmentAnalysisDTO analysis = predictResponse.getInvestmentAnalysis();
            if (analysis != null) {
                prompt.append("투자 추천: ").append(analysis.getRecommendation()).append("\n");
                prompt.append("액션: ").append(analysis.getAction()).append("\n");
                prompt.append("신뢰도: ").append(analysis.getConfidence()).append("\n");
                prompt.append("점수: ").append(analysis.getScore()).append("/").append(analysis.getMaxScore()).append("\n");

                if (analysis.getSignals() != null && !analysis.getSignals().isEmpty()) {
                    prompt.append("주요 신호:\n");
                    for (String signal : analysis.getSignals()) {
                        prompt.append("- ").append(signal).append("\n");
                    }
                }

                MetricsDTO metrics = analysis.getMetrics();
                if (metrics != null) {
                    prompt.append("예상 수익률: ").append(metrics.getExpectedTotalReturn()).append("%\n");
                    prompt.append("예상 일평균 수익률: ").append(metrics.getExpectedAvgDailyReturn()).append("%\n");
                    prompt.append("예상 변동성: ").append(metrics.getPredictedVolatility()).append("%\n");
                    prompt.append("상승 확률: ").append(metrics.getUpsideProbability() * 100).append("%\n");
                }

                RiskMetricsDTO riskMetrics = analysis.getRiskMetrics();
                if (riskMetrics != null) {
                    prompt.append("최대 예상 손실: ").append(riskMetrics.getMaxExpectedLoss()).append("%\n");
                    prompt.append("최대 예상 수익: ").append(riskMetrics.getMaxExpectedGain()).append("%\n");
                    prompt.append("Sharpe Ratio: ").append(riskMetrics.getEstimatedSharpeRatio()).append("\n");
                }
            }

            if (predictResponse.getPricePredictions() != null && !predictResponse.getPricePredictions().isEmpty()) {
                prompt.append("예측 가격 (다음 ").append(predictResponse.getPricePredictions().size()).append("일): ");
                for (int i = 0; i < Math.min(3, predictResponse.getPricePredictions().size()); i++) {
                    if (i > 0) prompt.append(", ");
                    prompt.append(predictResponse.getPricePredictions().get(i));
                }
                prompt.append("\n");
            }
            prompt.append("\n");
        }

        // 포트폴리오 분석 데이터 요약
        if (portfolioResponse != null && portfolioResponse.getMetrics() != null && !portfolioResponse.getMetrics().isEmpty()) {
            prompt.append("=== 포트폴리오 분석 ===\n");
            for (PortfolioResponseDTO.PortfolioMetric metric : portfolioResponse.getMetrics()) {
                PortfolioResponseDTO.QuantstatsMetrics qm = metric.getMetrics();
                if (qm != null) {
                    prompt.append("포트폴리오 ID: ").append(metric.getId()).append("\n");
                    prompt.append("기간: ").append(qm.getStartPeriod()).append(" ~ ").append(qm.getEndPeriod()).append("\n");
                    prompt.append("누적 수익률: ").append(qm.getCumulativeReturn() != null ? String.format("%.2f", qm.getCumulativeReturn() * 100) : "N/A").append("%\n");
                    prompt.append("연평균 수익률 (CAGR): ").append(qm.getCagr() != null ? String.format("%.2f", qm.getCagr() * 100) : "N/A").append("%\n");
                    prompt.append("Sharpe Ratio: ").append(qm.getSharpe() != null ? String.format("%.2f", qm.getSharpe()) : "N/A").append("\n");
                    prompt.append("Sortino Ratio: ").append(qm.getSortino() != null ? String.format("%.2f", qm.getSortino()) : "N/A").append("\n");
                    prompt.append("최대 낙폭 (Max Drawdown): ").append(qm.getMaxDrawdown() != null ? String.format("%.2f", qm.getMaxDrawdown() * 100) : "N/A").append("%\n");
                    prompt.append("변동성 (연율화): ").append(qm.getVolatilityAnnualized() != null ? String.format("%.2f", qm.getVolatilityAnnualized() * 100) : "N/A").append("%\n");
                    prompt.append("\n");
                }
            }
        }

        prompt.append("=== 요청 사항 ===\n");
        prompt.append("위 데이터를 바탕으로 다음 내용을 포함한 투자 점검 리포트를 작성해주세요:\n");
        prompt.append("1. 종합 투자 의견 (매수/보유/매도 추천 및 이유)\n");
        prompt.append("2. 주요 리스크 요인 분석\n");
        prompt.append("3. 포트폴리오 다각화 및 리밸런싱 제안\n");
        prompt.append("4. 단기/중기/장기 투자 전략 제안\n");
        prompt.append("5. 주의사항 및 투자 권고사항\n\n");
        prompt.append("답변은 한국어로 작성하고, 전문적이면서도 이해하기 쉬운 문체로 작성해주세요. 구체적인 수치와 데이터를 인용하여 설명해주세요.");

        return prompt.toString();
    }

}
