package team.shdsesc.stocksimul.redis.controller;

import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import lombok.extern.log4j.Log4j2;
import team.shdsesc.stocksimul.market.service.DbMarketService;
import team.shdsesc.stocksimul.market.dto.TickersResponse;
import team.shdsesc.stocksimul.redis.dao.StockRedisDAO;
import team.shdsesc.stocksimul.market.dto.RealTimeStockDTO;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/redis")
@Log4j2
public class RedisController {

    @Value("${finnhub.apiKey:}")
    private String finnhubApiKey;

    private final RestClient restClient = RestClient.create("https://finnhub.io/api/v1");
    private final DbMarketService dbMarketService;
    private final StockRedisDAO stockRedisDAO;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final List<String> allTickers = new ArrayList<>();
    private boolean schedulerEnabled = false;

    @Value("${market.scheduler.enabled:false}")
    private boolean schedulerEnabledDefault;

    @Value("${market.batch.size:50}")
    private int batchSize;

    @Value("${market.verbose.log:false}")
    private boolean verboseLog;

    @Value("${market.per.request.delay.ms:1000}")
    private int perRequestDelayMs;

    @Value("${market.sleep.between.batches.ms:60000}")
    private int sleepBetweenBatchesMs;

    @Value("${market.ui.log.max:500}")
    private int uiLogMax;

    private final java.util.concurrent.atomic.AtomicInteger uiLogSeq = new java.util.concurrent.atomic.AtomicInteger(0);
    private final List<Map<String, Object>> uiLogs = java.util.Collections.synchronizedList(new ArrayList<>());
    
    public RedisController(DbMarketService dbMarketService, StockRedisDAO stockRedisDAO) {
        this.dbMarketService = dbMarketService;
        this.stockRedisDAO = stockRedisDAO;
        this.schedulerEnabled = schedulerEnabledDefault;
        initializeTickers();
    }

    private void uiLog(String message) {
        try {
            int id = uiLogSeq.incrementAndGet();
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", id);
            entry.put("time", new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
            entry.put("message", message);
            uploadUiLog(entry);
            if (verboseLog) {
                log.info("[UI] {}", message);
            }
        } catch (Exception ignore) {}
    }

    private void uploadUiLog(Map<String, Object> entry) {
        synchronized (uiLogs) {
            uiLogs.add(entry);
            int overflow = uiLogs.size() - Math.max(50, uiLogMax);
            if (overflow > 0) {
                uiLogs.subList(0, overflow).clear();
            }
        }
    }
    
    private void initializeTickers() {
        try {
            allTickers.clear();
            TickersResponse response = dbMarketService.getTickers();
            if (response != null && response.getTickers() != null) {
                allTickers.addAll(response.getTickers());
            }
            if (verboseLog) {
                log.info("티커 목록 초기화 완료: {}개", allTickers.size());
            }
        } catch (Exception e) {
            log.warn("티커 목록 초기화 실패: {}", e.getMessage());
        }
    }

    // Redis 데이터 조회 (읽기 전용)
    @GetMapping("/stocks")
    public ResponseEntity<List<RealTimeStockDTO>> getRedisStocks(@RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        try {
            List<RealTimeStockDTO> stocks = stockRedisDAO.getAllStocks();
            String etag = "\"stocks-" + stocks.size() + "\"";
            if (etag.equals(ifNoneMatch)) {
                return ResponseEntity.status(304).eTag(etag).build();
            }
            return ResponseEntity.ok()
                    .eTag(etag)
                    .header("Cache-Control", "public, max-age=1140") // 19분
                    .body(stocks);
        } catch (Exception e) {
            log.warn("Redis 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/stock/{symbol}")
    public ResponseEntity<RealTimeStockDTO> getRedisStock(@PathVariable String symbol) {
        try {
            RealTimeStockDTO stock = stockRedisDAO.getStock(symbol);
            if (stock != null) {
                return ResponseEntity.ok(stock);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.warn("Redis 개별 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(502).build();
        }
    }

    // Redis 초기화 (테스트용 Mock 데이터)
    @PostMapping("/init")
    public ResponseEntity<String> initRedisStocks() {
        try {
            TickersResponse response = dbMarketService.getTickers();
            if (response == null || response.getTickers() == null) {
                return ResponseEntity.ok("Redis 초기화 실패: 티커 데이터를 가져올 수 없습니다.");
            }
            List<String> tickers = response.getTickers();
            if (verboseLog) {
                log.info("Redis 초기화 시작: {}개 티커", tickers.size());
            }
            // 테스트용 Mock 데이터 생성
            for (String ticker : tickers) {
                RealTimeStockDTO mockStock = generateMockStockData(ticker);
                stockRedisDAO.saveStock(mockStock);
            }
            return ResponseEntity.ok("Redis 초기화 완료: " + tickers.size() + "개 주식 저장");
        } catch (Exception e) {
            log.warn("Redis 초기화 실패: {}", e.getMessage());
            return ResponseEntity.ok("Redis 초기화 실패: " + e.getMessage() + " (Redis 서버가 실행되지 않았을 수 있습니다)");
        }
    }

    // 스케줄러 제어
    @PostMapping("/scheduler/start")
    public ResponseEntity<String> startScheduler() {
        schedulerEnabled = true;
        uiLog("스케줄러 시작됨");
        return ResponseEntity.ok("스케줄러 시작됨");
    }

    @PostMapping("/scheduler/stop")
    public ResponseEntity<String> stopScheduler() {
        schedulerEnabled = false;
        uiLog("스케줄러 중지됨");
        return ResponseEntity.ok("스케줄러 중지됨");
    }

    @GetMapping("/scheduler/status")
    public ResponseEntity<Map<String, Object>> getSchedulerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", schedulerEnabled);
        status.put("totalTickers", allTickers.size());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/scheduler/run-now")
    public ResponseEntity<String> runSchedulerNow() {
        if (verboseLog) {
            log.info("수동 스케줄러 실행 요청됨 (배치 방식)");
        }
        uiLog("수동 스케줄러 실행 요청됨");
        updateAllTickersBatch();
        return ResponseEntity.ok("수동 스케줄러 실행됨");
    }

    // 스케줄러 설정
    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> updateRedisSchedulerConfig(@RequestBody Map<String, Object> body) {
        Map<String, Object> out = new HashMap<>();
        try {
            if (body.containsKey("batchSize")) {
                Object v = body.get("batchSize");
                if (v instanceof Number) batchSize = ((Number) v).intValue();
                else batchSize = Integer.parseInt(String.valueOf(v));
            }
            if (body.containsKey("perRequestDelayMs")) {
                Object v = body.get("perRequestDelayMs");
                if (v instanceof Number) perRequestDelayMs = ((Number) v).intValue();
                else perRequestDelayMs = Integer.parseInt(String.valueOf(v));
            }
            if (body.containsKey("sleepBetweenBatchesMs")) {
                Object v = body.get("sleepBetweenBatchesMs");
                if (v instanceof Number) sleepBetweenBatchesMs = ((Number) v).intValue();
                else sleepBetweenBatchesMs = Integer.parseInt(String.valueOf(v));
            }
            if (body.containsKey("verboseLog")) {
                Object v = body.get("verboseLog");
                if (v instanceof Boolean) verboseLog = (Boolean) v; else verboseLog = Boolean.parseBoolean(String.valueOf(v));
            }
            if (body.containsKey("enabled")) {
                Object v = body.get("enabled");
                boolean en = (v instanceof Boolean) ? (Boolean) v : Boolean.parseBoolean(String.valueOf(v));
                schedulerEnabled = en;
            }
            out.put("status", "ok");
            out.put("batchSize", batchSize);
            out.put("perRequestDelayMs", perRequestDelayMs);
            out.put("sleepBetweenBatchesMs", sleepBetweenBatchesMs);
            out.put("verboseLog", verboseLog);
            out.put("enabled", schedulerEnabled);
            uiLog("설정 변경 적용: batch=" + batchSize + ", delayMs=" + perRequestDelayMs + ", sleepMs=" + sleepBetweenBatchesMs + ", verbose=" + verboseLog + ", enabled=" + schedulerEnabled);
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            out.put("status", "error");
            out.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(out);
        }
    }

    // UI 로그
    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getUiLogs(@RequestParam(value = "after", required = false) Integer after) {
        int pivot = after == null ? 0 : after;
        List<Map<String, Object>> lines;
        int lastId;
        synchronized (uiLogs) {
            lastId = uiLogSeq.get();
            if (uiLogs.isEmpty()) {
                lines = java.util.Collections.emptyList();
            } else {
                lines = uiLogs.stream()
                    .filter(e -> ((Number) e.get("id")).intValue() > pivot)
                    .collect(Collectors.toList());
            }
        }
        Map<String, Object> out = new HashMap<>();
        out.put("lastId", lastId);
        out.put("entries", lines);
        return ResponseEntity.ok(out);
    }

    // 자동 스케줄러 (20분마다)
    @Scheduled(fixedRate = 20 * 60 * 1000)
    public void updateAllTickers() {
        if (!schedulerEnabled || allTickers.isEmpty()) {
            return;
        }
        updateAllTickersBatch();
    }

    // 실제 배치 처리 로직
    private void updateAllTickersBatch() {
        if (allTickers.isEmpty()) {
            if (verboseLog) {
                log.info("티커 목록이 비어있습니다. Redis 초기화를 먼저 실행하세요.");
            }
            uiLog("티커 목록이 비어있습니다. Redis 초기화를 먼저 실행하세요.");
            return;
        }
        CompletableFuture.runAsync(() -> {
            int totalBatches = (int) Math.ceil((double) allTickers.size() / Math.max(1, batchSize));
            int successCount = 0;
            int failCount = 0;
            long startTime = System.currentTimeMillis();
            uiLog("배치 스케줄러 시작: 총 " + allTickers.size() + "개, 배치수=" + totalBatches + ", 배치크기=" + batchSize);
            
            for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
                int startIndex = batchIndex * Math.max(1, batchSize);
                int endIndex = Math.min(startIndex + Math.max(1, batchSize), allTickers.size());
                List<String> batchTickers = allTickers.subList(startIndex, endIndex);
                if (verboseLog) {
                    log.info("배치 {}/{} 시작 ({}개 티커)", (batchIndex + 1), totalBatches, batchTickers.size());
                }
                uiLog("배치 " + (batchIndex + 1) + "/" + totalBatches + " 시작 (" + batchTickers.size() + "개)");
                
                for (int i = 0; i < batchTickers.size(); i++) {
                    String ticker = batchTickers.get(i);
                    try {
                        // 실제 API 호출 (재시도/헤더/로그 포함)
                        String quoteJson = fetchQuoteRaw(ticker);
                        if (quoteJson == null || quoteJson.isBlank()) {
                            failCount++;
                            if (verboseLog) log.warn("빈 응답: {}", ticker);
                            continue;
                        }

                        RealTimeStockDTO realStock = parseQuoteToStockDTO(ticker, quoteJson);
                        if (realStock == null) {
                            // 최소한 현재가(c)만이라도 복구 시도
                            Double c = extractPriceC(quoteJson);
                            if (c != null && !Double.isNaN(c)) {
                                String price = String.format("$%.2f", c);
                                realStock = RealTimeStockDTO.builder()
                                        .symbol(ticker)
                                        .name(ticker)
                                        .price(price)
                                        .change("+0.00")
                                        .changePercent("+0.00%")
                                        .volume("0")
                                        .logo("📈")
                                        .build();
                            }
                        }
                        if (realStock != null) {
                            stockRedisDAO.saveStock(realStock);
                            successCount++;
                        } else {
                            failCount++;
                            if (verboseLog) {
                                String snippet = quoteJson.substring(0, Math.min(80, quoteJson.length()));
                                log.warn("파싱 실패: {} snippet={}", ticker, snippet);
                            }
                        }
                    } catch (Exception e) {
                        failCount++;
                        log.warn("[{}] {} 오류: {}", String.format("%03d/%03d", startIndex + i + 1, allTickers.size()), ticker, e.getMessage());
                        if (e.getMessage() != null && e.getMessage().contains("429")) {
                            log.warn("API 리미트 도달! 1분 대기 후 계속...");
                            uiLog("API 리미트(429) 도달. 60초 대기...");
                            try { Thread.sleep(60 * 1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
                        }
                    }
                    
                    // 요청 간 지연
                    if (i < batchTickers.size() - 1) {
                        try { Thread.sleep(Math.max(0, perRequestDelayMs)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                    }
                }
                
                if (verboseLog) { log.info("배치 {}/{} 완료", (batchIndex + 1), totalBatches); }
                uiLog("배치 " + (batchIndex + 1) + "/" + totalBatches + " 완료 (성공:" + successCount + ", 실패:" + failCount + ")");
                
                // 배치 간 대기
                if (batchIndex < totalBatches - 1) {
                    if (sleepBetweenBatchesMs > 0) {
                        if (verboseLog) { log.info("다음 배치 전 대기 중... {}ms", sleepBetweenBatchesMs); }
                        uiLog("다음 배치 전 대기: " + sleepBetweenBatchesMs + "ms");
                        try { Thread.sleep(sleepBetweenBatchesMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                    }
                }
            }
            
            long totalTime = (System.currentTimeMillis() - startTime) / 1000;
            if (verboseLog) {
                log.info("==========================================");
                log.info("배치 스케줄러 완료: 전체 티커 갱신 완료");
                log.info("성공: {}개", successCount);
                log.info("실패: {}개", failCount);
                log.info("총 소요 시간: {}초", totalTime);
                log.info("완료 시간: {}", new java.util.Date());
                log.info("==========================================");
            }
            uiLog("배치 스케줄러 완료: 성공=" + successCount + ", 실패=" + failCount + ", 소요=" + totalTime + "초");
        });
    }

    // 헬퍼 메서드들
    private String fetchQuoteRaw(String symbol) {
        try {
            org.springframework.http.ResponseEntity<String> fh = restClient
                    .get()
                    .uri((UriBuilder b) -> b
                            .path("/quote")
                            .queryParam("symbol", symbol)
                            .queryParam("token", finnhubApiKey)
                            .build())
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "identity")
                    .retrieve()
                    .toEntity(String.class);
            String raw = fh.getBody();
            // 비정상으로 보이면 1회 재시도
            boolean broken = (raw == null) || raw.trim().length() < 5 || !raw.contains("}");
            if (broken) {
                org.springframework.http.ResponseEntity<String> retry = restClient
                        .get()
                        .uri((UriBuilder b) -> b
                                .path("/quote")
                                .queryParam("symbol", symbol)
                                .queryParam("token", finnhubApiKey)
                                .queryParam("_", String.valueOf(System.currentTimeMillis()))
                                .build())
                        .header("Accept", "application/json")
                        .header("Accept-Encoding", "identity")
                        .retrieve()
                        .toEntity(String.class);
                String raw2 = retry.getBody();
                return raw2 != null ? raw2 : raw;
            }
            return raw;
        } catch (Exception e) {
            if (verboseLog) { log.warn("quote fetch error {}: {}", symbol, e.getMessage()); }
            return null;
        }
    }

    private Double extractPriceC(String json) {
        if (json == null) return null;
        try {
            // 빠른 경량 파싱 시도
            JsonNode root = objectMapper.readTree(json);
            if (root.has("c")) return root.path("c").asDouble(Double.NaN);
        } catch (Exception ignore) {}
        try {
            // 매우 비정상 문자열에서 숫자만 추출 시도 ("\"c\":123.45")
            int idx = json.indexOf("\"c\"");
            if (idx >= 0) {
                int colon = json.indexOf(":", idx);
                if (colon > idx) {
                    int end = colon + 1;
                    while (end < json.length() && (Character.isWhitespace(json.charAt(end)) || json.charAt(end) == '"')) end++;
                    int stop = end;
                    while (stop < json.length() && (Character.isDigit(json.charAt(stop)) || json.charAt(stop) == '.' || json.charAt(stop) == '-')) stop++;
                    String num = json.substring(end, stop);
                    if (!num.isBlank()) return Double.parseDouble(num);
                }
            }
        } catch (Exception ignore) {}
        return null;
    }
    private RealTimeStockDTO parseQuoteToStockDTO(String symbol, String quoteJson) {
        try {
            JsonNode root = objectMapper.readTree(quoteJson);
            double currentPrice = root.path("c").asDouble(Double.NaN);
            double previousClose = root.path("pc").asDouble(Double.NaN);
            double change = root.hasNonNull("d") ? root.path("d").asDouble() : (Double.isNaN(currentPrice) || Double.isNaN(previousClose) ? 0.0 : (currentPrice - previousClose));
            double changePercent = root.hasNonNull("dp") ? root.path("dp").asDouble() : (Double.isNaN(currentPrice) || Double.isNaN(previousClose) || previousClose == 0 ? 0.0 : (change / previousClose) * 100.0);
            if (Double.isNaN(currentPrice)) { return null; }
            String price = String.format("$%.2f", currentPrice);
            String changeStr = String.format("%+.2f", change);
            String changePercentStr = String.format("%+.2f%%", changePercent);
            return RealTimeStockDTO.builder()
                    .symbol(symbol)
                    .name(symbol) // 간단히 심볼 사용
                    .price(price)
                    .change(changeStr)
                    .changePercent(changePercentStr)
                    .volume("0")
                    .logo("📈")
                    .build();
        } catch (Exception e) { return null; }
    }

    private RealTimeStockDTO generateMockStockData(String symbol) {
        Random random = new Random();
        double basePrice = 50 + random.nextDouble() * 200;
        double change = (random.nextDouble() - 0.5) * 10;
        double changePercent = (change / basePrice) * 100;
        return RealTimeStockDTO.builder()
                .symbol(symbol)
                .name(symbol)
                .price(String.format("%.2f", basePrice))
                .change(String.format("%+.2f", change))
                .changePercent(String.format("%+.2f", changePercent))
                .volume(String.valueOf(random.nextInt(1000000) + 100000))
                .logo("📈")
                .build();
    }
}