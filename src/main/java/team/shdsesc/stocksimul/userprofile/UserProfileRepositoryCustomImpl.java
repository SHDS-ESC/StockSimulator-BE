package team.shdsesc.stocksimul.userprofile;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserProfileRepositoryCustomImpl implements UserProfileRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<List<UserProfileEntity>> findUserByUserEmail(String email) {
        QUserProfileEntity userProfile = QUserProfileEntity.userProfileEntity;

        List<UserProfileEntity> result = queryFactory
                .selectFrom(userProfile)
                .leftJoin(userProfile.user).fetchJoin()
                .where(userProfile.user.usersEmail.eq(email))
                .fetch();

        return Optional.ofNullable(result);
    }

    @Override
    public void updateCurrentProfileState(Long id, String email) {
        QUserProfileEntity userProfile = QUserProfileEntity.userProfileEntity;

        queryFactory.update(userProfile)
                .set(userProfile.state,
                        new CaseBuilder()
                                .when(userProfile.userProfileId.eq(id))
                                .then(1)
                                .otherwise(0))
                .where(userProfile.user.usersEmail.eq(email))
                .execute();
    }
}