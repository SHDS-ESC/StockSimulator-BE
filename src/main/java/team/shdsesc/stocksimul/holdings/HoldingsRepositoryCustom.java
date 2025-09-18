package team.shdsesc.stocksimul.holdings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface HoldingsRepositoryCustom{
    Optional<List<HoldingsEntity>> getHoldingsList(Long usersProfileId);
}
