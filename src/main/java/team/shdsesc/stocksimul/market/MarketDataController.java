package team.shdsesc.stocksimul.market;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@RestController
@RequestMapping("/api/market")
public class MarketDataController {

    @Value("${finnhub.apiKey:}")
    private String finnhubApiKey;

    private final RestClient restClient = RestClient.create("https://finnhub.io/api/v1");

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
}


