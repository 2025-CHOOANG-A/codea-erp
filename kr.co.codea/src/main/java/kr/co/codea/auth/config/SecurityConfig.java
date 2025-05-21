package kr.co.codea.auth.config;

import kr.co.codea.auth.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    /**
     * 비밀번호 암호화에 사용할 BCryptPasswordEncoder 빈 등록
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 사용자 인증을 위한 AuthenticationManager 빈 등록
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http
                .getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsServiceImpl)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    /**
     * JWT 인증 필터 빈 등록
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider);
    }

    /**
     * 정적 리소스 및 인증 대상에서 제외할 경로 정의
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()) // 정적 리소스(css, js, 이미지 등)
                .requestMatchers(
                        new AntPathRequestMatcher("/.well-known/**"), // Let's Encrypt 인증 등에서 사용
                        new AntPathRequestMatcher("/error")           // Spring Boot 기본 에러 페이지
                );
    }

    /**
     * Spring Security 필터 체인 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // JWT 사용 시 CSRF 비활성화
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 상태 없음
            .authenticationManager(authenticationManager)
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class) // JWT 필터 등록
            .authorizeHttpRequests(authz -> authz
                    .requestMatchers(
                        "/",
                        "/login",
                        "/auth/login",
                        "/auth/reissue",
                        "/favicon.ico",
                        "/.well-known/**",
                        "/error"
                    ).permitAll()
                    .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**"
                    ).permitAll()
                    .requestMatchers("/index").authenticated()
                    .requestMatchers("/notice/**").hasRole("ADMIN") // 공지사항 하위 모든 경로는 ADMIN 역할만 접근 가능
                    .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )
                .formLogin(formLogin -> formLogin
                    .loginPage("/login")
                    .permitAll()
                );
        return http.build();
    }
}
