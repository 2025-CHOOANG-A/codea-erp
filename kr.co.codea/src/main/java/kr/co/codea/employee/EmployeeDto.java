package kr.co.codea.employee;

import lombok.Data;

/**
 * 사원 정보 전달용 DTO 클래스입니다.
 * 사원 등록, 수정, 상세조회 등에서 사용됩니다.
 */
@Data
public class EmployeeDto {

    /** 사원 고유 ID (PK) */
    private Long empId;

    /** 로그인용 사용자 ID */
    private String empUserId;

    /** 사번 */
    private String empNo;

    /** 사원 이름 */
    private String empName;

    /** 부서명 */
    private String empDept;

    /** 직책명 */
    private String empPosition;

    /** 이메일 주소 */
    private String email;

    /** 내선 전화번호 */
    private String tel;

    /** 휴대폰 번호 */
    private String hp;

    /** 재직 상태 (true: 재직 중, false: 퇴사) */
    private boolean empStatus;

    /** 관리자 권한 코드 (예: ROLE_ADMIN) */
    private String adminCode;

    /** 관리자 권한 변경 요청 여부 플래그 */
    private boolean adminRoleUpdateIntent = false;

    /** 비밀번호 (암호화 대상) */
    private String empPw;
}
