    package team.shdsesc.stocksimul.auth.controller;

    import jakarta.servlet.http.HttpServletResponse;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.web.bind.annotation.*;
    import team.shdsesc.stocksimul.auth.dto.UserDTO;
    import team.shdsesc.stocksimul.auth.dto.UserRequestDTO;
    import team.shdsesc.stocksimul.auth.service.UserDetailService;
    import team.shdsesc.stocksimul.auth.util.JwtTokenProvider;

    import java.util.Map;

    @RestController
    @RequiredArgsConstructor
    @RequestMapping("/auth")
    public class UserController {
        private final UserDetailService userDetailService;
        private final JwtTokenProvider jwtTokenProvider;

        @PostMapping("/register")
        public ResponseEntity<UserDTO> register(@RequestBody UserRequestDTO request) {
            return userDetailService.registerUser(request);
        }

        @PostMapping("/logout")
        public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("message", "로그아웃 실패: 토큰 없음"));
            }

            String token = authHeader.substring(7);
            String username = jwtTokenProvider.getUserNameFromToken(token);
            return userDetailService.logoutUser(username);
        }

        @GetMapping("/me")
        public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails user) {
            return ResponseEntity.ok(user.getUsername());
        }
    }