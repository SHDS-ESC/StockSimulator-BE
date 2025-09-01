package team.shdsesc.stocksimul.security;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiUserRepository extends JpaRepository<ApiUser, String> {
}
