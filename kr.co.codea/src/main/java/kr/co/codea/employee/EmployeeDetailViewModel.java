// kr.co.codea.employee.EmployeeDetailViewModel.java
package kr.co.codea.employee;

import kr.co.codea.auth.dto.UserDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority; // GrantedAuthority 임포트 추가

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailViewModel {
    private EmployeeDto generalInfo;     // 사원의 일반 정보
    private UserDetailsDto accountInfo;  // 사원의 계정/인증 정보

    /**
     * 현재 프로필을 주어진 인증 정보의 사용자가 수정할 수 있는지 판단합니다.
     *
     * @param authentication 현재 로그인한 사용자의 Authentication 객체
     * @return 수정 가능하면 true, 그렇지 않으면 false
     */
    public boolean isEditableBy(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
            (authentication.getPrincipal() instanceof String)) { // 익명 사용자인 경우 (principal이 String "anonymousUser" 등)
            return false;
        }

        // 1. 관리자(ROLE_ADMIN)인 경우 항상 수정 가능
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(auth.getAuthority())) {
                return true;
            }
        }

        // 2. 관리자가 아니면서, 자신의 프로필을 보는 경우 수정 가능
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsDto) {
            UserDetailsDto loggedInUser = (UserDetailsDto) principal;
            Long loggedInUserEmpId = loggedInUser.getEmpId(); // 로그인한 사용자의 empId

            // 현재 ViewModel이 generalInfo를 가지고 있고, 그 안에 empId가 있는지 확인
            if (this.generalInfo != null && this.generalInfo.getEmpId() != null) {
                Long viewedProfileEmpId = this.generalInfo.getEmpId(); // 현재 보고 있는 프로필의 empId
                if (loggedInUserEmpId != null && loggedInUserEmpId.equals(viewedProfileEmpId)) {
                    return true; // 로그인한 사용자의 ID와 프로필의 ID가 일치하면 수정 가능
                }
            }
        }

        return false; // 그 외의 모든 경우 수정 불가
    }
}