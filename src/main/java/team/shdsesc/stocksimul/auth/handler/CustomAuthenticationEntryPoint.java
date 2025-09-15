package team.shdsesc.stocksimul.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import team.shdsesc.stocksimul.auth.exception.TokenAuthenticationException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.info("401 발생 - {}", authException.getMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        // CORS 헤더 (TokenCheckFilter의 sendErrorResponse와 동일 포맷)
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Expose-Headers", "Authorization, Set-Cookie");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);

        // 1) 커스텀 AuthenticationException이면 우선 반영
        if (authException instanceof TokenAuthenticationException tae) {
            errorResponse.put("errorCode", tae.getErrorCode());
            errorResponse.put("message", tae.getErrorMessage());
        } else {
            // 2) request attribute가 있으면 사용
            String errorCode = (String) request.getAttribute("ERROR_CODE");
            String message = (String) request.getAttribute("ERROR_MESSAGE");
            if (errorCode != null) {
                errorResponse.put("errorCode", errorCode);
                errorResponse.put("message", message != null ? message : "인증이 필요합니다.");
            } else {
                // 3) 기본값
                errorResponse.put("errorCode", "UNAUTHORIZED");
                errorResponse.put("message", "인증이 필요합니다.");
            }
        }

        errorResponse.put("timestamp", System.currentTimeMillis());

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
} 