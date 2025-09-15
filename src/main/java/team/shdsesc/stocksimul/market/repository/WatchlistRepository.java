package team.shdsesc.stocksimul.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.shdsesc.stocksimul.market.entity.Watchlist;
import team.shdsesc.stocksimul.market.entity.WatchlistId;

import java.time.LocalDateTime;
import java.util.List;

public interface WatchlistRepository extends JpaRepository<Watchlist, WatchlistId> {

    @Query("select w.id.ticker from Watchlist w where w.id.userId = :userId order by w.id.ticker asc")
    List<String> findTickersByUser(@Param("userId") String userId);

    @Modifying
    @Query("delete from Watchlist w where w.id.userId = :userId and w.id.ticker = :ticker")
    void deleteByUserAndTicker(@Param("userId") String userId, @Param("ticker") String ticker);

    default Watchlist create(String userId, String ticker, LocalDateTime createdAt) {
        Watchlist w = new Watchlist();
        w.setId(new WatchlistId(userId, ticker));
        w.setCreatedAt(createdAt);
        return save(w);
    }
}













