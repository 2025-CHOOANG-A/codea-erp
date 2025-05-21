package kr.co.codea.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie; // jakarta.servlet.http.Cookie 임포트
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component; // @Component는 SecurityConfig에서 Bean으로 등록 시 불필요할 수 있음
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // SecurityConfig에서 @Bean으로 생성하고 있으므로, 이 어노테이션은 없어도 됩니다.
           // 만약 컴포넌트 스캔으로 이 필터를 등록하고 싶다면 유지합니다.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken"; // 쿠키 이름을 상수로 정의

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = resolveToken(request);
        System.out.println("JwtAuthenticationFilter - resolveToken 결과로 추출된 JWT: "+ jwt + " (요청 경로: " + request.getRequestURI() + ")");

        if (StringUtils.hasText(jwt)) {
            boolean isValidToken = jwtProvider.validateToken(jwt);
            System.out.println("JwtAuthenticationFilter - 토큰 유효성 검증 결과: " + isValidToken);

            if (isValidToken) {
                Authentication authentication = jwtProvider.getAuthentication(jwt);
                if (authentication != null) {
                    System.out.println("JwtAuthenticationFilter - 생성된 Authentication 객체: 사용자명="+authentication.getName()+", 인증여부="+authentication.isAuthenticated());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    System.out.println("JwtAuthenticationFilter - jwtProvider.getAuthentication(jwt)가 null을 반환했습니다.");
                }
            }
        } else {
            System.out.println("JwtAuthenticationFilter - 요청 헤더 또는 쿠키에 JWT 토큰이 없거나 토큰이 비어있습니다. (요청 경로: " + request.getRequestURI() + ")");
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. HTTP Authorization 헤더에서 토큰 확인 (기존 방식)
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        System.out.println("JwtAuthenticationFilter - 토큰 추출 시도 - Authorization 헤더 값: " + bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            System.out.println("JwtAuthenticationFilter - 헤더에서 Bearer 토큰 발견 및 반환.");
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        // 2. 쿠키에서 "accessToken"이라는 이름의 토큰 확인 (새로 추가된 방식)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    String tokenFromCookie = cookie.getValue();
                    System.out.println("JwtAuthenticationFilter - 토큰 추출 시도 - 쿠키 '" + ACCESS_TOKEN_COOKIE_NAME + "' 값: " + tokenFromCookie);
                    if (StringUtils.hasText(tokenFromCookie)) {
                        System.out.println("JwtAuthenticationFilter - 쿠키에서 '" + ACCESS_TOKEN_COOKIE_NAME + "' 토큰 발견 및 반환.");
                        return tokenFromCookie;
                    }
                }
            }
        }
        System.out.println("JwtAuthenticationFilter - 토큰 추출 시도 - 쿠키에서 '" + ACCESS_TOKEN_COOKIE_NAME + "'을(를) 찾지 못함.");
        return null;
    }
}