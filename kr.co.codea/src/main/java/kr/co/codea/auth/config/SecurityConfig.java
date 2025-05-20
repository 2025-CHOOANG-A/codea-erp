package kr.co.codea.auth.config;

import kr.co.codea.auth.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
    private final UserDetailsServiceImpl userDetailsServiceImpl; // UserDetailsService 주입

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
            .userDetailsService(userDetailsServiceImpl) // 직접 구현한 UserDetailsService 설정
            .passwordEncoder(passwordEncoder());       // PasswordEncoder 설정
        return authenticationManagerBuilder.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (Stateless 서버)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함
            .authenticationManager(authenticationManager) // 직접 구성한 AuthenticationManager 설정
            .authorizeHttpRequests(authz -> authz
            		//.requestMatchers("/auth/login","/auth/reissue").permitAll()// 기존 설정, 아래 permitAll()에 의해 모두 허용됨
            		//.requestMatchers("/admin/**").hasRole("ADMIN") // 특정 권한 필요 경로 예시
            		.anyRequest().permitAll() // <-- 중요: 모든 요청을 인증 없이 허용하도록 변경
            )
            // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

            // 필요에 따라 인증/인가 예외 처리 핸들러 추가
            // .exceptionHandling(exceptions -> exceptions
            // .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
            // .accessDeniedHandler(new CustomAccessDeniedHandler())
            // );

        return http.build();
    }
}