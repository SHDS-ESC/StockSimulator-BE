package team.shdsesc.stocksimul.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.shdsesc.stocksimul.auth.entity.Users;


public interface UserRepository extends JpaRepository<Users, String>, UserRepositoryCustom {
}

