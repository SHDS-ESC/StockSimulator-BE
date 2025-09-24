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

import java.time.LocalDate;
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
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        // 풀네임 키만 제공
        resp.put("status", body.getStatus());
        resp.put("timestamps", body.getTimestamps());
        resp.put("dates", body.getDates());
        resp.put("opens", body.getOpens());
        resp.put("highs", body.getHighs());
        resp.put("lows", body.getLows());
        resp.put("closes", body.getCloses());
        resp.put("volumes", body.getVolumes());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/last-range")
    public ResponseEntity<Map<String, Object>> getLastRange(
            @RequestParam("ticker") String ticker,
            @RequestParam(value = "days", required = false) Integer days
    ) {
        RangeResponse r = dbMarketService.getLastRange(ticker, days);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        String status = r.getStatus();
        resp.put("status", status);
        if (!"ok".equals(status)) {
            return ResponseEntity.ok(resp);
        }
        resp.put("last", r.getLast());
        resp.put("from", r.getFrom());
        resp.put("to", r.getTo());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/tickers")
    public ResponseEntity<Map<String, Object>> getTickers() {
        TickersResponse r = dbMarketService.getTickers();
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("status", r.getStatus());
        resp.put("tickers", r.getTickers());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/symbols")
    public ResponseEntity<Map<String, Object>> getSymbols() {
        SymbolsResponse r = dbMarketService.getSymbols();
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("status", r.getStatus());
        resp.put("symbols", r.getSymbols());
        return ResponseEntity.ok(resp);
    }

    // 과거 스냅샷: 지정 날짜의 종가/전일 종가 기반 지표를 일괄 반환
    @GetMapping("/snapshot")
    public ResponseEntity<Map<String, Object>> getSnapshot(
            @RequestParam("date") String dateStr,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "6") Integer size,
            @RequestParam(value = "sort", required = false, defaultValue = "changePercent,desc") String sort,
            @RequestParam(value = "symbols", required = false) String symbolsCsv
    ) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            // 휴장일 자동 스킵이 포함된 응답 사용 (기본 30일 범위)
            java.util.Map<String, Object> pageResp = dbMarketService.getSnapshotPageWithSkip(date, page, size, sort, symbolsCsv, 30);
            return ResponseEntity.ok(pageResp);
        } catch (Exception e) {
            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("status", "error");
            resp.put("message", e.getMessage());
            resp.put("rows", java.util.List.of());
            return ResponseEntity.ok(resp);
        }
    }

    // 다음 유효 거래일 조회 API
    @GetMapping("/next-trading-day")
    public ResponseEntity<Map<String, Object>> getNextTradingDay(
            @RequestParam("date") String dateStr,
            @RequestParam(value = "max", required = false, defaultValue = "30") Integer max
    ) {
        LocalDate date = LocalDate.parse(dateStr);
        java.util.Map<String, Object> r = dbMarketService.findNextEffectiveDate(date, max != null ? max : 30);
        return ResponseEntity.ok(r);
    }
}



