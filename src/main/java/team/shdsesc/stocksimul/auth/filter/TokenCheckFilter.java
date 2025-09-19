package team.shdsesc.stocksimul.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import team.shdsesc.stocksimul.auth.exception.AccessTokenException;
import team.shdsesc.stocksimul.auth.exception.TokenAuthenticationException;
import team.shdsesc.stocksimul.auth.util.JwtToken;
import team.shdsesc.stocksimul.auth.util.JwtTokenProvider;

import java.io.IOException;

@Log4j2
public class TokenCheckFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TokenCheckFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

                // Swagger 관련 경로는 토큰 체크 제외
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/swagger-ui") ||
            requestURI.startsWith("/v3/api-docs") ||
            requestURI.startsWith("/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        // test, api만 인증 토큰 필터 거침
        // 그 외에는 matcher에 적용하지 않은 경우 403
        // 🔹 토큰 검증에서 완전히 제외할 경로
        if (path.startsWith("/auth") || 
            path.startsWith("/api/user/register") ||
            path.startsWith("/user/register") ||
            path.startsWith("/api/db/") ||
            path.startsWith("/api/api/") ||
            path.startsWith("/api/market/") ||
            path.startsWith("/api/news/") ||
            path.startsWith("/api/stock/") ||
            path.startsWith("/api/tickers") ||
            path.startsWith("/api/candles") ||
            path.startsWith("/api/watchlist") ||
            path.startsWith("/dev")) {
            filterChain.doFilter(request, response);
            return; // 여기서 바로 빠져나감 → 토큰 체크 안 함
        }
        try {
            // 1. Access Token 검증
            validateAccessToken(request);
            filterChain.doFilter(request, response);
        } catch (AccessTokenException e) {
            // 2. Access Token 만료일 경우 Refresh Token 검증
            if (e.getToken_error() == AccessTokenException.TOKEN_ERROR.EXPIRED) {
                String refreshToken = getRefreshTokenFromCookies(request);
                log.info("=== 토큰 만료 처리 시작 ===");
                log.info("쿠키에서 가져온 refreshToken: {}", refreshToken != null ? "존재함" : "없음");

                if (refreshToken != null) {
                    // Refresh Token 검증 전 로그
                    log.info("Refresh Token 검증 시작...");
                    boolean isValidRefreshToken = jwtTokenProvider.validateRefreshToken(refreshToken);
                    log.info("Refresh Token 유효성: {}", isValidRefreshToken);

                    if (isValidRefreshToken) {
                        log.info("토큰 갱신 로직 실행");
                        String username = jwtTokenProvider.getUserNameFromToken(refreshToken);
                        log.info("사용자명: {}", username);

                        JwtToken newTokens = jwtTokenProvider.generateTokenWithRefreshToken(username);
                        log.info("새 토큰 생성 완료");

                        // 새 AccessToken 내려주기
                        response.setHeader("Authorization", "Bearer " + newTokens.getAccessToken());
                        response.setHeader("Access-Control-Expose-Headers", "Authorization");

                        // 새 토큰으로 SecurityContext 설정
                        Authentication authentication = jwtTokenProvider.getAuthentication(newTokens.getAccessToken());
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        filterChain.doFilter(request, response);
                    } else {
                        log.info("Refresh Token이 유효하지 않음 - 로그아웃 처리");
                        String userName = null;
                        try {
                            userName = jwtTokenProvider.getUserNameFromToken(refreshToken);
                            log.info("만료된 토큰에서 추출한 사용자명: {}", userName);
                        } catch (Exception ex) {
                            log.warn("만료된 토큰 처리 중 사용자 추출 오류: {}", ex.getMessage());
                        }

                        // Redis에서 Refresh Token 삭제 (userName이 있을 때만)
                        if (userName != null && !userName.isEmpty()) {
                            try {
                                log.info("Redis에서 Refresh Token 삭제 중...");
                                jwtTokenProvider.deleteRefreshToken(userName);
                                log.info("Redis에서 Refresh Token 삭제 완료");
                            } catch (Exception ex) {
                                log.warn("Redis Refresh Token 삭제 중 오류: {}", ex.getMessage());
                            }
                        }

                        // 쿠키에서 Refresh Token 제거
                        log.info("쿠키에서 Refresh Token 삭제 중...");
                        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                                .path("/")
                                .httpOnly(true)
                                .secure(false)
                                .maxAge(0)
                                .build();
                        response.setHeader("Set-Cookie", cookie.toString());
                        log.info("쿠키에서 Refresh Token 삭제 완료");

                        // 에러 코드 속성 설정 (403/401 핸들러에서 사용 가능)
                        request.setAttribute("ERROR_CODE", "REFRESH_TOKEN_INVALID");
                        request.setAttribute("ERROR_MESSAGE", "리프레시 토큰이 유효하지 않습니다. 다시 로그인해주세요.");

                        log.info("=== 로그아웃 처리 완료 - 401 예외 위임 ===");
                        throw new TokenAuthenticationException(
                                "REFRESH_TOKEN_INVALID",
                                "리프레시 토큰이 유효하지 않습니다. 다시 로그인해주세요.");
                    }
                } else {
                    log.info("쿠키에 Refresh Token이 없음");
                    request.setAttribute("ERROR_CODE", "REFRESH_TOKEN_MISSING");
                    request.setAttribute("ERROR_MESSAGE", "로그인이 필요합니다.");
                    throw new TokenAuthenticationException(
                            "REFRESH_TOKEN_MISSING",
                            "로그인이 필요합니다.");
                }
            } else {
                // 전역 EntryPoint로 위임
                throw new TokenAuthenticationException(
                        "TOKEN_ERROR",
                        "요청에 필요한 토큰이 없거나 짧습니다.");
            }
        } catch (Exception e) {
            log.error("토큰 검증 중 예상치 못한 오류 발생", e);
            throw new TokenAuthenticationException(
                    "AUTHENTICATION_ERROR",
                    "인증 처리 중 오류가 발생했습니다.");
        }
    }

    // Access Token 검증
    private void validateAccessToken(HttpServletRequest request) throws AccessTokenException {
        String headerStr = request.getHeader("Authorization");
        if (headerStr == null || headerStr.length() < 8) {
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.UNACCEPT);
        }

        String tokenType = headerStr.substring(0, 6);
        String tokenStr = headerStr.substring(7);

        if (!tokenType.equalsIgnoreCase("Bearer")) {
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BADTYPE);
        }

        try {
            jwtTokenProvider.validateToken(tokenStr);
            // 인증 정보 생성해서 SecurityContext에 저장
            Authentication authentication = jwtTokenProvider.getAuthentication(tokenStr);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.MALFORM);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.EXPIRED);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty", e);
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.EMPTY_CLAIMS);
        }
    }

    // 쿠키에서 Refresh Token 가져오기
    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("refreshToken")) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
