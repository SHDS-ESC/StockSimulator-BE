package team.shdsesc.stocksimul.userprofile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserProfileRepositoryCustom {
    Optional<List<UserProfileEntity>> findUserByUserEmail(String email);
    void updateUserProfileByProcessDate(LocalDateTime date, Long userProfileId);
}
