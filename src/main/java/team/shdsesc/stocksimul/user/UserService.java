package team.shdsesc.stocksimul.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.redis.dao.RedisDAO;
import team.shdsesc.stocksimul.userprofile.UserProfileRepository;

import java.util.Map;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisDAO redisDAO;

    // 이 곳에서 DB의 UserEntity Entity를 가져와 인코딩 된 시큐리티의 password와 비교 후 검증 (Config에서 검증하므로 건드릴 것 없음)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<UserEntity> result = userRepository.findUserWithRolesByUserId(email);
        log.info("username:{}", email);
        UserEntity userEntity = result.orElseThrow(() -> new UsernameNotFoundException("Wrong ClientId or ClientSecret"));
        log.info("UserEntity:{}", userEntity);
        UserDTO dto = toUsersDTO(userEntity);
        log.info("loadUserByUsername:{}", dto);
        return dto;
    }

    public UserDTO toUsersDTO(UserEntity userEntity) {
        return new UserDTO(
                userEntity.getRegDate(),
                userEntity.getModDate(),
                userEntity.getUsersId(),
                userEntity.getEmail(),
                userEntity.getLastProfileId(),
                userEntity.getPassword(),
                userEntity.getLevel(),
                userEntity.getUsersRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.toString()))
                        .toList()
        );
    }

    public ResponseEntity<UserDTO> registerUser(UserRequestDTO request) {
        UserEntity userEntity = UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .level(Integer.parseInt(request.getLevel()))
                .build();

        userEntity.addMemberRole(UserRole.USER);
        userRepository.save(userEntity);
        return ResponseEntity.ok()
                .body(toUsersDTO(userEntity));
    }

    public ResponseEntity<?> logoutUser(String username) {
        // Redis에서 Refresh Token 삭제
        deleteRefreshToken(username);
        log.info("로그아웃 호출");
        // Refresh Token 쿠키 삭제
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .httpOnly(true)
                .secure(false) // 개발용
                .maxAge(0)// 만료시킴
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(Map.of("message", "로그아웃 성공"));
    }

    // RefreshToken 삭제
    public void deleteRefreshToken(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        // 로그아웃 시 Redis에서 RefreshToken 삭제
        redisDAO.deleteValues(username);
    }
}
