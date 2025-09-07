package team.shdsesc.stocksimul.auth.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.http.MediaType;

import java.util.Date;
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

    public void sendResponseError(HttpServletResponse response) {
        response.setStatus(token_error.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        try {
            ObjectMapper om = new ObjectMapper();

            String responseStr = om.writeValueAsString(Map.of("msg", token_error.getMsg(), "time", new Date()));
            response.getWriter().print(responseStr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
