package kr.co.codea.employee;

import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class EmployeeDto {
    Long empId;                      // 직원 고유 ID
    String empUserId;               // 로그인용 사용자 ID
    String empNo;                   // 사번
    String empName;                 // 이름
    String empDept;                 // 부서
    String empPosition;             // 직책
    String email;                   // 이메일
    String tel;                     // 내선번호
    String hp;                      // 휴대폰 번호
    String empImg;                  // 사원 이미지 파일명
    MultipartFile profileImgFile;   // 업로드된 프로필 이미지 파일
    boolean empStatus;              // 재직 상태 (true: 재직 중)
    String adminCode;               // 관리자 권한 코드
    boolean adminRoleUpdateIntent = false; // 관리자 권한 변경 여부
    String empPw;                   // 비밀번호
}
