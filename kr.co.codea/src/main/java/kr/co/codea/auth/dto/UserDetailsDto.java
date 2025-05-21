package kr.co.codea.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailsDto implements UserDetails {

    private Long empId;      // 숫자형 직원 고유 ID (DB의 EMP_ID)
    private String empUserId; // ✨ 로그인 ID (DB의 EMP_USER_ID) ✨
    private String empName;  // 실제 사용자 이름 (DB의 EMP_NAME)
    private String empPw;    // ✨ DB의 EMP_PW (암호화된 값) ✨
    private Collection<? extends GrantedAuthority> authorities; // 최종적으로 설정될 권한 목록
    private boolean empStatus; // ✨ 계정 활성화 상태 (DB의 EMP_STATUS) ✨

    // EmployeeMapper.xml의 'ADMIN_CODE as adminCode' 매핑을 통해 DB의 ADMIN_CODE 값을 받기 위한 필드
    // UserDetailsServiceImpl에서 이 값을 사용하여 'authorities'를 동적으로 생성합니다.
    private String adminCode;

    // UserDetailsServiceImpl에서 권한 문자열을 임시로 받아 처리하기 위한 필드 (선택 사항)
    // private String rawAuthoritiesString; // 필요하다면 유지, 현재는 adminCode 직접 사용


    // UserDetails 인터페이스 메소드 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    /**
     * 데이터베이스에 저장된 암호화된 비밀번호(empPw)를 반환합니다.
     */
    @Override
    public String getPassword() {
        return this.empPw; // ✨ empPw 필드 반환 ✨
    }

    /**
     * Spring Security에서 사용자의 고유 식별자로 사용됩니다.
     * 우리 시스템에서는 로그인 ID (empUserId)를 반환합니다.
     */
    @Override
    public String getUsername() {
        return this.empUserId; // ✨ empUserId 필드 반환 ✨
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 필요에 따라 로직 추가
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 필요에 따라 로직 추가
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 필요에 따라 로직 추가
    }

    /**
     * 계정 활성화 상태를 반환합니다. (DB의 EMP_STATUS 값에 따라 설정됨)
     */
    @Override
    public boolean isEnabled() {
        return this.empStatus; // ✨ empStatus 필드 반환 ✨
    }
}