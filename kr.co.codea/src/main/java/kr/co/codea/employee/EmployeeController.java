package kr.co.codea.employee;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/employee") // 기본 경로를 "/employee"로 설정
@RequiredArgsConstructor
public class EmployeeController {
	private final EmployeeService employeeService; 

    @GetMapping
    public String listEmployees(Model model) {
    	List<EmployeeDto> employees = employeeService.getAllEmployeesForList();
    	 model.addAttribute("employees", employees);
        return "employee/employee_list";
    }

    @GetMapping("/new")
    //@PreAuthorize("hasRole('ROLE_ADMIN')") //ADMIN 역할만 이 메소드 호출 가능
    public String showRegistrationForm(Model model) {

        return "employee/employee_write";
    }

    @PostMapping("/register")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String registerEmployee() {
    	return "employee/employee_write";
    }

    /* 사원 상세 정보 조회 */
    @GetMapping("/{empId}")
    public String viewEmployee() {
        return "employee/employee_detail";
    }

    @GetMapping("/{empId}/edit")
    // @PreAuthorize("hasRole('ROLE_ADMIN') or #empId == authentication.principal.empId")
    public String showEditForm() {
    	return "employee/employee_modify";
    }

    @PostMapping("/{empId}/edit")
    // @PreAuthorize("hasRole('ROLE_ADMIN') or #empId == authentication.principal.empId")
    public String updateEmployee() {
    	return "employee/employee_modify";
    }

    // 사원 삭제
    @PostMapping("/{empId}/delete")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteEmployee() {
        return "redirect:/employee";
    }
}