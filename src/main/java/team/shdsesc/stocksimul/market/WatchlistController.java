package team.shdsesc.stocksimul.market;

import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.shdsesc.stocksimul.market.dto.WatchlistResponse;
import team.shdsesc.stocksimul.market.service.WatchlistService;

import java.util.Map;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @PostConstruct
    public void ensureTable() {
        // JPA/Hibernate가 ddl-auto=update 설정으로 테이블을 관리합니다.
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getWatchlist(@RequestParam(value = "user", required = false) String user) {
        WatchlistResponse r = watchlistService.getWatchlist(user);
        return ResponseEntity.ok(Map.of("s", r.getS(), "tickers", r.getTickers()));
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addWatch(@RequestParam("ticker") String ticker,
                                                        @RequestParam(value = "user", required = false) String user) {
        if (ticker == null || ticker.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("s", "no_data", "msg", "ticker required"));
        }
        watchlistService.addWatch(ticker, user);
        return ResponseEntity.ok(Map.of("s", "ok"));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeWatch(@RequestParam("ticker") String ticker,
                                                           @RequestParam(value = "user", required = false) String user) {
        if (ticker == null || ticker.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("s", "no_data", "msg", "ticker required"));
        }
        watchlistService.removeWatch(ticker, user);
        return ResponseEntity.ok(Map.of("s", "ok"));
    }
}




