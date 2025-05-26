package kr.co.codea.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 관련 설정 값을 application.yml 또는 application.properties에서 불러와 바인딩하는 클래스.
 *
 * 예시 (application.yml):
 * jwt:
 *   secret: my-jwt-secret-key
 *   access-token-expiration: 3600000       # 1시간 (ms)
 *   refresh-token-expiration: 1209600000   # 14일 (ms)
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 서명을 위한 비밀 키 (Base64 또는 일반 문자열)
     */
    private String secret;

    /**
     * Access Token 유효 기간 (밀리초 단위)
     */
    private long accessTokenExpiration;

    /**
     * Refresh Token 유효 기간 (밀리초 단위)
     */
    private long refreshTokenExpiration;
}
