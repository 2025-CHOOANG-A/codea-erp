package kr.co.codea.auth.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import kr.co.codea.auth.dto.UserDetailsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성, 파싱, 검증, 인증 객체 생성 기능 제공
 */
@Slf4j
@Component
public class JwtProvider {

    private static final String AUTHORITIES_KEY = "auth";      // 권한 클레임 키
    private static final String EMP_ID_KEY = "emp_id";         // 직원 ID 클레임 키
    private static final String EMP_NAME_KEY = "emp_name";     // 직원 이름 클레임 키

    private final Key key;                         // JWT 서명용 키
    private final long accessTokenExpirationMillis;   // Access Token 유효시간 (ms)
    private final long refreshTokenExpirationMillis;  // Refresh Token 유효시간 (ms)

    public JwtProvider(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        this.accessTokenExpirationMillis = jwtProperties.getAccessTokenExpiration();
        this.refreshTokenExpirationMillis = jwtProperties.getRefreshTokenExpiration();
    }

    /**
     * 인증 정보(Authentication) 기반으로 Access Token 생성
     */
    public String createAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiry = new Date(now.getTime() + this.accessTokenExpirationMillis);

        String subject = authentication.getName(); // 보통 EMP_USER_ID
        Long empId = null;
        String empName = null;

        if (authentication.getPrincipal() instanceof UserDetailsDto principal) {
            empId = principal.getEmpId();
            empName = principal.getEmpName();
        }

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .claim(AUTHORITIES_KEY, authorities);

        if (empId != null) builder.claim(EMP_ID_KEY, empId);
        if (StringUtils.hasText(empName)) builder.claim(EMP_NAME_KEY, empName);

        return builder
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 인증 정보 기반으로 Refresh Token 생성
     */
    public String createRefreshToken(Authentication authentication) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + this.refreshTokenExpirationMillis);

        String subject = authentication.getName(); // 보통 EMP_USER_ID
        Long empId = null;

        if (authentication.getPrincipal() instanceof UserDetailsDto principal) {
            empId = principal.getEmpId();
        }

        JwtBuilder builder = Jwts.builder().setSubject(subject);
        if (empId != null) builder.claim(EMP_ID_KEY, empId);

        return builder
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT에서 Authentication 객체를 생성
     * - SecurityContext에 저장될 인증 정보 구성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        Collection<? extends GrantedAuthority> authorities = Optional.ofNullable(claims.get(AUTHORITIES_KEY))
                .map(Object::toString)
                .map(str -> Arrays.stream(str.split(","))
                        .map(String::trim)
                        .filter(auth -> !auth.isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        UserDetails principal = UserDetailsDto.builder()
                .empUserId(claims.getSubject())
                .empId(claims.get(EMP_ID_KEY, Long.class))
                .empName(claims.get(EMP_NAME_KEY, String.class))
                .authorities(authorities)
                .empStatus(true)
                .build();

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * 토큰에서 사용자 ID(subject)를 추출
     */
    public String getUserIdFromToken(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 토큰에서 직원 ID(emp_id) 추출
     */
    public Long getEmpIdAsLongFromToken(String token) {
        try {
            return parseClaims(token).get(EMP_ID_KEY, Long.class);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | ExpiredJwtException |
                 UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * JWT 문자열을 Claims 객체로 파싱
     * - 만료된 토큰도 클레임 추출 가능
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료된 토큰도 claims는 꺼낼 수 있음
        }
    }

    public long getAccessTokenExpirationMillis() {
        return this.accessTokenExpirationMillis;
    }

    public long getRefreshTokenExpirationMillis() {
        return this.refreshTokenExpirationMillis;
    }

    /**
     * 로그 출력 시 긴 토큰을 앞/뒤 일부만 보여주기 위한 유틸
     */
    private String tokenAbbreviated(String token) {
        if (token != null && token.length() > 20) {
            return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
        }
        return token;
    }
}
