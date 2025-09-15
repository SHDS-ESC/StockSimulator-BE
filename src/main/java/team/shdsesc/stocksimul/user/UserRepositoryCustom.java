package team.shdsesc.stocksimul.user;

import java.util.Optional;

public interface UserRepositoryCustom {
    Optional<UserEntity> findUserWithRolesByUserId(String email);
}

