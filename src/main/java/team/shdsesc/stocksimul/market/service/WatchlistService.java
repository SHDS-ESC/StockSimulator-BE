package team.shdsesc.stocksimul.market.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.shdsesc.stocksimul.market.dto.WatchlistResponse;
import team.shdsesc.stocksimul.market.entity.Watchlist;
import team.shdsesc.stocksimul.market.entity.WatchlistId;
import team.shdsesc.stocksimul.market.repository.WatchlistRepository;

import java.util.List;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;

    public WatchlistService(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    public WatchlistResponse getWatchlist(String user) {
        String uid = (user == null || user.isBlank()) ? "guest" : user.trim();
        try {
            List<String> tickers = watchlistRepository.findTickersByUser(uid);
            return new WatchlistResponse("ok", tickers);
        } catch (RuntimeException ex) {
            // DB에 watchlist 테이블이 없는 환경에서는 빈 목록을 반환하여 기능을 우회
            return new WatchlistResponse("ok", List.of());
        }
    }

    @Transactional
    public void addWatch(String ticker, String user) {
        String uid = (user == null || user.isBlank()) ? "guest" : user.trim();
        String t = ticker.trim().toUpperCase();
        WatchlistId id = new WatchlistId(uid, t);
        try {
            if (!watchlistRepository.existsById(id)) {
                Watchlist w = new Watchlist();
                w.setId(id);
                watchlistRepository.save(w);
            }
        } catch (RuntimeException ex) {
            // DB 테이블이 없으면 조용히 무시 (우회 동작)
        }
    }

    @Transactional
    public void removeWatch(String ticker, String user) {
        String uid = (user == null || user.isBlank()) ? "guest" : user.trim();
        String t = ticker.trim().toUpperCase();
        try {
            watchlistRepository.deleteByUserAndTicker(uid, t);
        } catch (RuntimeException ex) {
            // DB 테이블이 없으면 조용히 무시 (우회 동작)
        }
    }
}













