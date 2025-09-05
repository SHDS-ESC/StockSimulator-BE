package team.shdsesc.stocksimul.auth.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import team.shdsesc.stocksimul.auth.entity.ApiUser;
import team.shdsesc.stocksimul.auth.entity.QApiUser;

import java.util.Optional;

@RequiredArgsConstructor
public class ApiUserRepositoryImpl implements ApiUserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ApiUser> findApiUserWithRolesByUserId(String userId) {
        QApiUser apiUser = QApiUser.apiUser;

        ApiUser result = queryFactory
                .selectFrom(apiUser)
                .leftJoin(apiUser.roleSet).fetchJoin()
                .where(apiUser.userId.eq(userId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
