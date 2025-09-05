package team.shdsesc.stocksimul.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import team.shdsesc.stocksimul.auth.util.JWTUtil;

import java.io.IOException;
import java.util.Map;

@Log4j2
public class APILoginSuccessHandler implements AuthenticationSuccessHandler {

    private JWTUtil jwtUtil;

    public APILoginSuccessHandler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("Login Success...."); // 로그인 성공하면
        // 토큰생성해서 서블릿으로 응답
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        log.info(authentication.getName()); // username
        Map<String, Object> claim = Map.of("userId", authentication.getName());
        // Access Token 유효기간 1일
        String accessToken = jwtUtil.generationToken(claim, 1);

        Map<String, String> keyMap = Map.of("accessToken", accessToken);
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(keyMap);
        response.getWriter().print(json);
    }
}
