package kr.co.codea.auth.config;

import kr.co.codea.auth.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
// import org.springframework.boot.autoconfigure.security.servlet.PathRequest; // 이제 이 import는 필요하지 않을 수 있습니다.
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
// import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // 이 import도 이제 필요하지 않을 수 있습니다.

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
     * Spring Security 6.x에서는 대부분의 경우 WebSecurityCustomizer의 ignoring() 대신
     * HttpSecurity의 authorizeHttpRequests().permitAll()을 권장합니다.
     * 따라서 이 메서드는 비워두거나 (정적 리소스 외에 무시할 것이 없다면) 제거할 수 있습니다.
     * 만약 H2-console 등과 같이 필터 체인 자체를 완전히 무시해야 하는 특별한 경우가 아니라면 사용하지 않는 것이 좋습니다.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                // H2-console과 같이 필터 체인 자체를 무시해야 하는 경우에만 이곳에 추가합니다.
                // .requestMatchers(new AntPathRequestMatcher("/h2-console/**"))
                ; // 정적 리소스는 이제 SecurityFilterChain에서 처리합니다.
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
                        // 1. 가장 구체적인 규칙을 먼저 정의합니다. (예: ADMIN 역할)
                        .requestMatchers("/notice/**").hasRole("ADMIN") // 공지사항 하위 모든 경로는 ADMIN 역할만 접근 가능

                        // 2. 인증 없이 접근 가능한 공개 경로를 정의합니다.
                        // 이제 정적 리소스(css, js, 이미지 등), favicon.ico, .well-known/**, /error 등도 이곳에서 permitAll() 합니다.
                        .requestMatchers(
                                "/",
                                "/login",
                                "/auth/login",
                                "/auth/reissue",
                                "/favicon.ico",
                                "/.well-known/**",
                                "/error",
                                "/css/**", // 정적 리소스 추가
                                "/js/**",  // 정적 리소스 추가
                                "/images/**", // 정적 리소스 추가
                                "/webjars/**" // 정적 리소스 추가 (WebJars 사용 시)
                        ).permitAll()
                        // /index는 일반적으로 인증된 사용자만 접근하고 싶으므로, permitAll() 목록에서 제외합니다.

                        // 3. 그 외 모든 요청은 인증이 필요합니다. (가장 마지막에 위치)
                        .anyRequest().authenticated()
                        //.anyRequest().permitAll()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .permitAll()
                );
        return http.build();
    }
}