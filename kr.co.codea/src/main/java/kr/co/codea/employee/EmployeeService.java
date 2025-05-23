package kr.co.codea.employee;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Authentication;

public interface EmployeeService {

    List<EmployeeDto> getAllEmployeesForList();
    
    Optional<EmployeeDto> getEmployeeById(Long empId);
    
    Optional<EmployeeDetailViewModel> getEmployeeDetailForView(Long empId);
    
    void updateEmployee(EmployeeDto employeeDto, String adminCode, Boolean empStatus, Authentication currentUser);
}