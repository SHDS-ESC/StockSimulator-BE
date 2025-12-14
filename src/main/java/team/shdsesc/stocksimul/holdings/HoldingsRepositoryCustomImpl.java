package team.shdsesc.stocksimul.holdings;

import com.querydsl.core.QueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class HoldingsRepositoryCustomImpl implements HoldingsRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<HoldingsEntity> getHoldingsList(Long usersProfileId) {
        QHoldingsEntity h = QHoldingsEntity.holdingsEntity;
        return queryFactory
                .selectFrom(h)
                .leftJoin(h.stock).fetchJoin()
                .where(h.userProfile.usersProfileId.eq(usersProfileId))
                .fetch();
    }

    @Override
    public Double getHoldingsTotalPrice(Long usersProfileId) {
        QHoldingsEntity h = QHoldingsEntity.holdingsEntity;

        return queryFactory
                .select(h.totalPrice.sum().coalesce(0.0))
                .from(h)
                .where(h.userProfile.usersProfileId.eq(usersProfileId))
                .fetchOne();
    }

    @Override
    public Optional<HoldingsEntity> getHoldingsStockAmount(Long usersProfileId, Long stockId) {
        QHoldingsEntity holdingsEntity = QHoldingsEntity.holdingsEntity;

        HoldingsEntity holding = queryFactory
                .selectFrom(holdingsEntity)
                .leftJoin(holdingsEntity.stock).fetchJoin()
                .where(holdingsEntity.userProfile.usersProfileId.eq(usersProfileId)
                        .and(holdingsEntity.stock.stockId.eq(stockId)))
                .fetchOne();

        return Optional.ofNullable(holding);
    }

    @Override
    @Transactional
    public void updateHoldingStockAmount(Long usersProfileId, Long stockId, Long quantity, Double totalPrice) {
        QHoldingsEntity holdingsEntity = QHoldingsEntity.holdingsEntity;

        queryFactory.update(holdingsEntity)
                .set(holdingsEntity.quantity, quantity)
                .set(holdingsEntity.totalPrice, totalPrice)
                .where(holdingsEntity.userProfile.usersProfileId.eq(usersProfileId)
                        .and(holdingsEntity.stock.stockId.eq(stockId)))
                .execute();
    }
}
