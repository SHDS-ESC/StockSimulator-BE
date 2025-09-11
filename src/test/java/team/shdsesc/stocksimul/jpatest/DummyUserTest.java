package team.shdsesc.stocksimul.jpatest;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import team.shdsesc.stocksimul.auth.dto.MemberRole;
import team.shdsesc.stocksimul.auth.entity.ApiUser;
import team.shdsesc.stocksimul.auth.repository.ApiUserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DummyUserTest {

    @Autowired
    private ApiUserRepository apiUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // 매번 테스트 전에 유저 삭제 후 생성
        apiUserRepository.deleteById("testuser");

        ApiUser user = ApiUser.builder()
                .userId("testuser")
                .password(passwordEncoder.encode("password"))
                .phoneNumber("phoneNumber")
                .level(1)
                .tickerList(List.of("AAPL", "GOOG", "TSLA"))
                .build();

        user.addMemberRole(MemberRole.USER);
        apiUserRepository.save(user);
    }

    @Test
    @DisplayName("더미 유저 여부 확인")
    void testDummyUserCreated() {
        ApiUser user = apiUserRepository.findById("testuser").orElseThrow();
        assertThat(user.getUserId()).isEqualTo("testuser");
        assertThat(passwordEncoder.matches("password", user.getPassword())).isTrue();

        assertThat(user.getRoleSet()).contains(MemberRole.USER);

        assertThat(user.getTickerList()).containsExactly("AAPL", "GOOG", "TSLA");
    }
}
