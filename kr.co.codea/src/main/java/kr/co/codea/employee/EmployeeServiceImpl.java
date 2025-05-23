package kr.co.codea.employee;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kr.co.codea.auth.dto.UserDetailsDto;
import kr.co.codea.auth.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
	private final EmpManagementMapper empManagementMapper;
	private final UserDetailsServiceImpl userDetailsServiceImpl;
	@Override
	public List<EmployeeDto> getAllEmployeesForList() {
		// TODO Auto-generated method stub
		return empManagementMapper.findAllEmployeesForList();
	}
	
	@Override
    public Optional<EmployeeDto> getEmployeeById(Long empId) {
        // EmpManagementMapper에 findEmployeeById 메서드가 정의되어 있다고 가정
        return empManagementMapper.findEmployeeById(empId);
    }

	 @Override
	    public Optional<EmployeeDetailViewModel> getEmployeeDetailForView(Long empId) {
	        Optional<EmployeeDto> generalInfoOpt = empManagementMapper.findEmployeeById(empId); // (findEmployeeById 호출 가정)

	        if (generalInfoOpt.isEmpty()) {
	            return Optional.empty(); // 일반 정보가 없으면 ViewModel 생성 불가
	        }

	        EmployeeDto generalInfo = generalInfoOpt.get();
	        UserDetailsDto accountInfo = null; // 기본값 null

	        try {
	            // UserDetailsServiceImpl을 사용하여 UserDetailsDto (계정 정보 포함) 조회
	            UserDetails userDetails = userDetailsServiceImpl.loadUserByEmpId(empId); //
	            if (userDetails instanceof UserDetailsDto) {
	                accountInfo = (UserDetailsDto) userDetails;
	            }
	        } catch (UsernameNotFoundException e) {
	            // 계정 정보가 없는 경우 accountInfo는 null로 유지됨
	            // log.warn("Employee ID {} has no associated user account.", empId); // 필요시 로그
	        }

	        EmployeeDetailViewModel viewModel = new EmployeeDetailViewModel(generalInfo, accountInfo);
	        return Optional.of(viewModel);
	    }

	@Override
	public void updateEmployee(EmployeeDto employeeDto, String adminCode, Boolean empStatus,
			Authentication currentUser) {
		
	}

}
