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
        AuthenticationManagerBuilder authenticationManagerBuilder = http
                .getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsServiceImpl) // 직접 구현한 UserDetailsService 설정
                .passwordEncoder(passwordEncoder()); // PasswordEncoder 설정
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
            // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class) // 위치를 여기로 옮기거나, authorizeHttpRequests 이후에 두어도 됩니다.
            .authorizeHttpRequests(authz -> authz
                // 로그인 페이지 및 관련 정적 리소스, 로그인 API, 토큰 재발급 API는 항상 허용
                .requestMatchers("/", "/login", "/auth/login", "/auth/reissue").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // 정적 리소스 경로 예시
                // .requestMatchers("/index").authenticated() // /index 페이지는 인증 필요 (이전 HomeController 예시)
                .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
            )
            // formLogin 설정을 통해 로그인 페이지 지정 및 관련 설정
            .formLogin(formLogin -> formLogin
                .loginPage("/login") // 커스텀 로그인 페이지 경로 (PageController에서 설정한 경로)
                .permitAll() // 로그인 페이지 자체는 항상 접근 가능해야 함
            );

            // 필요에 따라 인증/인가 예외 처리 핸들러 추가
            // .exceptionHandling(exceptions -> exceptions
            // .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
            // .accessDeniedHandler(new CustomAccessDeniedHandler())
            // );

        return http.build();
    }
}