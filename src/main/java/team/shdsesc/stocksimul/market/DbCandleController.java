package team.shdsesc.stocksimul.market;

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
public class DbCandleController {

    private final DbMarketService dbMarketService;

    public DbCandleController(DbMarketService dbMarketService) {
        this.dbMarketService = dbMarketService;
    }

    /**
     * DB 테이블 'stock'에서 캔들 데이터를 조회하여 lightweight-charts 호환 포맷으로 반환
     * - 테이블 스키마: date(DATETIME), ticker(TEXT), open/high/low/close(DOUBLE), ...
     * - 요청 파라미터: ticker(or symbol), from, to (epoch seconds). 없으면 최근 7일로 기본값.
     * - 응답: { s: "ok"|"no_data", t: number[], o: number[], h: number[], l: number[], c: number[] }
     */
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
                "s", body.getS(),
                "t", body.getT(),
                "d", body.getD(),
                "o", body.getO(),
                "h", body.getH(),
                "l", body.getL(),
                "c", body.getC(),
                "v", body.getV()
        ));
    }

    /**
     * 티커의 가장 최근 일자를 기준으로 days일 범위를 계산해 반환
     * 응답: { s: "ok"|"no_data", last: number(epochSec), from: number, to: number }
     */
    @GetMapping("/last-range")
    public ResponseEntity<Map<String, Object>> getLastRange(
            @RequestParam("ticker") String ticker,
            @RequestParam(value = "days", required = false) Integer days
    ) {
        RangeResponse r = dbMarketService.getLastRange(ticker, days);
        if (!"ok".equals(r.getS())) {
            return ResponseEntity.ok(Map.of("s", "no_data"));
        }
        return ResponseEntity.ok(Map.of("s", r.getS(), "last", r.getLast(), "from", r.getFrom(), "to", r.getTo()));
    }

    /**
     * stock 테이블에서 존재하는 티커 목록을 반환
     * 응답: { s: "ok", tickers: string[] }
     */
    @GetMapping("/tickers")
    public ResponseEntity<Map<String, Object>> getTickers() {
        TickersResponse r = dbMarketService.getTickers();
        return ResponseEntity.ok(Map.of("s", r.getS(), "tickers", r.getTickers()));
    }

    /**
     * stock에 존재하는 티커와 snp_history의 회사명을 결합해 반환
     * 응답: { s: "ok", symbols: [{ ticker: string, name: string }] }
     */
    @GetMapping("/symbols")
    public ResponseEntity<Map<String, Object>> getSymbols() {
        SymbolsResponse r = dbMarketService.getSymbols();
        return ResponseEntity.ok(Map.of("s", r.getS(), "symbols", r.getSymbols()));
    }
}


