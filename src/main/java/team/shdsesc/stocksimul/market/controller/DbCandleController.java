package team.shdsesc.stocksimul.market.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.shdsesc.stocksimul.market.dto.CandleResponse;
import team.shdsesc.stocksimul.market.dto.RangeResponse;
import team.shdsesc.stocksimul.market.dto.TickersResponse;
import team.shdsesc.stocksimul.market.dto.SymbolsResponse;
import team.shdsesc.stocksimul.market.service.DbMarketService;

import java.util.Map;

@RestController
@RequestMapping("/api/db")
@Log4j2
public class DbCandleController {

    private final DbMarketService dbMarketService;

    public DbCandleController(DbMarketService dbMarketService) {
        this.dbMarketService = dbMarketService;
    }

    @GetMapping("/candles")
    public ResponseEntity<Map<String, Object>> getCandles(
            @RequestParam(value = "ticker", required = false) String tickerParam,
            @RequestParam(value = "symbol", required = false) String symbolParam,
            @RequestParam(value = "from", required = false) Long from,
            @RequestParam(value = "to", required = false) Long to,
            @RequestParam(value = "days", required = false) Integer days
    ) {
        CandleResponse body = dbMarketService.getCandles(tickerParam, symbolParam, from, to, days);
        return ResponseEntity.ok(Map.of(
                "s", body.getStatus(),
                "t", body.getTimestamps(),
                "d", body.getDates(),
                "o", body.getOpens(),
                "h", body.getHighs(),
                "l", body.getLows(),
                "c", body.getCloses(),
                "v", body.getVolumes()
        ));
    }

    @GetMapping("/last-range")
    public ResponseEntity<Map<String, Object>> getLastRange(
            @RequestParam("ticker") String ticker,
            @RequestParam(value = "days", required = false) Integer days
    ) {
        RangeResponse r = dbMarketService.getLastRange(ticker, days);
        if (!"ok".equals(r.getStatus())) {
            return ResponseEntity.ok(Map.of("s", "no_data"));
        }
        return ResponseEntity.ok(Map.of("s", r.getStatus(), "last", r.getLast(), "from", r.getFrom(), "to", r.getTo()));
    }

    @GetMapping("/tickers")
    public ResponseEntity<Map<String, Object>> getTickers() {
        TickersResponse r = dbMarketService.getTickers();
        return ResponseEntity.ok(Map.of("s", r.getStatus(), "tickers", r.getTickers()));
    }

    @GetMapping("/symbols")
    public ResponseEntity<Map<String, Object>> getSymbols() {
        SymbolsResponse r = dbMarketService.getSymbols();
        return ResponseEntity.ok(Map.of("s", r.getStatus(), "symbols", r.getSymbols()));
    }
}



