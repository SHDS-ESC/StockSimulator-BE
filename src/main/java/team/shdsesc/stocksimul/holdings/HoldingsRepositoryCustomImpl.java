package team.shdsesc.stocksimul.holdings;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class HoldingsRepositoryCustomImpl implements HoldingsRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<List<HoldingsEntity>> getHoldingsList(Long usersProfileId) {

        QHoldingsEntity holdingsEntity = QHoldingsEntity.holdingsEntity;

        List<HoldingsEntity> result =  queryFactory
                .selectFrom(holdingsEntity)
                .leftJoin(holdingsEntity.stock)
                .fetchJoin()
                .where(holdingsEntity.userProfile.usersProfileId.eq(usersProfileId))
                .fetch();
        return Optional.ofNullable(result);
    }
}
