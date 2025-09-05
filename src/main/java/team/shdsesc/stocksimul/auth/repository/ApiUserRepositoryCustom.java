package team.shdsesc.stocksimul.auth.repository;

import team.shdsesc.stocksimul.auth.entity.ApiUser;

import java.util.Optional;

public interface ApiUserRepositoryCustom {
    Optional<ApiUser> findApiUserWithRolesByUserId(String userId);
}

