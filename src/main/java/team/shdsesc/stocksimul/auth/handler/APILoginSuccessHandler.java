package team.shdsesc.stocksimul.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import team.shdsesc.stocksimul.auth.dto.JwtToken;
import team.shdsesc.stocksimul.auth.util.JwtTokenProvider;

import java.io.IOException;
import java.util.Map;

@Log4j2
public class APILoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    public APILoginSuccessHandler(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("Login Success...."); // 로그인 성공하면
        // 토큰생성해서 서블릿으로 응답
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);
        // Access Token 유효기간 1시간
        String accessToken = jwtToken.getAccessToken();
        // Refresh Token 유효기간 1일
        String refreshToken = jwtToken.getRefreshToken();

        // secure, httpOnly 옵션을 통해 XSS 공격 방지
        // SameSite 옵션을 통해 CSRF 공격 방지
        //  - None : 도메인 검증 X 어디서든 사용 가능하나 secure 옵션 필수
        //  - Lax : 외부 링크도 접근 허용 하지만 get 요청만 OK
        //  - Strict : 같은 도메인에서만 쿠키 전송 가능
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .maxAge(7 * 24 * 60 * 60)
                .path("/")
                // 브라우저에서 쿠키에 접근할 수 없도록 제한
                .httpOnly(true)
                // https 환경에서만 쿠키 발동
                .secure(false)
                // 동일 사이트과 크로스 사이트에 모두 쿠키 전송이 가능
                .sameSite("Lax") // 로컬 개발 환경에서는 None 사용 금지
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        Map<String, String> keyMap = Map.of(
                "accessToken", accessToken,
                "code", "200");
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(keyMap);
        response.getWriter().print(json);
    }
}
