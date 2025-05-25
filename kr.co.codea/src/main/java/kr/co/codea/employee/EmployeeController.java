package kr.co.codea.employee;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import kr.co.codea.auth.dto.UserDetailsDto;

@Slf4j
@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /** 사원 목록 조회 */
    @GetMapping
    public String listEmployees(Model model) {
        List<EmployeeDto> employees = employeeService.getAllEmployeesForList();
        model.addAttribute("employees", employees);
        return "employee/employee_list";
    }

    /** 신규 사원 등록 폼 요청 */
    @GetMapping("/new")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String showRegistrationForm(Model model) {
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setEmpStatus(true); // 기본값 설정
        model.addAttribute("employeeDto", employeeDto);
        return "employee/employee_write";
    }

    /** 신규 사원 등록 처리 */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String registerEmployee(@Valid @ModelAttribute("employeeDto") EmployeeDto employeeDto,
                                   BindingResult bindingResult,
                                   @RequestParam("confirmPassword") String confirmPassword,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {

        // 비밀번호 확인 일치 검사
        if (employeeDto.getEmpPw() != null && !employeeDto.getEmpPw().equals(confirmPassword)) {
            bindingResult.rejectValue("empPw", "password.mismatch", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("employeeDto", employeeDto);
            if (bindingResult.hasGlobalErrors()) {
                model.addAttribute("errorMessage", bindingResult.getGlobalError().getDefaultMessage());
            }
            return "employee/employee_write";
        }

        try {
            employeeService.registerEmployee(employeeDto);
            redirectAttributes.addFlashAttribute("successMessage", "사원(사번: " + employeeDto.getEmpNo() + ") 등록 완료");
            return "redirect:/employee";
        } catch (IllegalArgumentException e) {
            model.addAttribute("employeeDto", employeeDto);
            model.addAttribute("errorMessage", e.getMessage());
            return "employee/employee_write";
        } catch (Exception e) {
            model.addAttribute("employeeDto", employeeDto);
            model.addAttribute("errorMessage", "사원 등록 중 오류 발생: " + e.getMessage());
            return "employee/employee_write";
        }
    }

    /** 사원 상세 정보 조회 */
    @GetMapping("/{empId}")
    public String viewEmployee(@PathVariable("empId") Long empId, Model model, RedirectAttributes redirectAttributes) {
        Optional<EmployeeDetailViewModel> viewModelOpt = employeeService.getEmployeeDetailForView(empId);

        return viewModelOpt.map(viewModel -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean canEdit = viewModel.isEditableBy(authentication);
            model.addAttribute("canEdit", canEdit);
            model.addAttribute("employeeView", viewModel);
            return "employee/employee_detail";
        }).orElse("redirect:/employee");
    }

    /** 사원 정보 수정 폼 요청 */
    @GetMapping("/{empId}/edit")
    public String showEditForm(@PathVariable("empId") Long empId,
                               Model model,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        Optional<EmployeeDetailViewModel> viewModelOpt = employeeService.getEmployeeDetailForView(empId);
        if (viewModelOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "사원 정보를 찾을 수 없습니다. (ID: " + empId + ")");
            return "redirect:/employee";
        }

        EmployeeDetailViewModel employeeView = viewModelOpt.get();
        EmployeeDto formBackingDto = new EmployeeDto();

        if (employeeView.getGeneralInfo() != null) {
            formBackingDto = employeeView.getGeneralInfo(); // 필요한 값만 복사
        }

        model.addAttribute("employeeDto", formBackingDto);
        model.addAttribute("currentAdminCode", employeeView.getAccountInfo() != null ? employeeView.getAccountInfo().getAdminCode() : "");
        model.addAttribute("currentEmpStatus", employeeView.getAccountInfo() != null && employeeView.getAccountInfo().isEnabled());

        boolean isAdmin = false;
        boolean isEditingSelf = false;
        if (authentication != null && authentication.isAuthenticated()) {
            isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsDto currentUser) {
                isEditingSelf = currentUser.getEmpId() != null && currentUser.getEmpId().equals(empId);
            }
        }

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isEditingSelf", isEditingSelf);
        model.addAttribute("pageTitle", (formBackingDto.getEmpName() != null ? formBackingDto.getEmpName() : "사원") + " 정보 수정");

        return "employee/employee_modify";
    }

    /** 사원 정보 수정 처리 */
    @PostMapping("/{empId}/edit")
    public String updateEmployee(@PathVariable("empId") Long empId,
                                 @ModelAttribute("employeeDto") EmployeeDto employeeDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model,
                                 @RequestParam(value = "adminCode", required = false) String adminCode,
                                 @RequestParam(value = "empStatus", required = false) String empStatusStr,
                                 @RequestParam(value = "newPassword", required = false) String newPassword,
                                 @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                 Authentication authentication) {

        employeeDto.setEmpId(empId); // ID 명시적 설정

        if (bindingResult.hasErrors()) {
            populateEditFormModel(empId, model, authentication, employeeDto);
            model.addAttribute("errorMessage", "입력 값에 오류가 있습니다.");
            return "employee/employee_modify";
        }

        try {
            boolean isAdmin = false;
            boolean isEditingSelf = false;
            Long currentUserId = null;

            if (authentication != null && authentication.isAuthenticated()) {
                isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetailsDto user) {
                    currentUserId = user.getEmpId();
                    isEditingSelf = currentUserId != null && currentUserId.equals(empId);
                }
            }

            // 관리자만 adminCode 및 empStatus 변경 가능
            if (isAdmin) {
                employeeDto.setAdminRoleUpdateIntent(true);
                employeeDto.setAdminCode((adminCode != null && !adminCode.isEmpty()) ? adminCode : null);
                if (empStatusStr != null) {
                    employeeDto.setEmpStatus(Boolean.parseBoolean(empStatusStr));
                }
            } else {
                employeeDto.setAdminRoleUpdateIntent(false);
            }

            // 비밀번호 변경 조건 확인
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (isAdmin || isEditingSelf) {
                    if (!newPassword.equals(confirmPassword)) {
                        populateEditFormModel(empId, model, authentication, employeeDto);
                        model.addAttribute("errorMessage", "새 비밀번호와 확인이 일치하지 않습니다.");
                        return "employee/employee_modify";
                    }
                    if (newPassword.length() < 8) {
                        populateEditFormModel(empId, model, authentication, employeeDto);
                        model.addAttribute("errorMessage", "새 비밀번호는 최소 8자 이상이어야 합니다.");
                        return "employee/employee_modify";
                    }

                    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                    employeeDto.setEmpPw(passwordEncoder.encode(newPassword));
                } else {
                    employeeDto.setEmpPw(null); // 권한 없음 → 비밀번호 변경 무시
                }
            } else {
                employeeDto.setEmpPw(null); // 변경 없음 → null
            }

            employeeService.updateEmployee(employeeDto, authentication);
            redirectAttributes.addFlashAttribute("successMessage", "사원 정보가 성공적으로 수정되었습니다.");
            return "redirect:/employee/" + empId;

        } catch (Exception e) {
            populateEditFormModel(empId, model, authentication, employeeDto);
            model.addAttribute("errorMessage", "사원 정보 수정 중 오류 발생: " + e.getMessage());
            return "employee/employee_modify";
        }
    }

    /** 수정 폼 렌더링 시 필요한 모델 속성 설정용 헬퍼 */
    private void populateEditFormModel(Long empId, Model model, Authentication authentication, EmployeeDto submittedDto) {
        model.addAttribute("employeeDto", submittedDto);

        boolean isAdmin = false;
        boolean isEditingSelf = false;

        if (authentication != null && authentication.isAuthenticated()) {
            isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsDto user) {
                isEditingSelf = user.getEmpId() != null && user.getEmpId().equals(empId);
            }
        }

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isEditingSelf", isEditingSelf);
        model.addAttribute("pageTitle", (submittedDto.getEmpName() != null ? submittedDto.getEmpName() : "사원") + " 정보 수정");

        Optional<EmployeeDetailViewModel> viewModelOpt = employeeService.getEmployeeDetailForView(empId);
        if (viewModelOpt.isPresent()) {
            EmployeeDetailViewModel viewModel = viewModelOpt.get();
            model.addAttribute("currentAdminCode", viewModel.getAccountInfo() != null ? viewModel.getAccountInfo().getAdminCode() : "");
            model.addAttribute("currentEmpStatus", viewModel.getAccountInfo() != null && viewModel.getAccountInfo().isEnabled());

            if (submittedDto.getEmpImg() == null && viewModel.getGeneralInfo() != null) {
                submittedDto.setEmpImg(viewModel.getGeneralInfo().getEmpImg());
            }
        } else {
            model.addAttribute("currentAdminCode", "");
            model.addAttribute("currentEmpStatus", true);
        }
    }

    /** 사원 삭제 처리 */
    @PostMapping("/{empId}/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteEmployee(@PathVariable("empId") Long empId,
                                 RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteEmployee(empId);
            redirectAttributes.addFlashAttribute("successMessage", "사원 정보(ID: " + empId + ") 삭제 완료");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "사원 삭제 중 오류 발생");
        }
        return "redirect:/employee";
    }
}
