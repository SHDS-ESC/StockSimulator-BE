package team.shdsesc.stocksimul.auth.repository;

import team.shdsesc.stocksimul.auth.entity.Users;

import java.util.Optional;

public interface UserRepositoryCustom {
    Optional<Users> findUserWithRolesByUserId(String email);
}

