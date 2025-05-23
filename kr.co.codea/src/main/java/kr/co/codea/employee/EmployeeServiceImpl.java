package kr.co.codea.employee;

import java.util.List;

import org.springframework.stereotype.Service;
import kr.co.codea.employee.EmpManagementMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
	private final EmpManagementMapper empManagementMapper;
	@Override
	public List<EmployeeDto> getAllEmployeesForList() {
		// TODO Auto-generated method stub
		return empManagementMapper.findAllEmployeesForList();
	}

}
