package team.shdsesc.stocksimul.auth.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import team.shdsesc.stocksimul.auth.entity.QUsers;
import team.shdsesc.stocksimul.auth.entity.Users;

import java.util.Optional;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Users> findUserWithRolesByUserId(String email) {
        QUsers users = QUsers.users ;

        Users result = queryFactory
                .selectFrom(users)
                .leftJoin(users.roleSet).fetchJoin()
                .where(users.email.eq(email))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
