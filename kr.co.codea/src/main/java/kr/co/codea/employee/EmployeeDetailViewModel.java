package kr.co.codea.employee;

import kr.co.codea.auth.dto.UserDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * 사원 상세 정보 조회 시 사용되는 ViewModel 클래스입니다.
 * 사원의 일반 정보와 계정 정보를 함께 제공합니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailViewModel {

    /** 사원의 일반 정보 */
    private EmployeeDto generalInfo;

    /** 사원의 계정 및 인증 정보 */
    private UserDetailsDto accountInfo;

    /**
     * 주어진 인증 정보를 기준으로 현재 사원 정보를 수정할 수 있는지 여부를 판단합니다.
     *
     * @param authentication 현재 로그인한 사용자의 인증 정보
     * @return 수정 가능 여부 (true: 수정 가능, false: 수정 불가)
     */
    public boolean isEditableBy(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || (authentication.getPrincipal() instanceof String)) {
            return false; // 익명 사용자 또는 인증 실패
        }

        // 관리자 권한이 있는 경우 수정 가능
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(auth.getAuthority())) {
                return true;
            }
        }

        // 본인의 프로필인 경우 수정 가능
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsDto user) {
            Long loginEmpId = user.getEmpId();
            Long profileEmpId = (generalInfo != null) ? generalInfo.getEmpId() : null;
            return loginEmpId != null && loginEmpId.equals(profileEmpId);
        }

        return false;
    }
}
