package team.shdsesc.stocksimul.holdings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface HoldingsRepositoryCustom{
    List<HoldingsEntity> getHoldingsList(Long usersProfileId);
    Double getHoldingsTotalPrice(Long usersProfileId);
    Optional<HoldingsEntity> getHoldingsStockAmount(Long usersProfileId, Long StockId);
    void updateHoldingStockAmount(Long usersProfileId, Long StockId, Long quantity, Double totalPrice);
}
