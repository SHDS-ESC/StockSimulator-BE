package team.shdsesc.stocksimul.market.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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

}