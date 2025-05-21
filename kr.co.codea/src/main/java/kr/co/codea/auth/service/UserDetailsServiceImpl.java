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
 * Spring Security의 UserDetailsService 인터페이스 구현체입니다.
 * EmployeeMapper를 통해 직원(사용자) 정보를 조회하여 인증 및 권한 부여에 필요한 UserDetails 객체를 생성합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * 직원 정보에 접근하기 위한 MyBatis 매퍼 인터페이스입니다.
     * {@link RequiredArgsConstructor}에 의해 생성자 주입됩니다.
     */
    private final EmployeeMapper employeeMapper;

    /**
     * Spring Security의 UserDetailsService 인터페이스를 구현한 메소드입니다.
     * 사용자명(여기서는 loginId, 즉 EMP_USER_ID)을 기반으로 사용자 정보를 데이터베이스에서 조회합니다.
     * 조회된 사용자 정보를 바탕으로 UserDetails 객체를 생성하여 반환합니다.
     * 사용자의 권한 정보(authorities)는 ADMIN_CODE 값을 기준으로 동적으로 부여되며,
     * 권한이 없거나 비어있을 경우 기본 "ROLE_USER" 권한을 부여합니다.
     * 계정 활성화 상태는 EMP_STATUS 값을 사용합니다.
     *
     * @param loginId 사용자의 로그인 ID (데이터베이스의 EMP_USER_ID 컬럼에 해당).
     * @return Spring Security가 사용하는 사용자 상세 정보 객체 (UserDetails).
     * @throws UsernameNotFoundException 해당 loginId를 가진 사용자를 찾을 수 없을 경우 발생합니다.
     */
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        log.info("UserDetailsServiceImpl: Attempting to load user by loginId (EMP_USER_ID): {}", loginId);

        // 데이터베이스에서 loginId를 기준으로 사용자 정보를 조회합니다.
        // UserDetailsDto 형태로 반환되며, Optional로 감싸져 있어 null 처리를 용이하게 합니다.
        // EmployeeMapper.xml에서는 EMP_ID as empId, EMP_USER_ID as empUserId, EMP_NAME as empName,
        // EMP_PW as empPw, EMP_STATUS as empStatus, ADMIN_CODE as adminCode 로 매핑되어야 합니다.
        UserDetailsDto userFromDb = employeeMapper.findByLoginId(loginId)
                .orElseThrow(() -> { // 사용자가 존재하지 않을 경우 예외를 발생시킵니다.
                    log.warn("UserDetailsServiceImpl: User not found with loginId (EMP_USER_ID): {}", loginId);
                    return new UsernameNotFoundException("User not found with loginId (EMP_USER_ID): " + loginId);
                });

        // 디버깅 로그: DB에서 가져온 UserDetailsDto 객체의 필드 값들을 확인
        log.info("UserDetailsServiceImpl - User fetched from DB: empId={}, empUserId={}, empName={}, empStatus={}, adminCode={}",
                userFromDb.getEmpId(),
                userFromDb.getEmpUserId(),   // ✨ getUsername() 대신 getEmpUserId() 사용 ✨
                userFromDb.getEmpName(),
                userFromDb.isEmpStatus(),    // ✨ isEnabled() 대신 isEmpStatus() 또는 getEmpStatus() 사용 (boolean getter 규칙) ✨
                userFromDb.getAdminCode()
        );

        // 권한 설정 로직
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 모든 사용자는 기본적으로 ROLE_USER 권한을 가짐

        // ADMIN_CODE 값에 따라 ROLE_ADMIN 권한 추가
        if (StringUtils.hasText(userFromDb.getAdminCode()) && "ADMIN".equalsIgnoreCase(userFromDb.getAdminCode())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            log.info("UserDetailsServiceImpl: User {} granted ROLE_ADMIN based on ADMIN_CODE: {}", userFromDb.getEmpUserId(), userFromDb.getAdminCode()); // ✨ getUsername() 대신 getEmpUserId() 사용 ✨
        }

        if (authorities.isEmpty()) {
            log.warn("UserDetailsServiceImpl: Authorities list became empty for user {} after processing, granting default ROLE_USER.", userFromDb.getEmpUserId()); // ✨ getUsername() 대신 getEmpUserId() 사용 ✨
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        log.info("UserDetailsServiceImpl: Final authorities for user {}: {}", userFromDb.getEmpUserId(), authorities); // ✨ getUsername() 대신 getEmpUserId() 사용 ✨

        // 최종 UserDetailsDto 객체 생성
        return UserDetailsDto.builder()
                .empId(userFromDb.getEmpId())
                .empUserId(userFromDb.getEmpUserId())   // ✨ username 대신 empUserId 필드에 설정 ✨
                .empName(userFromDb.getEmpName())
                .empPw(userFromDb.getEmpPw())           // ✨ password 대신 empPw 필드에 설정 ✨
                .authorities(authorities)
                .empStatus(userFromDb.isEmpStatus())    // ✨ enabled 대신 empStatus 필드에 설정 (boolean getter 규칙) ✨
                .adminCode(userFromDb.getAdminCode())   // adminCode도 명시적으로 전달 (UserDetailsDto 빌더에 해당 필드가 있다면)
                .build();
    }

    /**
     * 직원 고유 ID(EMP_ID)를 기반으로 사용자 정보를 데이터베이스에서 조회합니다.
     * (이하 동일한 패턴으로 수정)
     */
    public UserDetails loadUserByEmpId(Long empId) throws UsernameNotFoundException {
        log.info("UserDetailsServiceImpl: Attempting to load user by EMP_ID: {}", empId);
        UserDetailsDto userFromDb = employeeMapper.findByEmpId(empId)
                .orElseThrow(() -> {
                    log.warn("UserDetailsServiceImpl: User not found with EMP_ID: {}", empId);
                    return new UsernameNotFoundException("User not found with EMP_ID: " + empId);
                });

        log.info("UserDetailsServiceImpl - User fetched from DB (by empId): empId={}, empUserId={}, empName={}, empStatus={}, adminCode={}",
                userFromDb.getEmpId(),
                userFromDb.getEmpUserId(),   // ✨
                userFromDb.getEmpName(),
                userFromDb.isEmpStatus(),    // ✨
                userFromDb.getAdminCode()
        );

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        if (StringUtils.hasText(userFromDb.getAdminCode()) && "ADMIN".equalsIgnoreCase(userFromDb.getAdminCode())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        log.info("UserDetailsServiceImpl: Final authorities for user {} (by empId): {}", userFromDb.getEmpUserId(), authorities); // ✨

        return UserDetailsDto.builder()
                .empId(userFromDb.getEmpId())
                .empUserId(userFromDb.getEmpUserId())   // ✨
                .empName(userFromDb.getEmpName())
                .empPw(userFromDb.getEmpPw())           // ✨
                .authorities(authorities)
                .empStatus(userFromDb.isEmpStatus())    // ✨
                .adminCode(userFromDb.getAdminCode())
                .build();
    }
}