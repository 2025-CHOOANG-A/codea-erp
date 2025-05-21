package kr.co.codea.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor 포함
@NoArgsConstructor // MyBatis 등에서 객체 생성 시 기본 생성자가 필요할 수 있음
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 추가
@Builder // 빌더 패턴 사용
public class UserDetailsDto implements UserDetails {

	private Long empId;      // 숫자형 직원 고유 ID (EMP_ID)
    private String username; // 로그인 ID (EMP_USER_ID, 예: "hongload")
    private String password; // EMP_PW (암호화된 값)
    private Collection<? extends GrantedAuthority> authorities;
    private boolean enabled; // 계정 활성화 상태 (EMP_STATUS)

    // EmployeeMapper.xml에서 권한 문자열을 가져올 경우를 대비한 임시 필드
    // UserDetailsServiceImpl에서 이 값을 사용하여 authorities를 채웁니다.
    // @Data 어노테이션으로 getter/setter 자동 생성됨
    private String rawAuthoritiesString;

    // UserDetails 인터페이스 메소드 구현
    // @Data 어노테이션이 getter를 생성해주므로 별도 구현 불필요한 경우가 많으나,
    // 인터페이스 메소드 시그니처와 정확히 일치해야 하므로 명시적으로 오버라이드 하는 것이 안전할 수 있습니다.
    // Lombok이 생성하는 getter 이름이 getUsername(), getPassword(), getAuthorities(), isEnabled()와 일치하므로
    // 대부분의 경우 명시적 오버라이드가 필요 없지만, is... 형태의 boolean getter는 주의가 필요할 수 있습니다.
    // 여기서는 Lombok이 생성하는 getter가 UserDetails 인터페이스의 요구사항을 만족한다고 가정합니다.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
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

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}