package team.shdsesc.stocksimul.security;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@Log4j2
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Autowired
    private ApiUserDetailsService apiUserDetailsService;

    @Autowired
    private JWTUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("security config...............");

        http.authorizeHttpRequests(auth -> auth
                // .requestMatchers("/boards/register").hasAnyRole("BASIC","MANAGER","ADMIN")
                .anyRequest().permitAll());
        http.csrf(csrf -> csrf.disable()); // CSRF 토큰 미사용 설정

        // CORS 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http.formLogin(c -> {
            // 이전페이지가 없는 상태에서 로그인성공 후 이동되는 페이지
            // c.loginPage("/login");
        });
        // 로그아웃 설정 (post로만 접근 가능/토큰 필요)
        http.logout(logout -> {
        });

        // JWT 관련 설정
        // AuthenticationManager 설정
        AuthenticationManagerBuilder authenticationManagerBuilder = http
                .getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(apiUserDetailsService).passwordEncoder(passwordEncoder());

        // AuthenticationManager 객체 생성
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
        http.authenticationManager(authenticationManager);

        // 필터
        ApiLoginFilter apiLoginFilter = new ApiLoginFilter("/auth"); // 토큰발급URL (http://localhost:8080/auth)
        apiLoginFilter.setAuthenticationManager(authenticationManager);
        apiLoginFilter.setAuthenticationSuccessHandler(new APILoginSuccessHandler(jwtUtil));

        // 필터동작위치
        http.addFilterBefore(apiLoginFilter, UsernamePasswordAuthenticationFilter.class);

        // 토큰체크필터
        TokenCheckFilter tokenCheckFilter = new TokenCheckFilter(jwtUtil);
        http.addFilterBefore(tokenCheckFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOriginPattern("http://localhost:3000/"); // 모든 도메인 허용
        configuration.addAllowedOriginPattern("http://localhost:5173/"); // 모든 도메인 허용
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
