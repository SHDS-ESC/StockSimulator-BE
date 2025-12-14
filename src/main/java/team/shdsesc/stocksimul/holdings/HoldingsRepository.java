package team.shdsesc.stocksimul.holdings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoldingsRepository extends JpaRepository<HoldingsEntity,Long>, HoldingsRepositoryCustom {
}
