package team.shdsesc.stocksimul.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.shdsesc.stocksimul.market.entity.Stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    @Query("select s from Stock s where s.ticker = :ticker")
    Optional<Stock> findByTicker(@Param("ticker") String ticker);

    @Query("select s from Stock s order by s.ticker asc")
    List<Stock> findAllOrderByTicker();

    @Query("select distinct s.ticker from Stock s order by s.ticker asc")
    List<String> findDistinctTickers();
}



