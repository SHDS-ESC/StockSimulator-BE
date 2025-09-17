package team.shdsesc.stocksimul.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.market.dto.CandleResponse;
import team.shdsesc.stocksimul.market.dto.RangeResponse;
import team.shdsesc.stocksimul.market.dto.SymbolDTO;
import team.shdsesc.stocksimul.market.dto.SymbolsResponse;
import team.shdsesc.stocksimul.market.dto.TickersResponse;
import team.shdsesc.stocksimul.market.entity.Report;
import team.shdsesc.stocksimul.market.entity.Stock;
import team.shdsesc.stocksimul.market.repository.ReportRepository;
import team.shdsesc.stocksimul.market.repository.MarketStockRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DbMarketService {

    private final MarketStockRepository stockRepository;
    private final ReportRepository reportRepository;

    public CandleResponse getCandles(String tickerParam, String symbolParam, Long fromEpochSec, Long toEpochSec, Integer days) {
        String ticker = resolveTicker(tickerParam, symbolParam);
        if (ticker == null || ticker.isBlank()) {
            return CandleResponse.noData();
        }

        // 티커로 stock_id 찾기
        Optional<Stock> stockOpt = stockRepository.findByTicker(ticker);
        if (stockOpt.isEmpty()) {
            return CandleResponse.noData();
        }
        Long stockId = stockOpt.get().getStockId();

        LocalDateTime from = null;
        LocalDateTime to = null;
        if (fromEpochSec != null) {
            from = LocalDateTime.ofEpochSecond(fromEpochSec, 0, ZoneOffset.UTC);
        }
        if (toEpochSec != null) {
            to = LocalDateTime.ofEpochSecond(toEpochSec, 0, ZoneOffset.UTC);
        }

        if (from == null && to == null) {
            // 기본: 최근 days일 (기본 7일)
            int fallbackDays = (days == null || days <= 0) ? 7 : days;
            Optional<LocalDateTime> lastOpt = reportRepository.findLastDate(stockId);
            if (lastOpt.isEmpty()) {
                return CandleResponse.noData();
            }
            LocalDateTime last = lastOpt.get();
            from = last.minusDays(fallbackDays - 1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            to = last.withHour(23).withMinute(59).withSecond(59).withNano(0);
        }

        List<Report> rows = reportRepository.findByStockIdAndRange(stockId, from, to);
        if (rows.isEmpty()) {
            return CandleResponse.noData();
        }

        rows.sort(Comparator.comparing(Report::getDate));

        List<Long> timestamps = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        List<Double> opens = new ArrayList<>();
        List<Double> highs = new ArrayList<>();
        List<Double> lows = new ArrayList<>();
        List<Double> closes = new ArrayList<>();
        List<Double> volumes = new ArrayList<>();

        for (Report r : rows) {
            LocalDateTime dt = r.getDate();
            long epochSec = dt.toEpochSecond(ZoneOffset.UTC);
            timestamps.add(epochSec);
            dates.add(dt.toLocalDate().toString());
            opens.add(nullSafe(r.getOpen()));
            highs.add(nullSafe(r.getHigh()));
            lows.add(nullSafe(r.getLow()));
            closes.add(nullSafe(r.getClose()));
            volumes.add(nullSafeDouble(r.getVolume()));
        }

        return new CandleResponse("ok", timestamps, dates, opens, highs, lows, closes, volumes);
    }

    public RangeResponse getLastRange(String ticker, Integer days) {
        if (ticker == null || ticker.isBlank()) {
            return RangeResponse.noData();
        }
        String t = ticker.trim().toUpperCase();
        
        // 티커로 stock_id 찾기
        Optional<Stock> stockOpt = stockRepository.findByTicker(t);
        if (stockOpt.isEmpty()) {
            return RangeResponse.noData();
        }
        Long stockId = stockOpt.get().getStockId();
        
        Optional<LocalDateTime> lastOpt = reportRepository.findLastDate(stockId);
        if (lastOpt.isEmpty()) {
            return RangeResponse.noData();
        }
        int d = (days == null || days <= 0) ? 7 : days;
        LocalDateTime last = lastOpt.get();
        LocalDateTime from = last.minusDays(d - 1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        long lastEpoch = last.toEpochSecond(ZoneOffset.UTC);
        long fromEpoch = from.toEpochSecond(ZoneOffset.UTC);
        long toEpoch = last.withHour(23).withMinute(59).withSecond(59).withNano(0).toEpochSecond(ZoneOffset.UTC);
        return new RangeResponse("ok", lastEpoch, fromEpoch, toEpoch);
    }

    public TickersResponse getTickers() {
        List<String> tickers = stockRepository.findDistinctTickers();
        return new TickersResponse("ok", tickers);
    }

    public SymbolsResponse getSymbols() {
        List<Stock> stocks = stockRepository.findAllOrderByTicker();
        List<SymbolDTO> symbols = stocks.stream()
                .map(s -> {
                    SymbolDTO dto = new SymbolDTO();
                    dto.setTicker(s.getTicker());
                    dto.setName(s.getName());
                    dto.setSector(s.getSector());
                    dto.setIndustry(s.getIndustry());
                    return dto;
                })
                .collect(Collectors.toList());
        return new SymbolsResponse("ok", symbols);
    }

    private static String resolveTicker(String tickerParam, String symbolParam) {
        String ticker = null;
        if (tickerParam != null && !tickerParam.isBlank()) {
            ticker = tickerParam.trim().toUpperCase();
        } else if (symbolParam != null && !symbolParam.isBlank()) {
            ticker = symbolParam.trim().toUpperCase();
        }
        return ticker;
    }

    private static Double nullSafe(Double v) {
        return v == null ? 0.0 : v;
    }

    private static Double nullSafeDouble(Long v) {
        return v == null ? 0.0 : v.doubleValue();
    }
}


