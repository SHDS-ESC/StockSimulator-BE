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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
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

    /**
     * 스냅샷 페이지(정렬/필터/페이징) 계산을 서비스에서 수행하여 컨트롤러 단순화
     */
    public java.util.Map<String, Object> getSnapshotPage(LocalDate date, Integer page, Integer size, String sort, String symbolsCsv) {
        List<java.util.Map<String, Object>> rows = getSnapshotRows(date);

        // sort parsing: key,direction
        String sortKey = "changePercentValue"; // default numeric key
        boolean desc = true;
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String k = parts[0].trim();
            String d = parts.length > 1 ? parts[1].trim().toLowerCase() : "desc";
            switch (k) {
                case "name": sortKey = "name"; break;
                case "price": sortKey = "priceValue"; break;
                case "change": sortKey = "changeValue"; break;
                case "changePercent": sortKey = "changePercentValue"; break;
                case "volume": sortKey = "volumeValue"; break;
                default: break;
            }
            desc = !"asc".equals(d);
        }

        final String sortKeyFinal = sortKey;
        final boolean descFinal = desc;
        rows.sort((a, b) -> {
            Object va = a.get(sortKeyFinal);
            Object vb = b.get(sortKeyFinal);
            int cmp;
            if (va instanceof Number && vb instanceof Number) {
                cmp = Double.compare(((Number) va).doubleValue(), ((Number) vb).doubleValue());
            } else {
                cmp = String.valueOf(va).compareToIgnoreCase(String.valueOf(vb));
            }
            return descFinal ? -cmp : cmp;
        });

        // 선택 심볼 필터
        java.util.Set<String> pick = null;
        if (symbolsCsv != null && !symbolsCsv.isBlank()) {
            pick = java.util.Arrays.stream(symbolsCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(java.util.stream.Collectors.toSet());
            if (!pick.isEmpty()) {
                final java.util.Set<String> pickFinal = pick;
                rows = rows.stream().filter(r -> pickFinal.contains(String.valueOf(r.get("symbol")))).toList();
            }
        }

        int total = rows.size();
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 6 : size;
        int from = Math.min((p - 1) * s, Math.max(total - 1, 0));
        int to = Math.min(from + s, total);
        List<java.util.Map<String, Object>> pageRows = rows.subList(from, to);

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("status", "ok");
        resp.put("s", "ok");
        resp.put("total", total);
        resp.put("page", p);
        resp.put("size", s);
        resp.put("rows", pageRows);
        return resp;
    }

    /**
     * 휴장일 자동 스킵을 포함한 스냅샷 페이지 응답.
     * 요청된 날짜에 데이터가 없으면 앞으로 하루씩 이동하며 데이터가 있는 첫 날짜를 effectiveDate로 사용.
     * 최대 maxForwardDays까지만 탐색.
     */
    public java.util.Map<String, Object> getSnapshotPageWithSkip(LocalDate requested, Integer page, Integer size, String sort, String symbolsCsv, int maxForwardDays) {
        LocalDate effective = requested;
        int skipped = 0;
        List<java.util.Map<String, Object>> rows = getSnapshotRows(effective);
        while ((rows == null || rows.isEmpty()) && skipped < Math.max(0, maxForwardDays)) {
            effective = effective.plusDays(1);
            skipped++;
            rows = getSnapshotRows(effective);
        }

        // sort parsing: key,direction (동일 로직 재사용)
        String sortKey = "changePercentValue";
        boolean desc = true;
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String k = parts[0].trim();
            String d = parts.length > 1 ? parts[1].trim().toLowerCase() : "desc";
            switch (k) {
                case "name": sortKey = "name"; break;
                case "price": sortKey = "priceValue"; break;
                case "change": sortKey = "changeValue"; break;
                case "changePercent": sortKey = "changePercentValue"; break;
                case "volume": sortKey = "volumeValue"; break;
                default: break;
            }
            desc = !"asc".equals(d);
        }

        if (rows == null) rows = new java.util.ArrayList<>();
        final String sortKeyFinal = sortKey;
        final boolean descFinal = desc;
        rows.sort((a, b) -> {
            Object va = a.get(sortKeyFinal);
            Object vb = b.get(sortKeyFinal);
            int cmp;
            if (va instanceof Number && vb instanceof Number) {
                cmp = Double.compare(((Number) va).doubleValue(), ((Number) vb).doubleValue());
            } else {
                cmp = String.valueOf(va).compareToIgnoreCase(String.valueOf(vb));
            }
            return descFinal ? -cmp : cmp;
        });

        // 선택 심볼 필터
        if (symbolsCsv != null && !symbolsCsv.isBlank()) {
            final java.util.Set<String> pick = java.util.Arrays.stream(symbolsCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(java.util.stream.Collectors.toSet());
            if (!pick.isEmpty()) {
                final java.util.Set<String> pickFinal = pick;
                rows = rows.stream().filter(r -> pickFinal.contains(String.valueOf(r.get("symbol")))).toList();
            }
        }

        int total = rows.size();
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 6 : size;
        int from = Math.min((p - 1) * s, Math.max(total - 1, 0));
        int to = Math.min(from + s, total);
        List<java.util.Map<String, Object>> pageRows = rows.subList(from, to);

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("status", "ok");
        resp.put("s", "ok");
        resp.put("requestedDate", requested != null ? requested.toString() : null);
        resp.put("effectiveDate", effective != null ? effective.toString() : null);
        resp.put("skippedDays", skipped);
        resp.put("total", total);
        resp.put("page", p);
        resp.put("size", s);
        resp.put("rows", pageRows);
        return resp;
    }

    /**
     * 다음 유효 거래일 계산 (스냅샷 데이터가 존재하는 날짜).
     */
    public java.util.Map<String, Object> findNextEffectiveDate(LocalDate start, int maxForwardDays) {
        LocalDate effective = start;
        int skipped = 0;
        List<java.util.Map<String, Object>> rows = getSnapshotRows(effective);
        while ((rows == null || rows.isEmpty()) && skipped < Math.max(0, maxForwardDays)) {
            effective = effective.plusDays(1);
            skipped++;
            rows = getSnapshotRows(effective);
        }
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("requestedDate", start != null ? start.toString() : null);
        resp.put("effectiveDate", effective != null ? effective.toString() : null);
        resp.put("skippedDays", skipped);
        resp.put("hasData", rows != null && !rows.isEmpty());
        return resp;
    }

    // getSnapshotRowsPage 제거됨 (미사용)

    /**
     * 지정된 날짜에 대한 모든 종목의 스냅샷(해당일 종가와 전일 종가 기반)을 계산하여 반환합니다.
     * 반환 형식: List<Map> with keys: symbol, name, price, change, changePercent
     */
    public List<java.util.Map<String, Object>> getSnapshotRows(LocalDate date) {
        if (date == null) {
            return java.util.Collections.emptyList();
        }
        LocalDate prev = date.minusDays(1);
        LocalDateTime selStart = date.atStartOfDay();
        LocalDateTime selEnd = date.atTime(23, 59, 59);
        LocalDateTime prevStart = prev.atStartOfDay();
        LocalDateTime prevEnd = prev.atTime(23, 59, 59);
        // 대량 쿼리로 (stockId, date, close, volume) 한번에 로드
        // 빈 결과 방지: 기간에 데이터가 없으면 최근 range를 기준으로 계산
        List<Object[]> tuples = reportRepository.findClosesByRange(prevStart, selEnd);
        if (tuples == null || tuples.isEmpty()) {
            // 가장 최근 날짜 범위를 조회해 스냅샷 구성 (fallback)
            try {
                List<Stock> allStocks = stockRepository.findAllOrderByTicker();
                if (allStocks.isEmpty()) return java.util.Collections.emptyList();
                // 첫 종목 기준으로 최근 범위를 구하고 동일 범위를 사용 (간단한 폴백)
                Optional<LocalDateTime> lastOpt = reportRepository.findLastDate(allStocks.get(0).getStockId());
                if (lastOpt.isPresent()) {
                    LocalDateTime last = lastOpt.get();
                    LocalDateTime from = last.minusDays(7).withHour(0).withMinute(0).withSecond(0).withNano(0);
                    LocalDateTime to = last.withHour(23).withMinute(59).withSecond(59).withNano(0);
                    List<Object[]> backup = reportRepository.findClosesByRange(from, to);
                    if (backup != null) {
                        tuples = backup;
                    }
                }
            } catch (Exception ignored) {}
        }
        java.util.Map<Long, double[]> closeMap = new java.util.HashMap<>();
        if (tuples == null) tuples = java.util.Collections.emptyList();
        java.util.Map<Long, Long> volumeMap = new java.util.HashMap<>();
        for (Object[] t : tuples) {
            Long sid = (Long) t[0];
            LocalDateTime dt = (LocalDateTime) t[1];
            Double close = (Double) t[2];
            Long vol = (Long) t[3];
            double[] arr = closeMap.computeIfAbsent(sid, k -> new double[]{Double.NaN, Double.NaN});
            if (!dt.isAfter(prevEnd)) {
                // 전일 종가(마지막 값)
                arr[0] = close;
            } else if (!dt.isBefore(selStart)) {
                // 당일 종가(첫 값)
                if (Double.isNaN(arr[1])) arr[1] = close;
                // 당일 첫 레코드의 거래량 사용 (일봉 누적과 다를 수 있음)
                if (!volumeMap.containsKey(sid)) volumeMap.put(sid, vol);
            }
        }

        List<Stock> all = stockRepository.findAllOrderByTicker();
        List<java.util.Map<String, Object>> rows = new ArrayList<>();
        for (Stock s : all) {
            Long stockId = s.getStockId();
            if (stockId == null) continue;
            double[] arr = closeMap.get(stockId);
            if (arr == null) continue;
            double prevClose = arr[0];
            double selClose = arr[1];
            if (Double.isNaN(prevClose) || Double.isNaN(selClose)) continue;

            rows.add(buildSnapshotRow(s, prevClose, selClose, volumeMap.getOrDefault(stockId, 0L)));
        }
        return rows;
    }

    private static java.util.Map<String, Object> buildSnapshotRow(Stock stock, double prevClose, double selClose, Long volume) {
        double chg = selClose - prevClose;
        double pct = prevClose != 0.0 ? (chg / prevClose) * 100.0 : 0.0;
        java.util.Map<String, Object> row = new HashMap<>();
        row.put("symbol", stock.getTicker());
        row.put("name", stock.getName());
        // numeric values for server-side sort
        row.put("priceValue", selClose);
        row.put("changeValue", chg);
        row.put("changePercentValue", pct);
        row.put("volumeValue", volume == null ? 0L : volume);
        // formatted strings for display
        row.put("price", String.format("$%.2f", selClose));
        row.put("change", String.format("%s%.2f", chg >= 0 ? "+" : "", chg));
        row.put("changePercent", String.format("%s%.2f%%", pct >= 0 ? "+" : "", pct));
        row.put("volume", volume == null ? "0" : String.valueOf(volume));
        return row;
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


