package team.shdsesc.stocksimul.userprofile;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
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
                .where(userProfile.user.email.eq(email))
                .fetch();

        return Optional.ofNullable(result);
    }

    @Override
    public void updateUserProfileByProcessDate(LocalDateTime date, Long userProfileId){
        QUserProfileEntity userProfile = QUserProfileEntity.userProfileEntity;

        queryFactory.update(userProfile)
                .set(userProfile.processDate, date)
                .where(userProfile.usersProfileId.eq(userProfileId))
                .execute();
    }
}