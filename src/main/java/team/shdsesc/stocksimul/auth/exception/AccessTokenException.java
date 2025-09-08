package team.shdsesc.stocksimul.auth.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
public class AccessTokenException extends RuntimeException {
    TOKEN_ERROR token_error;

    @Getter
    public enum TOKEN_ERROR {
        UNACCEPT(401, "Token is null or too short"),
        BADTYPE(401, "Token type Bearer"),
        MALFORM(403, "Malformed Token"),
        BADSIGN(403, "BadSignatured Token"),
        EXPIRED(403, "Expired Token"),
        EMPTY_CLAIMS(403, "Empty Claim Token"),
        UNSUPPORTED(403, "UnSupported Token"),
        SEQURITY(403, "Security Exception");

        private int status;
        private String msg;

        TOKEN_ERROR(int status, String msg) {
            this.status = status;
            this.msg = msg;
        }

    }

    public AccessTokenException(TOKEN_ERROR error) {
        super(error.name());
        this.token_error = error;
    }

    /**
     * @deprecated Use TokenCheckFilter.sendErrorResponse() instead for consistent error format
     */
    @Deprecated
    public void sendResponseError(HttpServletResponse response) {
        response.setStatus(token_error.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        try {
            ObjectMapper om = new ObjectMapper();

            // 기존 형식 유지 (하위 호환성을 위해)
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("errorCode", this.token_error.name());
            errorResponse.put("message", this.token_error.getMsg());
            errorResponse.put("timestamp", System.currentTimeMillis());

            String responseStr = om.writeValueAsString(errorResponse);
            response.getWriter().print(responseStr);
        } catch (IOException e) {
            throw new RuntimeException("Error writing response", e);
        }
    }

    /**
     * 개선된 에러 응답 전송 메서드
     */
    public void sendImprovedResponseError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 모든 토큰 오류를 401로 통일
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("errorCode", "TOKEN_ERROR");
        errorResponse.put("message", this.token_error.getMsg());
        errorResponse.put("tokenError", this.token_error.name());
        errorResponse.put("timestamp", System.currentTimeMillis());

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}
