package team.shdsesc.stocksimul.market.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.shdsesc.stocksimul.market.dto.WatchlistResponse;
import team.shdsesc.stocksimul.market.entity.Watchlist;
import team.shdsesc.stocksimul.market.entity.WatchlistId;
import team.shdsesc.stocksimul.market.repository.WatchlistRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;

    public WatchlistService(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    public WatchlistResponse getWatchlist(String user) {
        String uid = (user == null || user.isBlank()) ? "guest" : user.trim();
        List<String> tickers = watchlistRepository.findTickersByUser(uid);
        return new WatchlistResponse("ok", tickers);
    }

    @Transactional
    public void addWatch(String ticker, String user) {
        String uid = (user == null || user.isBlank()) ? "guest" : user.trim();
        String t = ticker.trim().toUpperCase();
        WatchlistId id = new WatchlistId(uid, t);
        if (!watchlistRepository.existsById(id)) {
            Watchlist w = new Watchlist();
            w.setId(id);
            w.setCreatedAt(LocalDateTime.ofEpochSecond(Instant.now().getEpochSecond(), 0, ZoneOffset.UTC));
            watchlistRepository.save(w);
        }
    }

    @Transactional
    public void removeWatch(String ticker, String user) {
        String uid = (user == null || user.isBlank()) ? "guest" : user.trim();
        String t = ticker.trim().toUpperCase();
        watchlistRepository.deleteByUserAndTicker(uid, t);
    }
}













