package kr.co.codea.employee;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
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
    private final PasswordEncoder passwordEncoder;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 전체 사원 목록 조회
    @Override
    public List<EmployeeDto> getAllEmployeesForList() {
        return empManagementMapper.findAllEmployeesForList();
    }

    // 사원 ID로 기본 정보 조회
    @Override
    public Optional<EmployeeDto> getEmployeeById(Long empId) {
        return empManagementMapper.findEmployeeById(empId);
    }

    // 사원 상세 보기용 정보 + 계정 정보 조회
    @Override
    public Optional<EmployeeDetailViewModel> getEmployeeDetailForView(Long empId) {
        Optional<EmployeeDto> generalInfoOpt = empManagementMapper.findEmployeeById(empId);
        if (generalInfoOpt.isEmpty()) return Optional.empty();

        EmployeeDto generalInfo = generalInfoOpt.get();
        UserDetailsDto accountInfo = null;
        try {
            UserDetails userDetails = userDetailsServiceImpl.loadUserByEmpId(empId);
            if (userDetails instanceof UserDetailsDto) {
                accountInfo = (UserDetailsDto) userDetails;
            }
        } catch (UsernameNotFoundException ignored) {}

        return Optional.of(new EmployeeDetailViewModel(generalInfo, accountInfo));
    }

    // 사원 정보 수정 (비밀번호 포함 가능)
    @Transactional
    @Override
    public void updateEmployee(EmployeeDto employeeDto, org.springframework.security.core.Authentication currentUser) {
        log.info("사원 정보 업데이트 시도: empId={}", employeeDto.getEmpId());

        if (employeeDto.getEmpPw() != null && !employeeDto.getEmpPw().isEmpty()) {
            log.info("비밀번호 변경 예정");
        } else {
            log.info("비밀번호 변경 없음");
        }

        int updatedRows = empManagementMapper.updateEmployee(employeeDto);

        if (updatedRows == 0) {
            empManagementMapper.findEmployeeById(employeeDto.getEmpId())
                .orElseThrow(() -> new RuntimeException("수정할 사원을 찾을 수 없습니다. ID: " + employeeDto.getEmpId()));
            log.warn("사원 정보 업데이트 실패");
        } else {
            log.info("사원 정보 업데이트 완료");
        }
    }

    // 비밀번호 제외 사원 정보 수정
    @Transactional
    @Override
    public EmployeeDto updateEmployeeNonPassword(EmployeeDto employeeDto) {
        Long empId = employeeDto.getEmpId();
        EmployeeDto existing = empManagementMapper.findEmployeeById(empId)
            .orElseThrow(() -> new RuntimeException("사원 정보 없음"));

        employeeDto.setEmpPw(existing.getEmpPw());
        int updatedRows = empManagementMapper.updateEmployee(employeeDto);
        if (updatedRows == 0) {
            throw new RuntimeException("사원 정보 수정 실패");
        }
        return empManagementMapper.findEmployeeById(empId)
            .orElseThrow(() -> new RuntimeException("수정 후 사원 정보 없음"));
    }

    // 사원 삭제
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

    // 사원 등록 처리 (중복 확인, 이미지 저장, 비밀번호 암호화 포함)
    @Transactional
    @Override
    public EmployeeDto registerEmployee(@Valid EmployeeDto employeeDto) throws Exception {
        log.info("신규 사원 등록 시도: {}", employeeDto.getEmpNo());

        empManagementMapper.findByEmpNo(employeeDto.getEmpNo()).ifPresent(e -> {
            throw new IllegalArgumentException("중복된 사번: " + employeeDto.getEmpNo());
        });
        empManagementMapper.findByEmail(employeeDto.getEmail()).ifPresent(e -> {
            throw new IllegalArgumentException("중복된 이메일: " + employeeDto.getEmail());
        });

        MultipartFile profileImgFile = employeeDto.getProfileImgFile();
        if (profileImgFile != null && !profileImgFile.isEmpty()) {
            String originalFileName = profileImgFile.getOriginalFilename();
            String storedFileName = UUID.randomUUID() + "_" + originalFileName;
            Path destinationPath = Paths.get(uploadDir + File.separator + storedFileName);

            try {
                Files.createDirectories(destinationPath.getParent());
                profileImgFile.transferTo(destinationPath);
                employeeDto.setEmpImg("/uploads/" + storedFileName);
                log.info("프로필 이미지 저장 완료");
            } catch (IOException e) {
                log.error("이미지 저장 실패", e);
                throw new RuntimeException("프로필 이미지 업로드 실패", e);
            }
        } else {
            employeeDto.setEmpImg(null); // 또는 기본 이미지 경로 설정 가능
        }

        employeeDto.setEmpPw(passwordEncoder.encode(employeeDto.getEmpPw()));
        empManagementMapper.insertEmployee(employeeDto);
        log.info("사원 등록 완료: empId={}", employeeDto.getEmpId());
        return employeeDto;
    }
}
