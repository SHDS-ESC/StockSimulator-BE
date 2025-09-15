package team.shdsesc.stocksimul.userprofile;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepositoryCustom {
    Optional<List<UserProfileEntity>> findUserByUserEmail(String email);
    void updateCurrentProfileState(Long id, String email);
}
