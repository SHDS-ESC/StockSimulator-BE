package team.shdsesc.stocksimul.market;

import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.log4j.Log4j2;
import team.shdsesc.stocksimul.market.service.DbMarketService;
import team.shdsesc.stocksimul.market.dto.TickersResponse;
import team.shdsesc.stocksimul.redis.dao.StockRedisDAO;
import team.shdsesc.stocksimul.stock.RealTimeStockDTO;

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
    
    
    // 스케줄러용 변수
    private final List<String> allTickers = new ArrayList<>();
    private int currentTickerIndex = 0;
    private boolean schedulerEnabled = false;

    // 동작 토글 및 파라미터 (application.properties 미변경 시에도 기본값으로 작동)

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
        
        // 스케줄러 기본 상태 반영 및 티커 목록 초기화
        this.schedulerEnabled = schedulerEnabledDefault;
        initializeTickers();
    }
    
    // 티커 목록 초기화
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

    @GetMapping("/candles")
    public ResponseEntity<String> getCandles(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "resolution", required = false) String resolution,
            @RequestParam(value = "from", required = false) Long from,
            @RequestParam(value = "to", required = false) Long to,
            @RequestParam(value = "range", required = false) String range // 예: 1h, 24h, 7d
    ) {
        try {
            // 기본값 채우기
            final long nowSec = System.currentTimeMillis() / 1000L;
            final long toSec = (to == null) ? nowSec : to;
            final long fromSec;
            {
                long tmpFrom = (from == null) ? -1 : from;
                if (tmpFrom < 0) {
                    long delta = 24 * 60 * 60; // 기본 24h
                    if (range != null && !range.isBlank()) {
                        delta = parseRangeToSeconds(range.trim());
                    }
                    tmpFrom = Math.max(0, toSec - delta);
                }
                fromSec = tmpFrom;
            }
            final String resVal = (resolution == null || resolution.isBlank()) ? "60" : resolution;

            boolean isCrypto = symbol != null && symbol.contains(":");
            String api = isCrypto ? "/crypto/candle" : "/stock/candle";

            String body = restClient
                    .get()
                    .uri((UriBuilder b) -> b
                            .path(api)
                            .queryParam("symbol", symbol)
                            .queryParam("resolution", resVal)
                            .queryParam("from", fromSec)
                            .queryParam("to", toSec)
                            .queryParam("token", finnhubApiKey)
                            .build())
                    .retrieve()
                    .body(String.class);
            return ResponseEntity.ok(body);
        } catch (HttpClientErrorException ex) {
            // 상류 상태코드/본문 그대로 전달
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    private long parseRangeToSeconds(String r) {
        try {
            String lower = r.toLowerCase();
            if (lower.endsWith("h")) {
                long h = Long.parseLong(lower.substring(0, lower.length() - 1));
                return Math.max(60, h * 3600);
            }
            if (lower.endsWith("d")) {
                long d = Long.parseLong(lower.substring(0, lower.length() - 1));
                return Math.max(3600, d * 86400);
            }
            // 숫자만 오면 초로 해석
            return Math.max(60, Long.parseLong(lower));
        } catch (Exception e) {
            return 86400; // 기본 24h
        }
    }

    

    // 티커 목록 조회
    @GetMapping("/tickers")
    public ResponseEntity<Map<String, Object>> getTickers() {
        try {
            var tickersResponse = dbMarketService.getTickers();
            Map<String, Object> result = new HashMap<>();
            result.put("status", tickersResponse    .getS());
            result.put("tickers", tickersResponse.getTickers());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "티커 목록을 가져오는데 실패했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    // 심볼 목록 조회 (티커 + 이름 + 섹터 정보)
    @GetMapping("/symbols")
    public ResponseEntity<Map<String, Object>> getSymbols() {
        try {
            var symbolsResponse = dbMarketService.getSymbols();
            Map<String, Object> result = new HashMap<>();
            result.put("status", symbolsResponse.getS());
            result.put("symbols", symbolsResponse.getSymbols());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "심볼 목록을 가져오는데 실패했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    // Redis에서 모든 주식 데이터 조회
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

    // Redis에서 특정 주식 데이터 조회
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

    // Redis에 주식 데이터 저장 (초기화용)
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
            
            // 모든 티커에 대해 모의 데이터로 초기화
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

    // 스케줄러 시작/중지
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

    // 스케줄러 상태 조회
    @GetMapping("/redis/scheduler/status")
    public ResponseEntity<Map<String, Object>> getSchedulerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", schedulerEnabled);
        status.put("totalTickers", allTickers.size());
        status.put("currentIndex", currentTickerIndex);
        return ResponseEntity.ok(status);
    }

    // 스케줄러 수동 실행 (테스트용)
    @PostMapping("/redis/scheduler/run-now")
    public ResponseEntity<String> runSchedulerNow() {
        if (verboseLog) {
            log.info("수동 스케줄러 실행 요청됨 (배치 방식)");
        }
        updateAllTickersBatch();
        return ResponseEntity.ok("수동 스케줄러 실행됨");
    }

    // 단일 심볼 강제 갱신 (라이브 값과 동기화용)
    @PostMapping("/redis/stock/{symbol}/refresh")
    public ResponseEntity<String> refreshOne(@PathVariable String symbol) {
        try {
            String quoteJson = restClient
                    .get()
                    .uri((UriBuilder b) -> b
                            .path("/quote")
                            .queryParam("symbol", symbol)
                            .queryParam("token", finnhubApiKey)
                            .build())
                    .retrieve()
                    .body(String.class);

            RealTimeStockDTO dto = parseQuoteToStockDTO(symbol, quoteJson);
            if (dto == null) {
                return ResponseEntity.status(502).body("quote 파싱 실패: " + symbol);
            }
            stockRedisDAO.saveStock(dto);
            return ResponseEntity.ok(symbol + " 갱신 완료");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("갱신 실패: " + e.getMessage());
        }
    }

    // 20분마다 실행되는 스케줄러 (60개 배치 + 1분 대기 방식)
    @Scheduled(fixedRate = 20 * 60 * 1000)
    public void updateAllTickers() {
        if (!schedulerEnabled || allTickers.isEmpty()) {
            return;
        }
        updateAllTickersBatch();
    }

    // 새로운 배치 방식 갱신 로직 (60개씩 배치 + 1분 대기)
    private void updateAllTickersBatch() {
        if (allTickers.isEmpty()) {
            if (verboseLog) {
                log.info("티커 목록이 비어있습니다. Redis 초기화를 먼저 실행하세요.");
            }
            return;
        }

        if (verboseLog) {
            log.info("==========================================");
            log.info("배치 스케줄러 시작: {}개씩 배치", batchSize);
            log.info("시작 시간: {}", new java.util.Date());
            log.info("총 티커 수: {}개", allTickers.size());
            log.info("예상 완료 시간: {}", new java.util.Date(System.currentTimeMillis() + 12 * 60 * 1000));
            log.info("==========================================");
        }
        
        // 비동기로 배치 갱신 실행
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
                
                // 현재 배치의 모든 티커를 빠르게 처리
                for (int i = 0; i < batchTickers.size(); i++) {
                    String ticker = batchTickers.get(i);
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = (currentTime - startTime) / 1000;
                    
                    try {
                        if (verboseLog) {
                            log.info("[{}] {} 갱신 중... (경과: {}초)", String.format("%03d/%03d", startIndex + i + 1, allTickers.size()), ticker, elapsedTime);
                        }
                        
                        // API에서 실제 데이터 가져오기
                        String quoteJson = restClient
                                .get()
                                .uri((UriBuilder b) -> b
                                        .path("/quote")
                                        .queryParam("symbol", ticker)
                                        .queryParam("token", finnhubApiKey)
                                        .build())
                                .retrieve()
                                .body(String.class);

                        // JSON 파싱하여 RealTimeStockDTO 생성
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
                        
                        // API 리미트 오류인지 확인
                        if (e.getMessage() != null && e.getMessage().contains("429")) {
                            log.warn("API 리미트 도달! 1분 대기 후 계속...");
                            try {
                                Thread.sleep(60 * 1000); // 1분 대기
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                    
                    // 배치 내에서는 짧은 간격으로 요청 (0.1초)
                    if (i < batchTickers.size() - 1) {
                        try {
                            Thread.sleep(100); // 0.1초 대기
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                
                if (verboseLog) {
                    log.info("배치 {}/{} 완료", (batchIndex + 1), totalBatches);
                }
                
                // 마지막 배치가 아니면 1분 대기
                if (batchIndex < totalBatches - 1) {
                    if (verboseLog) {
                        log.info("1분 대기 중... (API 제한 준수)");
                    }
                    try {
                        Thread.sleep(60 * 1000); // 1분 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
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
        });
    }

    // updateAllTickersInternal 제거됨 (배치 방식으로 통일)

    // JSON 응답을 RealTimeStockDTO로 파싱 (Finnhub quote)
    private RealTimeStockDTO parseQuoteToStockDTO(String symbol, String quoteJson) {
        try {
            JsonNode root = objectMapper.readTree(quoteJson);
            double c = root.path("c").asDouble(Double.NaN);   // current price
            double pc = root.path("pc").asDouble(Double.NaN); // previous close
            double d = root.hasNonNull("d") ? root.path("d").asDouble() : (Double.isNaN(c) || Double.isNaN(pc) ? 0.0 : (c - pc));
            double dp = root.hasNonNull("dp") ? root.path("dp").asDouble() : (Double.isNaN(c) || Double.isNaN(pc) || pc == 0 ? 0.0 : (d / pc) * 100.0);

            if (Double.isNaN(c)) {
                return null;
            }

            String price = String.format("$%.2f", c);
            String change = String.format("%+.2f", d);
            String changePercent = String.format("%+.2f%%", dp);

            return RealTimeStockDTO.builder()
                    .symbol(symbol)
                    .name(getStockName(symbol))
                    .price(price)
                    .change(change)
                    .changePercent(changePercent)
                    .volume("0")
                    .logo(getStockLogo(symbol))
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    // 모의 주식 데이터 생성
    private RealTimeStockDTO generateMockStockData(String symbol) {
        Random random = new Random();
        double basePrice = 50 + random.nextDouble() * 200; // 50-250 사이의 가격
        double change = (random.nextDouble() - 0.5) * 10; // -5 ~ +5 사이의 변동
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

    // 주식 이름 매핑
    private String getStockName(String symbol) {
        Map<String, String> nameMap = Map.of(
                "AAPL", "Apple Inc.",
                "MSFT", "Microsoft Corporation",
                "GOOGL", "Alphabet Inc.",
                "AMZN", "Amazon.com Inc.",
                "TSLA", "Tesla Inc.",
                "META", "Meta Platforms Inc.",
                "NVDA", "NVIDIA Corporation",
                "NFLX", "Netflix Inc.",
                "AMD", "Advanced Micro Devices",
                "INTC", "Intel Corporation"
        );
        return nameMap.getOrDefault(symbol, symbol + " Corporation");
    }

    // 주식 로고 매핑
    private String getStockLogo(String symbol) {
        Map<String, String> logoMap = Map.of(
                "AAPL", "🍎",
                "MSFT", "🪟",
                "GOOGL", "🔍",
                "AMZN", "📦",
                "TSLA", "⚡",
                "META", "📘",
                "NVDA", "🎮",
                "NFLX", "🎬",
                "AMD", "💻",
                "INTC", "🔧"
        );
        return logoMap.getOrDefault(symbol, "📈");
    }
}


