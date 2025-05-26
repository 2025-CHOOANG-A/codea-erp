package kr.co.codea.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * - 요청이 들어올 때마다 JWT 토큰을 검사하고, 인증 정보를 SecurityContext에 설정한다.
 * - 이 필터는 Spring Security의 OncePerRequestFilter를 상속받아 모든 요청마다 한 번만 실행된다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // HTTP 요청 헤더명
    public static final String AUTHORIZATION_HEADER = "Authorization";

    // Bearer 접두사
    public static final String BEARER_PREFIX = "Bearer ";

    // accessToken 쿠키명
    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 요청에서 JWT 추출
        String jwt = resolveToken(request);

        // 토큰이 존재하고 유효할 경우
        if (StringUtils.hasText(jwt) && jwtProvider.validateToken(jwt)) {

            // 토큰으로부터 Authentication 객체 생성
            Authentication authentication = jwtProvider.getAuthentication(jwt);

            // 인증 객체가 유효할 경우 SecurityContext에 설정
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * 요청에서 JWT 토큰을 추출한다.
     * 1. Authorization 헤더에서 추출 (우선순위)
     * 2. accessToken 쿠키에서 추출 (헤더가 없는 경우)
     *
     * @param request HTTP 요청 객체
     * @return 추출된 JWT 토큰 문자열 또는 null
     */
    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더에서 "Bearer {token}" 형식으로 추출
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        // 2. accessToken 쿠키에서 추출
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (StringUtils.hasText(token)) {
                        return token;
                    }
                }
            }
        }

        // 유효한 토큰이 없을 경우 null 반환
        return null;
    }
}
