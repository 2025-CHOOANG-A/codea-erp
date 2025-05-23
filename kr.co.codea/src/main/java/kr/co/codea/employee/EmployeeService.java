package kr.co.codea.employee;

import kr.co.codea.employee.EmployeeDto; // EmployeeDto 임포트

import java.util.List;

public interface EmployeeService {

    List<EmployeeDto> getAllEmployeesForList();
}