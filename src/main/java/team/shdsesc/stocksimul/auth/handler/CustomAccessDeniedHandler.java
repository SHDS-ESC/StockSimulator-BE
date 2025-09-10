package team.shdsesc.stocksimul.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        log.info("403 발생 - {}", accessDeniedException.getMessage());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");

        // CORS 헤더 (TokenCheckFilter의 sendErrorResponse와 동일 포맷)
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Expose-Headers", "Authorization, Set-Cookie");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);

        // TokenCheckFilter에서 상황별로 심어준 경우 우선 적용
        String errorCode = (String) request.getAttribute("ERROR_CODE");
        String message = (String) request.getAttribute("ERROR_MESSAGE");

        if (errorCode != null) {
            errorResponse.put("errorCode", errorCode);
            errorResponse.put("message", message != null ? message : "접근 권한이 없습니다.");
        } else {
            errorResponse.put("errorCode", "ACCESS_DENIED");
            errorResponse.put("message", "접근 권한이 없습니다.");
        }

        errorResponse.put("timestamp", System.currentTimeMillis());

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
} 