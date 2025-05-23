package kr.co.codea.employee;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.co.codea.employee.EmployeeDto;

@Mapper
public interface EmpManagementMapper {
	List<EmployeeDto> findAllEmployeesForList();
}
