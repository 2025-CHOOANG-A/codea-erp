package kr.co.codea.employee;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmpManagementMapper {
	List<EmployeeDto> findAllEmployeesForList();

	Optional<EmployeeDto> findEmployeeById(@Param("empId") Long empId);
	
	int updateEmployeeDetailsByMap(Map<String, Object> params);
}
