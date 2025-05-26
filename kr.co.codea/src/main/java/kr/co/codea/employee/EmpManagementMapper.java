package kr.co.codea.employee;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 사원 관리 관련 MyBatis 매퍼
 */
@Mapper
public interface EmpManagementMapper {

    // 전체 사원 목록 조회 (리스트 출력용)
    List<EmployeeDto> findAllEmployeesForList();

    // 사원 ID로 사원 정보 조회
    Optional<EmployeeDto> findEmployeeById(@Param("empId") Long empId);

    // Map 기반의 동적 사원 정보 수정
    int updateEmployeeDetailsByMap(Map<String, Object> params);

    // 사원 정보 수정 (DTO 기반)
    int updateEmployee(EmployeeDto employeeDto);

    // 사원 삭제 (ID 기준)
    int deleteEmployeeById(@Param("empId") Long empId);

    // 사원 등록
    int insertEmployee(EmployeeDto employeeDto);

    // 사번으로 중복 여부 확인
    Optional<EmployeeDto> findByEmpNo(@Param("empNo") String empNo);

    // 이메일로 중복 여부 확인
    Optional<EmployeeDto> findByEmail(@Param("email") String email);
}
