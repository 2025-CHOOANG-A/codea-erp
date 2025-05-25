package kr.co.codea.auth.config;

import kr.co.codea.auth.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    /**
     * 비밀번호 암호화에 사용할 BCryptPasswordEncoder 등록
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager 등록
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsServiceImpl).passwordEncoder(passwordEncoder());
        return builder.build();
    }

    /**
     * JwtAuthenticationFilter 빈 등록
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider);
    }

    /**
     * WebSecurityCustomizer 설정 (필터 자체를 완전히 제외할 경우만 사용)
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring();
        // 예: .requestMatchers("/h2-console/**")
    }

    /**
     * Spring Security 필터 체인 구성
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager)
            throws Exception {

        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationManager(authenticationManager)
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz
                // ADMIN 전용 경로
                .requestMatchers("/notice/**", "/employee/new", "/employee/register", "/employee/{empId}/delete")
                .hasRole("ADMIN")

                // 인증 없이 접근 가능한 공개 경로
                .requestMatchers(
                        "/", "/login", "/auth/login", "/auth/reissue", "/favicon.ico", "/.well-known/**", "/error",
                        "/css/**", "/js/**", "/images/**", "/webjars/**"
                ).permitAll()

                // 인증 필요 경로 (일반 사용자 접근 가능)
                .requestMatchers("/index", "/employee").authenticated()

                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.loginPage("/login").permitAll());

        return http.build();
    }
}
