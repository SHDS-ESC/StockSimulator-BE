package team.shdsesc.stocksimul.auth.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import team.shdsesc.stocksimul.auth.filter.ApiLoginFilter;
import team.shdsesc.stocksimul.auth.filter.TokenCheckFilter;
import team.shdsesc.stocksimul.auth.handler.APILoginFailureHandler;
import team.shdsesc.stocksimul.auth.handler.APILoginSuccessHandler;
import team.shdsesc.stocksimul.auth.service.UserDetailService;
import team.shdsesc.stocksimul.auth.util.JwtTokenProvider;

@Configuration
@EnableWebSecurity
@Log4j2
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final UserDetailService userDetailService;
    private final PasswordEncoder passwordEncoder;
//    private final JWTUtil jwtUtil;

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    SecurityConfig(UserDetailService userDetailService, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userDetailService = userDetailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("security config...............");

        http.authorizeHttpRequests(auth -> auth
                // .requestMatchers("/boards/register").hasAnyRole("BASIC","MANAGER","ADMIN")
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated());
        http.csrf(AbstractHttpConfigurer::disable); // CSRF 토큰 미사용 설정

        // CORS 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 자체 로그인 설정으로 폼 로그인은 비활성화
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        // JWT 관련 설정
        // AuthenticationManager 설정
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailService).passwordEncoder(passwordEncoder);

        // AuthenticationManager 객체 생성
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
        http.authenticationManager(authenticationManager);

        // 필터
        // 토큰발급URL (http://localhost:8080/auth)
        ApiLoginFilter apiLoginFilter = new ApiLoginFilter("/auth");

        // AuthenticationManager 세팅
        apiLoginFilter.setAuthenticationManager(authenticationManager);

        // 성공 시
        apiLoginFilter.setAuthenticationSuccessHandler(new APILoginSuccessHandler(jwtTokenProvider));
        // 실패 시
        apiLoginFilter.setAuthenticationFailureHandler(new APILoginFailureHandler());

        // 필터동작위치
        http.addFilterBefore(apiLoginFilter, UsernamePasswordAuthenticationFilter.class);

        // 토큰체크필터
        TokenCheckFilter tokenCheckFilter = new TokenCheckFilter(jwtTokenProvider);
        http.addFilterBefore(tokenCheckFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
//        configuration.addAllowedOriginPattern("http://localhost:3000/"); // 모든 도메인 허용
        configuration.addAllowedOriginPattern("http://localhost:5173");
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
