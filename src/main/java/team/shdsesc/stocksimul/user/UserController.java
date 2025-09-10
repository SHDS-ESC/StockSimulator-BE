package team.shdsesc.stocksimul.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.shdsesc.stocksimul.auth.util.JwtTokenProvider;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserRequestDTO request) {
        return userService.registerUser(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("message", "failed : Token is missing or invalid"));
        }

        String token = authHeader.substring(7);
        String username = jwtTokenProvider.getUserNameFromToken(token);
        return userService.logoutUser(username);
    }
}