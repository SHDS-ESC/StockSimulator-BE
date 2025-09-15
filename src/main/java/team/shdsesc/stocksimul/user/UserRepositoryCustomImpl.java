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
                .leftJoin(users.usersRoles).fetchJoin()
                .where(users.usersEmail.eq(email))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public void updateCurrentProfileUser(Long id, Long pid) {
        QUserEntity users = QUserEntity.userEntity;
        queryFactory.update(users)
                .set(users.lastProfileId, pid)
                .where(users.usersId.eq(id))
                .execute();
    }
}
