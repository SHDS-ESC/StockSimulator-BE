package team.shdsesc.stocksimul.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.util.Map;

@Log4j2
public class APILoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        log.info("Login Failed....");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json; charset=UTF-8"); // UTF-8 지정

        Map<String, String> errorResponse = Map.of(
                "code", "401",
                "error", "Authentication failed",
                "message", exception.getMessage()
        );

        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(errorResponse);

        response.getWriter().print(json);
    }
}
