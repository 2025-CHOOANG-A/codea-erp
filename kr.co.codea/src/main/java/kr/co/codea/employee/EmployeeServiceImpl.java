package kr.co.codea.employee;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import kr.co.codea.auth.dto.UserDetailsDto;
import kr.co.codea.auth.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사원 관리 서비스를 구현한 클래스입니다. 등록, 조회, 수정, 삭제 등 비즈니스 로직을 수행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmpManagementMapper empManagementMapper;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final PasswordEncoder passwordEncoder;

    /**
     * 전체 사원 목록을 조회합니다.
     *
     * @return 사원 DTO 리스트
     */
    @Override
    public List<EmployeeDto> getAllEmployeesForList() {
        return empManagementMapper.findAllEmployeesForList();
    }

    /**
     * 사원 ID로 기본 정보를 조회합니다.
     *
     * @param empId 사원 ID
     * @return 사원 정보 DTO (Optional)
     */
    @Override
    public Optional<EmployeeDto> getEmployeeById(Long empId) {
        return empManagementMapper.findEmployeeById(empId);
    }

    /**
     * 사원 상세 정보를 조회합니다 (기본 정보 + 계정 정보 포함).
     *
     * @param empId 사원 ID
     * @return 사원 상세 정보 뷰 모델 (Optional)
     */
    @Override
    public Optional<EmployeeDetailViewModel> getEmployeeDetailForView(Long empId) {
        Optional<EmployeeDto> generalInfoOpt = empManagementMapper.findEmployeeById(empId);
        if (generalInfoOpt.isEmpty())
            return Optional.empty();

        EmployeeDto generalInfo = generalInfoOpt.get();
        UserDetailsDto accountInfo = null;

        try {
            UserDetails userDetails = userDetailsServiceImpl.loadUserByEmpId(empId);
            if (userDetails instanceof UserDetailsDto) {
                accountInfo = (UserDetailsDto) userDetails;
            }
        } catch (UsernameNotFoundException ignored) {
        }

        return Optional.of(new EmployeeDetailViewModel(generalInfo, accountInfo));
    }

    /**
     * 사원 정보를 수정합니다 (비밀번호 포함 가능).
     *
     * @param employeeDto 수정 대상 사원 정보
     * @param currentUser 현재 로그인한 사용자 인증 정보
     */
    @Transactional
    @Override
    public void updateEmployee(EmployeeDto employeeDto, org.springframework.security.core.Authentication currentUser) {
        log.info("사원 정보 업데이트 시도: empId={}", employeeDto.getEmpId());

        int updatedRows = empManagementMapper.updateEmployee(employeeDto);

        if (updatedRows == 0) {
            empManagementMapper.findEmployeeById(employeeDto.getEmpId())
                    .orElseThrow(() -> new RuntimeException("수정할 사원을 찾을 수 없습니다. ID: " + employeeDto.getEmpId()));
            log.warn("사원 정보 업데이트 실패");
        } else {
            log.info("사원 정보 업데이트 완료");
        }
    }

    /**
     * 비밀번호를 제외한 사원 정보를 수정합니다.
     *
     * @param employeeDto 수정할 사원 정보
     * @return 수정된 사원 정보
     */
    @Transactional
    @Override
    public EmployeeDto updateEmployeeNonPassword(EmployeeDto employeeDto) {
        Long empId = employeeDto.getEmpId();

        EmployeeDto existing = empManagementMapper.findEmployeeById(empId)
                .orElseThrow(() -> new RuntimeException("사원 정보 없음"));

        employeeDto.setEmpPw(existing.getEmpPw()); // 기존 비밀번호 유지
        int updatedRows = empManagementMapper.updateEmployee(employeeDto);

        if (updatedRows == 0) {
            throw new RuntimeException("사원 정보 수정 실패");
        }

        return empManagementMapper.findEmployeeById(empId).orElseThrow(() -> new RuntimeException("수정 후 사원 정보 없음"));
    }

    /**
     * 사원을 삭제합니다.
     *
     * @param empId 삭제할 사원의 ID
     */
    @Transactional
    @Override
    public void deleteEmployee(Long empId) {
        log.info("사원 삭제 시도: empId={}", empId);
        int affectedRows = empManagementMapper.deleteEmployeeById(empId);

        if (affectedRows == 0) {
            throw new RuntimeException("삭제할 사원을 찾을 수 없거나 이미 삭제됨");
        }

        log.info("사원 삭제 완료");
    }

    /**
     * 신규 사원을 등록합니다. (사번, 이메일 중복 확인 및 비밀번호 암호화 포함)
     *
     * @param employeeDto 등록할 사원 정보
     * @return 등록된 사원 정보
     */
    @Transactional
    @Override
    public EmployeeDto registerEmployee(@Valid EmployeeDto employeeDto) throws Exception {
        // 1. 사번 자동 생성
        String generatedEmpNo = generateEmployeeId();
        employeeDto.setEmpNo(generatedEmpNo);
        log.info("신규 사원 등록 시도: {}", generatedEmpNo);

        // 2. 중복 검사
        empManagementMapper.findByEmpNo(employeeDto.getEmpNo()).ifPresent(e -> {
            throw new IllegalArgumentException("중복된 사번: " + employeeDto.getEmpNo());
        });

        empManagementMapper.findByEmail(employeeDto.getEmail()).ifPresent(e -> {
            throw new IllegalArgumentException("중복된 이메일: " + employeeDto.getEmail());
        });

        // 3. 비밀번호 암호화
        employeeDto.setEmpPw(passwordEncoder.encode(employeeDto.getEmpPw()));

        // 4. DB 저장
        empManagementMapper.insertEmployee(employeeDto);
        log.info("사원 등록 완료: empId={}, empNo={}", employeeDto.getEmpId(), employeeDto.getEmpNo());

        return employeeDto;
    }


    // 사원 번호 생성 메서드
    @Override
    public String generateEmployeeId() {
        String lastEmployeeId = empManagementMapper.findLastEmployeeId();

        int newNumber;
        if (lastEmployeeId == null || !lastEmployeeId.matches("E\\d+")) {
            newNumber = 1001;
        } else {
            newNumber = Integer.parseInt(lastEmployeeId.substring(1)) + 1;
        }

        String newEmpNo;
        do {
            newEmpNo = "E" + newNumber++;
        } while (empManagementMapper.findByEmpNo(newEmpNo).isPresent());

        return newEmpNo;
    }
}
