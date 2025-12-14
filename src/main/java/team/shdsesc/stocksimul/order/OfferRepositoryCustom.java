package team.shdsesc.stocksimul.order;

import team.shdsesc.stocksimul.holdings.HoldingsEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OfferRepositoryCustom{
    List<OfferEntity> findByTodayOfferHistory(Long usersProfileId, LocalDateTime offerDate);
    Long findByTodayOfferAdjustment(Long usersProfileId, LocalDateTime offerDate);
}
