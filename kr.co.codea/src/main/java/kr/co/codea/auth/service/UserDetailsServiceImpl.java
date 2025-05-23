package kr.co.codea.auth.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import kr.co.codea.auth.dto.UserDetailsDto;
import kr.co.codea.auth.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security의 UserDetailsService 구현체
 * - DB에서 사용자 정보를 조회하여 UserDetailsDto로 반환
 * - 권한 부여는 ADMIN_CODE 기준으로 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final EmployeeMapper employeeMapper;

    /**
     * 로그인 ID(EMP_USER_ID)로 사용자 정보 조회
     */
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        // log.info("Attempting to load user by loginId (EMP_USER_ID): {}", loginId);

        UserDetailsDto userFromDb = employeeMapper.findByLoginId(loginId)
                .orElseThrow(() -> {
                    // log.warn("User not found with loginId (EMP_USER_ID): {}", loginId);
                    return new UsernameNotFoundException("User not found with loginId (EMP_USER_ID): " + loginId);
                });

        // log.info("User fetched from DB: empId={}, empUserId={}, empName={}, empStatus={}, adminCode={}",
        //         userFromDb.getEmpId(), userFromDb.getEmpUserId(), userFromDb.getEmpName(),
        //         userFromDb.isEmpStatus(), userFromDb.getAdminCode());

        // 권한 설정 (기본: ROLE_USER, 추가: ROLE_ADMIN)
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        if (StringUtils.hasText(userFromDb.getAdminCode()) && "ADMIN".equalsIgnoreCase(userFromDb.getAdminCode())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            // log.info("User {} granted ROLE_ADMIN based on ADMIN_CODE: {}", userFromDb.getEmpUserId(), userFromDb.getAdminCode());
        }

        if (authorities.isEmpty()) {
            // log.warn("Authorities list empty for user {}. Granting default ROLE_USER.", userFromDb.getEmpUserId());
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // log.info("Final authorities for user {}: {}", userFromDb.getEmpUserId(), authorities);

        return UserDetailsDto.builder()
                .empId(userFromDb.getEmpId())
                .empUserId(userFromDb.getEmpUserId())
                .empName(userFromDb.getEmpName())
                .empPw(userFromDb.getEmpPw())
                .authorities(authorities)
                .empStatus(userFromDb.isEmpStatus())
                .adminCode(userFromDb.getAdminCode())
                .build();
    }

    /**
     * 사원 번호(EMP_ID)로 사용자 정보 조회
     */
    public UserDetails loadUserByEmpId(Long empId) throws UsernameNotFoundException {
        // log.info("Attempting to load user by EMP_ID: {}", empId);

        UserDetailsDto userFromDb = employeeMapper.findByEmpId(empId)
                .orElseThrow(() -> {
                    // log.warn("User not found with EMP_ID: {}", empId);
                    return new UsernameNotFoundException("User not found with EMP_ID: " + empId);
                });

        // log.info("User fetched from DB (by empId): empId={}, empUserId={}, empName={}, empStatus={}, adminCode={}",
        //         userFromDb.getEmpId(), userFromDb.getEmpUserId(), userFromDb.getEmpName(),
        //         userFromDb.isEmpStatus(), userFromDb.getAdminCode());

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        if (StringUtils.hasText(userFromDb.getAdminCode()) && "ADMIN".equalsIgnoreCase(userFromDb.getAdminCode())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        // log.info("Final authorities for user {} (by empId): {}", userFromDb.getEmpUserId(), authorities);

        return UserDetailsDto.builder()
                .empId(userFromDb.getEmpId())
                .empUserId(userFromDb.getEmpUserId())
                .empName(userFromDb.getEmpName())
                .empPw(userFromDb.getEmpPw())
                .authorities(authorities)
                .empStatus(userFromDb.isEmpStatus())
                .adminCode(userFromDb.getAdminCode())
                .build();
    }
}
