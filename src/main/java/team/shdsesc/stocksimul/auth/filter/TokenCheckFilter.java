package team.shdsesc.stocksimul.auth.filter;

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
import team.shdsesc.stocksimul.auth.dto.JwtToken;
import team.shdsesc.stocksimul.auth.exception.AccessTokenException;
import team.shdsesc.stocksimul.auth.util.JwtTokenProvider;

import java.io.IOException;

@Log4j2
public class TokenCheckFilter extends OncePerRequestFilter {

//    private final JWTUtil jwtUtil;

    private final JwtTokenProvider jwtTokenProvider;

    public TokenCheckFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        // test, api만 인증 토큰 필터 거침
        // 그 외에는 matcher에 적용하지 않은 경우 403
        if (!path.startsWith("/test") && !path.startsWith("/api") && !path.startsWith("/auth/logout")) { // /api가 아니면 통과
            filterChain.doFilter(request, response);
            return;
        }

        log.info("Authorization 헤더: {}", response.getHeader("Authorization"));
        log.info("Authorization 헤더: {}", request.getHeader("Authorization"));

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

                            // Redis에서 Refresh Token 삭제
                            log.info("Redis에서 Refresh Token 삭제 중...");
                            jwtTokenProvider.deleteRefreshToken(userName);
                            log.info("Redis에서 Refresh Token 삭제 완료");
                        } catch (Exception ex) {
                            log.warn("만료된 토큰 처리 중 오류: {}", ex.getMessage());
                        }
                        log.info("쿠키에서 Refresh Token 삭제 중...");
                        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                                .path("/")
                                .httpOnly(true)
                                .secure(false)
                                .maxAge(0)
                                .build();
                        response.setHeader("Set-Cookie", cookie.toString());
                        log.info("쿠키에서 Refresh Token 삭제 완료");
                        jwtTokenProvider.deleteRefreshToken(userName);
                        log.info("=== 로그아웃 처리 완료 - 401 응답 전송 ===");
                        // 401 응답
                        e.sendResponseError(response);
                    }
                } else {
                    log.info("쿠키에 Refresh Token이 없음");
                    e.sendResponseError(response);
                }
            }
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
