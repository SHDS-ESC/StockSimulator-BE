package team.shdsesc.stocksimul.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.shdsesc.stocksimul.auth.entity.ApiUser;


public interface ApiUserRepository extends JpaRepository<ApiUser, String>, ApiUserRepositoryCustom {
}

