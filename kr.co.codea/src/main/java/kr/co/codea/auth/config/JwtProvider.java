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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성, 파싱, 검증 및 인증 객체 생성 기능 제공
 */
@Slf4j
@Component
public class JwtProvider {

    private static final String AUTHORITIES_KEY = "auth";         // 권한 정보 클레임 키
    private static final String EMP_ID_KEY = "emp_id";            // 직원 ID 클레임 키
    private static final String EMP_NAME_KEY = "emp_name";        // 직원 이름 클레임 키

    private final Key key;                         // 서명용 키
    private final long accessTokenExpirationMillis;   // Access Token 유효 기간
    private final long refreshTokenExpirationMillis;  // Refresh Token 유효 기간

    public JwtProvider(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        this.accessTokenExpirationMillis = jwtProperties.getAccessTokenExpiration();
        this.refreshTokenExpirationMillis = jwtProperties.getRefreshTokenExpiration();
    }

    /**
     * 인증 정보를 바탕으로 Access Token 생성
     */
    public String createAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.accessTokenExpirationMillis);

        String empUserIdForSubject = authentication.getName();
        Long empIdForClaim = null;
        String empNameForClaim = null;

        // principal이 UserDetailsDto인 경우, 추가 정보 추출
        if (authentication.getPrincipal() instanceof UserDetailsDto) {
            UserDetailsDto principalDto = (UserDetailsDto) authentication.getPrincipal();
            empIdForClaim = principalDto.getEmpId();
            empNameForClaim = principalDto.getEmpName();
        }

        JwtBuilder builder = Jwts.builder()
                .setSubject(empUserIdForSubject)
                .claim(AUTHORITIES_KEY, authorities);

        if (empIdForClaim != null) {
            builder.claim(EMP_ID_KEY, empIdForClaim);
        }

        if (StringUtils.hasText(empNameForClaim)) {
            builder.claim(EMP_NAME_KEY, empNameForClaim);
        }

        return builder.setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 인증 정보를 바탕으로 Refresh Token 생성
     */
    public String createRefreshToken(Authentication authentication) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.refreshTokenExpirationMillis);

        String empUserIdForSubject = authentication.getName();
        Long empIdForClaim = null;

        if (authentication.getPrincipal() instanceof UserDetailsDto) {
            empIdForClaim = ((UserDetailsDto) authentication.getPrincipal()).getEmpId();
        }

        JwtBuilder builder = Jwts.builder()
                .setSubject(empUserIdForSubject);

        if (empIdForClaim != null) {
            builder.claim(EMP_ID_KEY, empIdForClaim);
        }

        return builder.setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 인증 정보 추출 → Authentication 객체 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        Object authoritiesClaim = claims.get(AUTHORITIES_KEY);
        Collection<? extends GrantedAuthority> authorities = new ArrayList<>();

        if (authoritiesClaim != null && StringUtils.hasText(authoritiesClaim.toString())) {
            authorities = Arrays.stream(authoritiesClaim.toString().split(","))
                    .map(String::trim)
                    .filter(auth -> !auth.isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } else {
            // log.warn("No '{}' claim found in token or claim is empty for subject: {}. Setting empty authorities.",
            //         AUTHORITIES_KEY, claims.getSubject());
        }

        String empUserIdFromSubject = claims.getSubject();
        Long empIdFromClaim = claims.get(EMP_ID_KEY, Long.class);
        String empNameFromClaim = claims.get(EMP_NAME_KEY, String.class);

        UserDetails principal = UserDetailsDto.builder()
                .empUserId(empUserIdFromSubject)
                .empId(empIdFromClaim)
                .empName(empNameFromClaim)
                .authorities(authorities)
                .empStatus(true)
                .build();

        // log.debug("Authentication object created from token. Principal: empUserId={}, empId={}, empName={}, authorities={}",
        //         ((UserDetailsDto) principal).getEmpUserId(),
        //         ((UserDetailsDto) principal).getEmpId(),
        //         ((UserDetailsDto) principal).getEmpName(),
        //         principal.getAuthorities());

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * 토큰에서 사용자 ID(subject) 추출
     */
    public String getUserIdFromToken(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            // log.warn("Failed to extract user ID (subject) from token: {}. Token: {}", e.getMessage(), tokenAbbreviated(token));
            return null;
        }
    }

    /**
     * 토큰에서 emp_id(Long) 추출
     */
    public Long getEmpIdAsLongFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.get(EMP_ID_KEY, Long.class);
        } catch (JwtException | IllegalArgumentException e) {
            // log.warn("Failed to extract emp_id (Long) from token: {}. Token: {}", e.getMessage(), tokenAbbreviated(token));
            return null;
        }
    }

    /**
     * JWT 유효성 검사
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            // log.warn("Invalid JWT signature. Token: [{}], Message: {}", tokenAbbreviated(token), e.getMessage());
        } catch (ExpiredJwtException e) {
            // log.warn("Expired JWT token. Token: [{}], Message: {}", tokenAbbreviated(token), e.getMessage());
        } catch (UnsupportedJwtException e) {
            // log.warn("Unsupported JWT token. Token: [{}], Message: {}", tokenAbbreviated(token), e.getMessage());
        } catch (IllegalArgumentException e) {
            // log.warn("JWT token compact of handler are invalid. Token: [{}], Message: {}", tokenAbbreviated(token), e.getMessage());
        }
        return false;
    }

    /**
     * JWT 파싱하여 Claims 반환 (만료된 토큰도 Claims 추출 가능)
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // log.warn("Token already expired during parsing claims. Subject: {}. Message: {}", e.getClaims().getSubject(), e.getMessage());
            return e.getClaims();
        }
    }

    public long getAccessTokenExpirationMillis() {
        return this.accessTokenExpirationMillis;
    }

    public long getRefreshTokenExpirationMillis() {
        return this.refreshTokenExpirationMillis;
    }

    /**
     * 긴 토큰 문자열을 앞뒤로만 잘라 로그에 간단히 출력하기 위한 유틸
     */
    private String tokenAbbreviated(String token) {
        if (token != null && token.length() > 20) {
            return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
        }
        return token;
    }
}
