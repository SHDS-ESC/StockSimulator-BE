//package team.shdsesc.stocksimul.auth;
//
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import team.shdsesc.stocksimul.auth.dto.ApiUserDTO;
//import team.shdsesc.stocksimul.auth.dto.MemberRole;
//import team.shdsesc.stocksimul.auth.dto.UserRegisterRequest;
//import team.shdsesc.stocksimul.auth.entity.ApiUser;
//import team.shdsesc.stocksimul.auth.repository.ApiUserRepository;
//import team.shdsesc.stocksimul.auth.service.ApiUserDetailsService;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//public class AuthTests {
//
//    @Autowired
//    ApiUserDetailsService apiUserDetailsService;
//
//    @Autowired
//    PasswordEncoder passwordEncoder;
//
//    @Autowired
//    ApiUserRepository apiUserRepository;
//    @Autowired
//    private UserDetailsService userDetailsService;
//
//    @BeforeEach
//    void setUp() {
//        // 매번 테스트 전에 유저 삭제 후 생성
//        apiUserRepository.deleteById("testuser");
//
//        ApiUser user = ApiUser.builder()
//                .userId("testuser")
//                .password(passwordEncoder.encode("password"))
//                .phoneNumber("phoneNumber")
//                .level(1)
//                .tickerList(List.of("AAPL", "GOOG", "TSLA"))
//                .build();
//
//        user.addMemberRole(MemberRole.USER);
//        apiUserRepository.save(user);
//    }
//
//
//    @Test
//    @DisplayName("회원 가입 여부 확인")
//    void register() {
//        String userId = "admin";
//        String password = "admin";
//        String level = "1";
//        List<String> tickerList = List.of("AAPL", "GOOG", "TSLA");
//        String phoneNumber = "010-0000-0000";
//
//        // 1. 서비스 메서드를 호출하여 사용자 객체를 저장하고 반환받습니다.
//        ApiUserDTO apiUser = apiUserDetailsService.saveApiUser(new UserRegisterRequest(userId, password, level, tickerList));
//
//        // 2. 반환된 객체가 null이 아닌지 확인합니다.
//        assertNotNull(apiUser, "반환된 ApiUser 객체는 null이 아니어야 합니다.");
//
//        // 3. 반환된 객체의 ID가 예상한 값과 일치하는지 확인합니다.
//        assertEquals(userId, apiUser.getSecretId(), "getSecretId 입력값과 일치해야 합니다.");
//
//        // 4. 비밀번호가 원본(인코딩되지 않은) 비밀번호와 다른지 확인합니다.
//        // 이는 인코딩이 정상적으로 이루어졌음을 보장합니다.
//        assertNotEquals(password, apiUser.getPassword(), "비밀번호는 인코딩된 값이어야 합니다.");
//
//        // 5. (선택 사항) PasswordEncoder를 사용하여 인코딩된 비밀번호가 원본 비밀번호와 일치하는지 확인합니다.
//        assertTrue(passwordEncoder.matches(password, apiUser.getPassword()), "인코딩된 비밀번호가 원본 비밀번호와 일치해야 합니다.");
//    }
//
//    @Test
//    @DisplayName("로그인 확인")
//    void login(){
//        ApiUserDTO user = (ApiUserDTO) apiUserDetailsService.loadUserByUsername("testuser");
//        System.out.println(user);
//    }
//
//}
