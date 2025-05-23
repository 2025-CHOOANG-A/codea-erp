package kr.co.codea.employee;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.co.codea.auth.dto.UserDetailsDto;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // 필요시 추가
import org.springframework.security.core.context.SecurityContextHolder; // 필요시 추가

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
    public String viewEmployee(@PathVariable("empId") Long empId, Model model, RedirectAttributes redirectAttributes) {
        log.info("EmployeeController: GET /employee/{} (viewEmployee) called", empId);

        Optional<EmployeeDetailViewModel> viewModelOpt = employeeService.getEmployeeDetailForView(empId);
        log.info("EmployeeController: employeeService.getEmployeeDetailForView({}) returned: {}", empId, viewModelOpt.isPresent() ? "ViewModel Present" : "Optional Empty");

        return viewModelOpt.map(viewModel -> { // viewModel은 EmployeeDetailViewModel 타입
            log.info("EmployeeController: ViewModel is present. generalInfo: {}, accountInfo: {}",
                    (viewModel.getGeneralInfo() != null ? "Present" : "null"),
                    (viewModel.getAccountInfo() != null ? "Present" : "null"));

            // ViewModel의 isEditableBy 메서드를 호출하여 수정 가능 여부 판단
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean canEdit = viewModel.isEditableBy(authentication);
            model.addAttribute("canEdit", canEdit);
            log.info("EmployeeController: 수정 가능 여부 (canEdit) from ViewModel: {}", canEdit);

            // --- 나머지 모델 설정 로직은 동일 ---
            if (viewModel.getGeneralInfo() != null) {
                log.info("EmployeeController: viewModel.generalInfo.empId = {}", viewModel.getGeneralInfo().getEmpId());
                log.info("EmployeeController: viewModel.generalInfo.empName = {}", viewModel.getGeneralInfo().getEmpName());
            }
            // ... (accountInfo 로그 등) ...
            model.addAttribute("employeeView", viewModel); // employeeView는 계속 모델에 추가
            // ... (pageTitle 설정 등) ...

            return "employee/employee_detail";
        }).orElseGet(() -> {
            // ... (기존 redirect 로직) ...
            return "redirect:/employee";
        });
    }

    @GetMapping("/{empId}/edit")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (#empId == authentication.principal.empId)")
    public String showEditForm(@PathVariable("empId") Long empId, Model model, Authentication authentication) {
        log.info("EmployeeController: GET /employee/{}/edit (showEditForm) called for empId: {}", empId);

        Optional<EmployeeDetailViewModel> viewModelOpt = employeeService.getEmployeeDetailForView(empId);
        if (viewModelOpt.isEmpty()) {
            log.warn("No EmployeeDetailViewModel found for empId: {}. Redirecting.", empId);
            return "redirect:/employee";
        }
        EmployeeDetailViewModel employeeView = viewModelOpt.get();
        EmployeeDto formBackingDto = new EmployeeDto(); // 새 DTO 인스턴스

        if (employeeView.getGeneralInfo() != null) {
            EmployeeDto generalInfo = employeeView.getGeneralInfo();
            formBackingDto.setEmpId(generalInfo.getEmpId()); // <<--- empId 설정
            formBackingDto.setEmpNo(generalInfo.getEmpNo());
            formBackingDto.setEmpName(generalInfo.getEmpName());
            formBackingDto.setEmail(generalInfo.getEmail());
            formBackingDto.setHp(generalInfo.getHp());
            formBackingDto.setEmpDept(generalInfo.getEmpDept());
            formBackingDto.setEmpPosition(generalInfo.getEmpPosition());
            formBackingDto.setTel(generalInfo.getTel());
            formBackingDto.setEmpImg(generalInfo.getEmpImg()); // 이미지 경로도 설정
        } else {
            // generalInfo가 null인 경우는 서비스 로직상 거의 없지만, 방어적으로 처리
            log.warn("employeeView.generalInfo is null for empId: {}", empId);
            // 이 경우 formBackingDto.empId가 설정되지 않아 문제가 될 수 있습니다.
            // 하지만 "cannot be found on null" 오류는 formBackingDto 자체가 null이라는 의미입니다.
        }

        model.addAttribute("employeeDto", formBackingDto); // 모델에 "employeeDto" 이름으로 추가
        log.info("Added to model - employeeDto: {}", formBackingDto);


        if (employeeView.getAccountInfo() != null) {
            model.addAttribute("currentAdminCode", employeeView.getAccountInfo().getAdminCode());
            model.addAttribute("currentEmpStatus", employeeView.getAccountInfo().isEnabled());
        } else {
            model.addAttribute("currentAdminCode", "");
            model.addAttribute("currentEmpStatus", true);
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                              .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        log.info("Added to model - isAdmin: {}", isAdmin);

        UserDetailsDto currentUserPrincipal = (UserDetailsDto) authentication.getPrincipal();
        boolean isEditingSelf = currentUserPrincipal.getEmpId().equals(empId);
        model.addAttribute("isEditingSelf", isEditingSelf);
        log.info("Added to model - isEditingSelf: {}", isEditingSelf);
        
        // pageTitle은 하드코딩하므로 아래 라인은 제거하거나 주석 처리
        // String pageTitleEmpName = formBackingDto.getEmpName() != null ? formBackingDto.getEmpName() : "사원";
        // model.addAttribute("pageTitle", pageTitleEmpName + " 정보 수정");

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