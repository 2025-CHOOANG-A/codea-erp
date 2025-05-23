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

@Component // SecurityConfig에서 @Bean으로 등록하면 생략 가능
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = resolveToken(request);

        // [로그] 요청에서 추출한 JWT 출력
 //       System.out.println("JWT 추출 결과: " + jwt + " (요청 경로: " + request.getRequestURI() + ")");

        if (StringUtils.hasText(jwt)) {
            boolean isValidToken = jwtProvider.validateToken(jwt);

            // [로그] JWT 유효성 결과 출력
//            System.out.println("JWT 유효성 검증 결과: " + isValidToken);

            if (isValidToken) {
                Authentication authentication = jwtProvider.getAuthentication(jwt);

                if (authentication != null) {
                    // [로그] 생성된 Authentication 정보 출력
//                    System.out.println("인증 정보 생성 완료: 사용자명=" + authentication.getName()
//                            + ", 인증 여부=" + authentication.isAuthenticated());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // [로그] 인증 객체 생성 실패
//                    System.out.println("인증 객체 생성 실패: jwtProvider.getAuthentication() == null");
                }
            }
        } else {
            // [로그] JWT가 요청에 존재하지 않거나 비어 있음
//            System.out.println("JWT 미존재 또는 빈 값 (요청 경로: " + request.getRequestURI() + ")");
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더에서 토큰 추출
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
//        System.out.println("Authorization 헤더 값: " + bearerToken);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
//            System.out.println("Bearer 토큰 형식 확인됨. 헤더에서 토큰 반환.");
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        // 2. accessToken 쿠키에서 토큰 추출
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    String tokenFromCookie = cookie.getValue();
//                    System.out.println("accessToken 쿠키 값: " + tokenFromCookie);

                    if (StringUtils.hasText(tokenFromCookie)) {
//                        System.out.println("쿠키에서 토큰 반환.");
                        return tokenFromCookie;
                    }
                }
            }
        }

        // [로그] 토큰 추출 실패
//        System.out.println("'" + ACCESS_TOKEN_COOKIE_NAME + "' 쿠키 없음 또는 값 비어 있음.");
        return null;
    }
}
