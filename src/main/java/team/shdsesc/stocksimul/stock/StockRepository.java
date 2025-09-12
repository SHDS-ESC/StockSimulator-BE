package team.shdsesc.stocksimul.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface StockRepository extends JpaRepository<StockEntity, Long>,
        QuerydslPredicateExecutor<StockEntity> {

    public StockEntity findByTicker(String ticker);

}
