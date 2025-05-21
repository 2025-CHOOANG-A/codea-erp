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
import org.springframework.util.StringUtils; // StringUtils import

import java.security.Key;
import java.util.ArrayList; // ArrayList import
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String EMP_ID_KEY = "emp_id"; // EMP_ID를 저장할 클레임 이름

    private final Key key;
    private final long accessTokenExpirationMillis;
    private final long refreshTokenExpirationMillis;

    public JwtProvider(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        this.accessTokenExpirationMillis = jwtProperties.getAccessTokenExpiration();
        this.refreshTokenExpirationMillis = jwtProperties.getRefreshTokenExpiration();
        log.info("JwtProvider initialized. AccessToken Expiration: {}ms, RefreshToken Expiration: {}ms",
                accessTokenExpirationMillis, refreshTokenExpirationMillis);
        if (jwtProperties.getSecret() == null || jwtProperties.getSecret().getBytes().length < 32) { // HS256은 최소 256비트(32바이트) 권장
            log.warn("JWT secret key is too short (less than 32 bytes) or null. Consider using a stronger key.");
        }
    }

    /**
     * Access Token을 생성합니다.
     * @param authentication 현재 인증된 사용자의 Authentication 객체.
     * principal로 UserDetailsDto를 포함하며, getName()은 empUserId를 반환해야 합니다.
     * @return 생성된 Access Token 문자열.
     */
    public String createAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.accessTokenExpirationMillis);

        // UserDetailsDto의 getUsername()은 empUserId를 반환합니다.
        String empUserIdForSubject = authentication.getName();
        Long empIdForClaim = null;

        if (authentication.getPrincipal() instanceof UserDetailsDto) {
            // UserDetailsDto의 getEmpId()를 호출하여 숫자 EMP_ID를 가져옵니다.
            empIdForClaim = ((UserDetailsDto) authentication.getPrincipal()).getEmpId();
        }

        JwtBuilder builder = Jwts.builder()
                .setSubject(empUserIdForSubject) // 토큰의 주체(subject)로는 empUserId (로그인 ID) 사용
                .claim(AUTHORITIES_KEY, authorities); // 권한 정보 저장

        if (empIdForClaim != null) {
            builder.claim(EMP_ID_KEY, empIdForClaim); // 숫자 EMP_ID를 별도 클레임으로 저장
            log.debug("Creating Access Token for subject (empUserId): {}, emp_id: {}, authorities: {}",
                    empUserIdForSubject, empIdForClaim, authorities);
        } else {
            log.warn("Creating Access Token for subject (empUserId): {} without emp_id claim. Authorities: {}",
                    empUserIdForSubject, authorities);
        }

        return builder.setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token을 생성합니다.
     * Refresh Token에는 일반적으로 민감한 정보(예: 권한)를 최소화합니다.
     * @param authentication 현재 인증된 사용자의 Authentication 객체.
     * @return 생성된 Refresh Token 문자열.
     */
    public String createRefreshToken(Authentication authentication) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.refreshTokenExpirationMillis);

        String empUserIdForSubject = authentication.getName(); // UserDetailsDto.getUsername() -> empUserId
        Long empIdForClaim = null;

        if (authentication.getPrincipal() instanceof UserDetailsDto) {
            empIdForClaim = ((UserDetailsDto) authentication.getPrincipal()).getEmpId();
        }

        JwtBuilder builder = Jwts.builder()
                .setSubject(empUserIdForSubject); // Refresh Token의 subject도 empUserId 사용

        if (empIdForClaim != null) {
            builder.claim(EMP_ID_KEY, empIdForClaim); // 숫자 EMP_ID도 포함
            log.debug("Creating Refresh Token for subject (empUserId): {}, emp_id: {}",
                    empUserIdForSubject, empIdForClaim);
        } else {
            log.warn("Creating Refresh Token for subject (empUserId): {} without emp_id claim.",
                    empUserIdForSubject);
        }

        return builder.setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 토큰에서 인증 정보(Authentication 객체)를 조회합니다.
     * @param token 검증할 JWT 토큰 문자열.
     * @return 생성된 Authentication 객체. principal은 UserDetailsDto 인스턴스입니다.
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
            log.warn("No '{}' claim found in token or claim is empty for subject: {}. Setting empty authorities.",
                    AUTHORITIES_KEY, claims.getSubject());
        }

        String empUserIdFromSubject = claims.getSubject(); // 토큰의 subject (EMP_USER_ID)
        Long empIdFromClaim = claims.get(EMP_ID_KEY, Long.class); // "emp_id" 클레임에서 EMP_ID (Long) 가져오기

        // JwtAuthenticationFilter에서 사용할 Principal 객체(UserDetailsDto) 생성
        // 이 객체는 인증 컨텍스트에 저장되며, @AuthenticationPrincipal로 주입됩니다.
        // 비밀번호(empPw)나 계정상태(empStatus) 등은 토큰에 보통 없으므로,
        // 여기서는 토큰에서 가져온 정보 위주로 채웁니다.
        // UserDetailsDto의 UserDetails 인터페이스 메소드(getPassword, isEnabled)는
        // UserDetailsServiceImpl에서 DB 조회 시 설정된 값을 사용합니다.
        // 필터에서 만드는 principal은 주로 식별자와 권한 정보가 중요합니다.
        UserDetails principal = UserDetailsDto.builder()
                .empUserId(empUserIdFromSubject) // ✨ username 대신 empUserId 필드에 설정 ✨
                .empId(empIdFromClaim)
                .authorities(authorities)
                // enabled 상태는 토큰 유효 시 true로 간주할 수 있으나,
                // 실제 UserDetailsDto의 empStatus 필드는 DB값 기반이므로 여기서는 기본값 처리.
                // UserDetailsServiceImpl에서 생성된 UserDetailsDto와는 역할이 약간 다름.
                // 이 principal은 인증된 사용자를 나타내는 식별표 역할.
                .empStatus(true) // 토큰이 유효하면 일단 활성 상태로 간주 (필요시 DB 재확인 정책 고려)
                // empName, empPw 등은 토큰에 없으므로 여기서는 설정하지 않음.
                .build();

        log.debug("Authentication object created from token. Principal: empUserId={}, empId={}, authorities={}",
                ((UserDetailsDto) principal).getEmpUserId(),
                ((UserDetailsDto) principal).getEmpId(),
                principal.getAuthorities());

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * 토큰에서 사용자 로그인 ID (Subject, 즉 EMP_USER_ID)를 추출합니다.
     * @param token JWT 토큰 문자열.
     * @return 추출된 사용자 로그인 ID (EMP_USER_ID) 또는 오류 시 null.
     */
    public String getUserIdFromToken(String token) { // 메소드명은 "로그인 ID"를 가져온다는 의미로 getLoginIdFromToken 등이 더 명확할 수 있음
        try {
            return parseClaims(token).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Failed to extract user ID (subject) from token: {}. Token: {}", e.getMessage(), token);
            return null;
        }
    }

    /**
     * 토큰에서 EMP_ID (Long 타입)를 추출합니다.
     * @param token JWT 토큰 문자열.
     * @return 추출된 EMP_ID (Long) 또는 오류 시 null.
     */
    public Long getEmpIdAsLongFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.get(EMP_ID_KEY, Long.class);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Failed to extract emp_id (Long) from token: {}. Token: {}", e.getMessage(), token);
            return null;
        }
    }

    /**
     * 토큰의 유효성을 검증합니다 (서명, 만료 시간 등).
     * @param token 검증할 JWT 토큰 문자열.
     * @return 유효하면 true, 그렇지 않으면 false.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature. Token: [{}], Message: {}", tokenAbbreviated(token), e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token. Token: [{}], Message: {}", tokenAbbreviated(token), e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token. Token: [{}], Message: {}", tokenAbbreviated(token), e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT token compact of handler are invalid. Token: [{}], Message: {}", tokenAbbreviated(token), e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token already expired during parsing claims. Subject: {}. Message: {}", e.getClaims().getSubject(), e.getMessage());
            return e.getClaims(); // 만료된 토큰이라도 클레임은 반환 (validateToken에서 최종 판단)
        }
    }

    public long getAccessTokenExpirationMillis() {
        return this.accessTokenExpirationMillis;
    }

    public long getRefreshTokenExpirationMillis() {
        return this.refreshTokenExpirationMillis;
    }

    // 로그 출력 시 긴 토큰 전체를 보여주지 않기 위한 유틸리티 메소드
    private String tokenAbbreviated(String token) {
        if (token != null && token.length() > 20) {
            return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
        }
        return token;
    }
}