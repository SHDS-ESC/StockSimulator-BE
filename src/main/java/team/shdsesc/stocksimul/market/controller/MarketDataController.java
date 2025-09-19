package team.shdsesc.stocksimul.market.controller;

import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
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
@RequestMapping("/api/market")
@Log4j2
public class MarketDataController {

    @Value("${finnhub.apiKey:}")
    private String finnhubApiKey;

    private final RestClient restClient = RestClient.create("https://finnhub.io/api/v1");
    private final DbMarketService dbMarketService;
    private final StockRedisDAO stockRedisDAO;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final List<String> allTickers = new ArrayList<>();
    private int currentTickerIndex = 0;
    private boolean schedulerEnabled = false;

    @Value("${market.scheduler.enabled:false}")
    private boolean schedulerEnabledDefault;

    @Value("${market.batch.size:60}")
    private int batchSize;

    @Value("${market.verbose.log:false}")
    private boolean verboseLog;

    @Value("${market.mock.on.redis.fail:true}")
    private boolean mockOnRedisFail;

    @Value("${market.mock.on.single.fail:true}")
    private boolean mockOnSingleFail;
    
    public MarketDataController(DbMarketService dbMarketService, StockRedisDAO stockRedisDAO) {
        this.dbMarketService = dbMarketService;
        this.stockRedisDAO = stockRedisDAO;
        this.schedulerEnabled = schedulerEnabledDefault;
        initializeTickers();
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

    @GetMapping("/quote")
    public ResponseEntity<String> getQuote(@RequestParam("symbol") String symbol) {
        try {
            String body = restClient
                    .get()
                    .uri((UriBuilder b) -> b
                            .path("/quote")
                            .queryParam("symbol", symbol)
                            .queryParam("token", finnhubApiKey)
                            .build())
                    .retrieve()
                    .body(String.class);
            return ResponseEntity.ok(body);
        } catch (HttpClientErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @GetMapping("/tickers")
    public ResponseEntity<Map<String, Object>> getTickers() {
        try {
            var tickersResponse = dbMarketService.getTickers();
            Map<String, Object> result = new HashMap<>();
            result.put("status", tickersResponse.getStatus());
            result.put("tickers", tickersResponse.getTickers());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "티커 목록을 가져오는데 실패했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/symbols")
    public ResponseEntity<Map<String, Object>> getSymbols() {
        try {
            var symbolsResponse = dbMarketService.getSymbols();
            Map<String, Object> result = new HashMap<>();
            result.put("status", symbolsResponse.getStatus());
            result.put("symbols", symbolsResponse.getSymbols());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "심볼 목록을 가져오는데 실패했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/redis/stocks")
    public ResponseEntity<List<RealTimeStockDTO>> getRedisStocks() {
        try {
            List<RealTimeStockDTO> stocks = stockRedisDAO.getAllStocks();
            return ResponseEntity.ok(stocks);
        } catch (Exception e) {
            log.warn("Redis 조회 실패: {}", e.getMessage());
            if (mockOnRedisFail) {
                try {
                    TickersResponse response = dbMarketService.getTickers();
                    if (response != null && response.getTickers() != null) {
                        List<RealTimeStockDTO> mockStocks = response.getTickers().stream()
                                .map(this::generateMockStockData)
                                .collect(Collectors.toList());
                        return ResponseEntity.ok(mockStocks);
                    }
                    return ResponseEntity.ok(Collections.emptyList());
                } catch (Exception dbError) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
            }
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/redis/stock/{symbol}")
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
            if (mockOnSingleFail) {
                RealTimeStockDTO mockStock = generateMockStockData(symbol);
                return ResponseEntity.ok(mockStock);
            }
            return ResponseEntity.status(502).build();
        }
    }

    @PostMapping("/redis/init")
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

    @PostMapping("/redis/scheduler/start")
    public ResponseEntity<String> startScheduler() {
        schedulerEnabled = true;
        return ResponseEntity.ok("스케줄러 시작됨");
    }

    @PostMapping("/redis/scheduler/stop")
    public ResponseEntity<String> stopScheduler() {
        schedulerEnabled = false;
        return ResponseEntity.ok("스케줄러 중지됨");
    }

    @GetMapping("/redis/scheduler/status")
    public ResponseEntity<Map<String, Object>> getSchedulerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", schedulerEnabled);
        status.put("totalTickers", allTickers.size());
        status.put("currentIndex", currentTickerIndex);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/redis/scheduler/run-now")
    public ResponseEntity<String> runSchedulerNow() {
        if (verboseLog) {
            log.info("수동 스케줄러 실행 요청됨 (배치 방식)");
        }
        updateAllTickersBatch();
        return ResponseEntity.ok("수동 스케줄러 실행됨");
    }

    @Scheduled(fixedRate = 20 * 60 * 1000)
    public void updateAllTickers() {
        if (!schedulerEnabled || allTickers.isEmpty()) {
            return;
        }
        updateAllTickersBatch();
    }

    private void updateAllTickersBatch() {
        if (allTickers.isEmpty()) {
            if (verboseLog) {
                log.info("티커 목록이 비어있습니다. Redis 초기화를 먼저 실행하세요.");
            }
            return;
        }
        CompletableFuture.runAsync(() -> {
            int totalBatches = (int) Math.ceil((double) allTickers.size() / Math.max(1, batchSize));
            int successCount = 0;
            int failCount = 0;
            long startTime = System.currentTimeMillis();
            for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
                int startIndex = batchIndex * Math.max(1, batchSize);
                int endIndex = Math.min(startIndex + Math.max(1, batchSize), allTickers.size());
                List<String> batchTickers = allTickers.subList(startIndex, endIndex);
                if (verboseLog) {
                    log.info("배치 {}/{} 시작 ({}개 티커)", (batchIndex + 1), totalBatches, batchTickers.size());
                }
                for (int i = 0; i < batchTickers.size(); i++) {
                    String ticker = batchTickers.get(i);
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = (currentTime - startTime) / 1000;
                    try {
                        if (verboseLog) {
                            log.info("[{}] {} 갱신 중... (경과: {}초)", String.format("%03d/%03d", startIndex + i + 1, allTickers.size()), ticker, elapsedTime);
                        }
                        String quoteJson = restClient
                                .get()
                                .uri((UriBuilder b) -> b
                                        .path("/quote")
                                        .queryParam("symbol", ticker)
                                        .queryParam("token", finnhubApiKey)
                                        .build())
                                .retrieve()
                                .body(String.class);
                        RealTimeStockDTO realStock = parseQuoteToStockDTO(ticker, quoteJson);
                        if (realStock != null) {
                            stockRedisDAO.saveStock(realStock);
                            successCount++;
                            if (verboseLog) {
                                log.info("[{}] {} 갱신 성공! (성공: {}, 실패: {})", String.format("%03d/%03d", startIndex + i + 1, allTickers.size()), ticker, successCount, failCount);
                            }
                        } else {
                            failCount++;
                            if (verboseLog) {
                                log.info("[{}] {} 파싱 실패 (성공: {}, 실패: {})", String.format("%03d/%03d", startIndex + i + 1, allTickers.size()), ticker, successCount, failCount);
                            }
                        }
                    } catch (Exception e) {
                        failCount++;
                        log.warn("[{}] {} 오류: {} (성공: {}, 실패: {})", String.format("%03d/%03d", startIndex + i + 1, allTickers.size()), ticker, e.getMessage(), successCount, failCount);
                        if (e.getMessage() != null && e.getMessage().contains("429")) {
                            log.warn("API 리미트 도달! 1분 대기 후 계속...");
                            try { Thread.sleep(60 * 1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
                        }
                    }
                    if (i < batchTickers.size() - 1) {
                        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                    }
                }
                if (verboseLog) { log.info("배치 {}/{} 완료", (batchIndex + 1), totalBatches); }
                if (batchIndex < totalBatches - 1) {
                    if (verboseLog) { log.info("1분 대기 중... (API 제한 준수)"); }
                    try { Thread.sleep(60 * 1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
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
        });
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
                    .name(getStockName(symbol))
                    .price(price)
                    .change(changeStr)
                    .changePercent(changePercentStr)
                    .volume("0")
                    .logo(getStockLogo(symbol))
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
                .name(getStockName(symbol))
                .price(String.format("%.2f", basePrice))
                .change(String.format("%+.2f", change))
                .changePercent(String.format("%+.2f", changePercent))
                .volume(String.valueOf(random.nextInt(1000000) + 100000))
                .logo(getStockLogo(symbol))
                .build();
    }

    private String getStockName(String symbol) {
		return symbol;
    }

    private String getStockLogo(String symbol) {
		return "📈";
    }
}



