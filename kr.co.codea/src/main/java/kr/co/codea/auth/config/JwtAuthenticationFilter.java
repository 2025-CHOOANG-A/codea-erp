package kr.co.codea.auth.config;

import java.io.IOException;

import org.slf4j.Logger; // Slf4j Logger 사용 시
import org.slf4j.LoggerFactory; // Slf4j Logger 사용 시
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component // SecurityConfig에서 Bean으로 등록하므로 여기서는 제거해도 무방, 또는 Config에서 new로 생성
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = resolveToken(request);
        System.out.println("JwtAuthenticationFilter - 추출된 JWT: "+ jwt);

        if (StringUtils.hasText(jwt)) {
            boolean isValidToken = jwtProvider.validateToken(jwt);
            System.out.println("JwtAuthenticationFilter - 토큰 유효성 검증 결과: " + isValidToken);

            if (isValidToken) {
                Authentication authentication = jwtProvider.getAuthentication(jwt);
                if (authentication != null) {
                	System.out.println("JwtAuthenticationFilter - 생성된 Authentication 객체: 사용자명="+authentication.getName()+", 인증여부="+authentication.isAuthenticated()); // "생성된 Authentication 객체"
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                	System.out.println("JwtAuthenticationFilter - jwtProvider.getAuthentication(jwt)가 null을 반환했습니다.");
                }
            }
        } else {
        	System.out.println("JwtAuthenticationFilter - 요청 헤더에 JWT 토큰이 없거나 토큰이 비어있습니다."); 
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}