package team.shdsesc.stocksimul.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class ApiLoginFilter extends AbstractAuthenticationProcessingFilter {
    protected ApiLoginFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        log.info("ApiLoginFilter 실행");
        Map<String, Object> map = new HashMap<>();
        try (Reader reader = new InputStreamReader(request.getInputStream())) {
            ObjectMapper om = new ObjectMapper();
            map = om.readValue(reader, Map.class);
            log.info(map);

        } catch (Exception e) {
            log.info(e.getMessage());
        }

        // 인증처리
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(map.get("client_id"), map.get("client_secret"));
        return getAuthenticationManager().authenticate(authenticationToken);
    }


}
