package team.shdsesc.stocksimul.market.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
// import org.springframework.core.ParameterizedTypeReference; // unused
import org.springframework.http.MediaType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import lombok.extern.log4j.Log4j2;
import team.shdsesc.stocksimul.market.service.DbMarketService;

import java.util.*;

@RestController
@RequestMapping("/api/market")
@Log4j2
public class MarketDataController {

    @Value("${finnhub.apiKey:}")
    private String finnhubApiKey;

    private final RestClient restClient = RestClient.create("https://finnhub.io/api/v1");
    private final DbMarketService dbMarketService;
    
    public MarketDataController(DbMarketService dbMarketService) {
        this.dbMarketService = dbMarketService;
    }

    @GetMapping("/quote")
    public ResponseEntity<?> getQuote(@RequestParam("symbol") String symbol) {
        try {
            if (finnhubApiKey == null || finnhubApiKey.isBlank()) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("status", "error");
                resp.put("message", "Finnhub API key is not configured (finnhub.apiKey)");
                return ResponseEntity.status(500).body(resp);
            }

            // Finnhub 호출 (압축 비활성화)
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

            if (raw == null || raw.isBlank()) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("status", "error");
                resp.put("message", "Empty response from finnhub");
                return ResponseEntity.status(502).body(resp);
            }

            // JSON 파싱만 시도, 실패 시 원문 그대로 반환
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> parsed = mapper.readValue(raw, new TypeReference<Map<String, Object>>(){ });
                return ResponseEntity.ok(parsed);
            } catch (Exception parseEx) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(raw);
            }
        } catch (HttpClientErrorException ex) {
            // Finnhub에서 온 에러를 JSON으로 전달 시도
            try {
                return ResponseEntity.status(ex.getStatusCode()).body(new com.fasterxml.jackson.databind.ObjectMapper().readValue(ex.getResponseBodyAsString(), Map.class));
            } catch (Exception ignore) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("status", "error");
                resp.put("message", ex.getResponseBodyAsString());
                return ResponseEntity.status(ex.getStatusCode()).body(resp);
            }
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "error");
            resp.put("message", e.getMessage());
            return ResponseEntity.status(500).body(resp);
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

}