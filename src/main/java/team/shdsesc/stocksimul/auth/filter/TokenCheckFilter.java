package team.shdsesc.stocksimul.auth.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.filter.OncePerRequestFilter;
import team.shdsesc.stocksimul.auth.exception.AccessTokenException;
import team.shdsesc.stocksimul.auth.util.JWTUtil;

import java.io.IOException;
import java.security.SignatureException;
import java.util.Map;

@Log4j2
public class TokenCheckFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    public TokenCheckFilter(JWTUtil jwUtil) {
        this.jwtUtil = jwUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/api")) { // /api 주소가 아니면(일반접속이면) 통과
            filterChain.doFilter(request, response);
            return;
        }
        log.info("Token Check Filter............");
        log.info("JWUtil: "+jwtUtil.toString());

        // AccessToken 검증
        try {
            validateAccessToken(request);
            filterChain.doFilter(request, response);
        } catch (AccessTokenException e) {
            e.sendResponseError(response);
        }

    }

    // AccessToken 검증
    private void validateAccessToken(HttpServletRequest request) throws AccessTokenException {
        String headerStr = request.getHeader("Authorization");
        if (headerStr == null || headerStr.length() < 8) {
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.UNACCEPT);
        }
        // Bearer 생략
        String tokenType = headerStr.substring(0,6);
        String tokenStr = headerStr.substring(7);

        if (!tokenType.equalsIgnoreCase("Bearer")) {
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BADTYPE);
        }

        try {
            Map<String, Object> value = jwtUtil.validateToken(tokenStr);
        } catch (MalformedJwtException e) {
            log.info("MalformedJwtException................");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.MALFORM);
        } catch (SignatureException e) {
            log.info("SignaturedException..................");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BADSIGN);
        } catch(ExpiredJwtException e) {
            log.info("ExpiredJwtException..................");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.EXPIRED);
        } catch (Exception e) {
            log.info("Exception......................");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.UNACCEPT);
        }
    }

}
