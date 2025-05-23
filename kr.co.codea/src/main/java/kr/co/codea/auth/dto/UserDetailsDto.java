package kr.co.codea.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Spring Security의 UserDetails 구현체
 * - 사원 정보 및 권한 정보를 담는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailsDto implements UserDetails {

    private Long empId;           // 직원 고유 ID (DB: EMP_ID)
    private String empUserId;     // 로그인 ID (DB: EMP_USER_ID)
    private String empName;       // 사용자 이름 (DB: EMP_NAME)
    private String empPw;         // 암호화된 비밀번호 (DB: EMP_PW)
    private Collection<? extends GrantedAuthority> authorities; // 권한 목록
    private boolean empStatus;    // 계정 활성 상태 (DB: EMP_STATUS)

    private String adminCode;     // DB의 ADMIN_CODE (권한 부여 시 사용)

    // 필요 시 사용 가능한 필드 예시:
    // private String rawAuthoritiesString; // 문자열 형태의 권한 목록 (현재 미사용)

    /** 권한 목록 반환 */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    /** 암호화된 비밀번호 반환 */
    @Override
    public String getPassword() {
        return this.empPw;
    }

    /** 로그인 ID 반환 (고유 식별자) */
    @Override
    public String getUsername() {
        return this.empUserId;
    }

    /** 계정 만료 여부 (기본값: 만료되지 않음) */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 계정 잠금 여부 (기본값: 잠금 아님) */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** 자격 증명 만료 여부 (기본값: 만료되지 않음) */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** 계정 활성 상태 반환 */
    @Override
    public boolean isEnabled() {
        return this.empStatus;
    }
}
