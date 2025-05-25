package kr.co.codea.employee;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;

/**
 * 사원 관련 서비스 인터페이스
 */
public interface EmployeeService {

    // 전체 사원 목록 조회
    List<EmployeeDto> getAllEmployeesForList();

    // ID로 사원 기본 정보 조회
    Optional<EmployeeDto> getEmployeeById(Long empId);

    // 사원 상세 보기용 정보 조회
    Optional<EmployeeDetailViewModel> getEmployeeDetailForView(Long empId);

    // 사원 정보 수정 (비밀번호 포함 가능)
    void updateEmployee(EmployeeDto employeeDto, Authentication currentUser);

    // 비밀번호 제외한 사원 정보 수정
    EmployeeDto updateEmployeeNonPassword(EmployeeDto employeeDto);

    // 사원 삭제
    void deleteEmployee(Long empId);

    // 사원 등록
    EmployeeDto registerEmployee(EmployeeDto employeeDto) throws Exception;
}
