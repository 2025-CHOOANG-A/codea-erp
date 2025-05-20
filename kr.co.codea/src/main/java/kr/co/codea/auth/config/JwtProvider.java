package kr.co.codea.auth.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import kr.co.codea.auth.dto.UserDetailsDto; // UserDetailsDto 사용 (Step 1에서 생성)
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private final Key key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtProvider(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        this.accessTokenValidityInMilliseconds = jwtProperties.getAccessTokenExpiration();
        this.refreshTokenValidityInMilliseconds = jwtProperties.getRefreshTokenExpiration();
    }

    // Access Token 생성 (Authentication 객체 받도록 수정)
    public String createAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(authentication.getName()) // username (여기서는 empId 또는 로그인 ID)
                .claim(AUTHORITIES_KEY, authorities) // 권한 정보 저장
                .setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성 (Authentication 객체 받도록 수정)
    // Refresh Token에는 일반적으로 최소한의 정보만 담거나, 별도의 식별자만 사용하기도 합니다.
    // 여기서는 Access Token과 유사하게 만들되, 만료 시간만 다르게 설정합니다.
    public String createRefreshToken(Authentication authentication) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                // Refresh Token에는 Subject만 포함하거나, 필요에 따라 다른 정보 추가 가능
                .setSubject(authentication.getName())
                .setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // UserDetailsDto (또는 UserDetails 구현체)를 사용하여 Authentication 객체 생성
        // 여기서 claims.getSubject()는 사용자의 ID (예: empId)
        UserDetails principal = UserDetailsDto.builder()
                                    .username(claims.getSubject())
                                    // password는 토큰에 저장하지 않으므로 빈 문자열 또는 null
                                    // 실제 UserDetails 객체는 UserDetailsService에서 로드된 것을 사용하는 것이 일반적
                                    // 이 부분은 JWT 필터에서 SecurityContext에 저장하기 위한 형태
                                    .authorities(authorities)
                                    .build();

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }


    // 토큰에서 사용자 ID (Subject) 추출
    public String getUserIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(key)
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    public long getRefreshTokenExpirationMillis() {
        return this.refreshTokenValidityInMilliseconds;
    }
}