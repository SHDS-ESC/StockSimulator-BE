package team.shdsesc.stocksimul.order;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Log4j2
public class OfferRepositoryCustomImpl implements OfferRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<OfferEntity> findByTodayOfferHistory(Long usersProfileId, LocalDateTime offerDate) {
        QOfferEntity qOfferEntity = QOfferEntity.offerEntity;
        return queryFactory.selectFrom(qOfferEntity)
                .leftJoin(qOfferEntity.stock)
                .fetchJoin()
                .where(qOfferEntity.userProfile.usersProfileId
                        .eq(usersProfileId)
                        .and(qOfferEntity.offerDate.eq(offerDate)))
                .fetch();
    }

    @Override
    public Long findByTodayOfferAdjustment(Long usersProfileId, LocalDateTime offerDate) {
        QOfferEntity qOfferEntity = QOfferEntity.offerEntity;

        return queryFactory
                .select(
                        new CaseBuilder()
                                .when(qOfferEntity.type.eq(OfferType.valueOf("BUY")))
                                .then(qOfferEntity.quantity.multiply(-1))
                                .when(qOfferEntity.type.eq(OfferType.valueOf("SELL")))
                                .then(qOfferEntity.quantity)
                                .otherwise(0L)
                                .sum()
                )
                .from(qOfferEntity)
                .where(qOfferEntity.userProfile.usersProfileId.eq(usersProfileId)
                        .and(qOfferEntity.offerDate.eq(offerDate)))
                .fetchOne();
    }
}
