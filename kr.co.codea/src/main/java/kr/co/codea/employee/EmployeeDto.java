package kr.co.codea.employee;

import lombok.Data;

@Data
public class EmployeeDto {
	Long empId;         // 직원 고유 ID
    String empNo;       // 사번
    String empName;     // 이름
    String empDept;     // 부서
    String empPosition; // 직책
    String email;       // 이메일
    String tel;
    String hp;         // 내선번호 (회사 전화번호)
    String empImg;      // 사원 이미지
}
