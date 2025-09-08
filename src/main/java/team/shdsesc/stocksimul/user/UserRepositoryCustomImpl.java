package team.shdsesc.stocksimul.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<UserEntity> findUserWithRolesByUserId(String email) {
        QUserEntity users = QUserEntity.userEntity;

        UserEntity result = queryFactory
                .selectFrom(users)
                .leftJoin(users.roleSet).fetchJoin()
                .where(users.email.eq(email))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
