package kr.co.codea.auth.service;

import kr.co.codea.auth.dto.UserDetailsDto;
import kr.co.codea.auth.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
// import java.util.Optional; // Optional은 orElseThrow에서 사용되므로 명시적 임포트는 필수 아님
import java.util.stream.Collectors;

/**
 * Spring Security의 UserDetailsService 인터페이스 구현체입니다.
 * EmployeeMapper를 통해 직원(사용자) 정보를 조회하여 인증 및 권한 부여에 필요한 UserDetails 객체를 생성합니다.
 * Lombok 어노테이션(@Slf4j, @Service, @RequiredArgsConstructor)을 사용하여 로깅, 서비스 빈 등록, 생성자 주입을 간결하게 처리합니다.
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
     * 사용자의 권한 정보(authorities)는 데이터베이스에서 쉼표로 구분된 문자열로 가져와 파싱하며,
     * 권한이 없거나 비어있을 경우 기본 "ROLE_USER" 권한을 부여합니다.
     *
     * @param loginId 사용자의 로그인 ID (데이터베이스의 EMP_USER_ID 컬럼에 해당).
     * @return Spring Security가 사용하는 사용자 상세 정보 객체 (UserDetails).
     * @throws UsernameNotFoundException 해당 loginId를 가진 사용자를 찾을 수 없을 경우 발생합니다.
     */
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        log.info("Attempting to load user by loginId (EMP_USER_ID): {}", loginId);

        // 데이터베이스에서 loginId를 기준으로 사용자 정보를 조회합니다.
        // UserDetailsDto 형태로 반환되며, Optional로 감싸져 있어 null 처리를 용이하게 합니다.
        UserDetailsDto userFromDb = employeeMapper.findByLoginId(loginId)
                .orElseThrow(() -> { // 사용자가 존재하지 않을 경우 예외를 발생시킵니다.
                    log.warn("User not found with loginId (EMP_USER_ID): {}", loginId);
                    return new UsernameNotFoundException("User not found with loginId (EMP_USER_ID): " + loginId);
                });

     // 디버깅 로그 추가: userFromDb 객체의 필드 값들을 확인
        log.info("UserDetailsServiceImpl - User fetched from DB: empId={}, username={}, enabled={}, rawAuthoritiesString={}",
                userFromDb.getEmpId(),
                userFromDb.getUsername(),
                userFromDb.isEnabled(),
                userFromDb.getRawAuthoritiesString());
        
        Collection<? extends GrantedAuthority> authorities;
        String rawAuthorities = userFromDb.getRawAuthoritiesString(); // DB에서 가져온 쉼표로 구분된 권한 문자열
        log.info("Raw authorities string from DB for user {}: {}", userFromDb.getUsername(), rawAuthorities);

        // 권한 문자열을 파싱하여 GrantedAuthority 컬렉션으로 변환합니다.
        if (rawAuthorities != null && !rawAuthorities.trim().isEmpty()) {
            authorities = Arrays.stream(rawAuthorities.split(",")) // 쉼표로 분리
                                  .map(String::trim) // 각 권한 문자열의 앞뒤 공백 제거
                                  .filter(role -> !role.isEmpty()) // 비어있는 문자열 필터링
                                  .map(SimpleGrantedAuthority::new) // SimpleGrantedAuthority 객체로 변환
                                  .collect(Collectors.toList()); // 리스트로 수집
        } else {
            // 권한 문자열이 없거나 비어있으면 기본 권한 "ROLE_USER"를 부여합니다.
            log.warn("No authorities found for user {}, granting default ROLE_USER.", userFromDb.getUsername());
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // 파싱 후에도 권한 목록이 비어있을 경우 (예: ",," 같은 문자열로 인해 filter 후 비게 되는 경우)
        // 최종적으로 권한이 없는 상태를 방지하기 위해 기본 권한을 부여합니다.
        if (authorities.isEmpty()) {
            log.warn("Authorities list is empty for user {} after processing, granting default ROLE_USER.", userFromDb.getUsername());
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        log.info("Authorities for user {}: {}", userFromDb.getUsername(), authorities);

        // 조회된 사용자 정보와 처리된 권한 정보를 사용하여 UserDetailsDto 객체를 빌더 패턴으로 생성합니다.
        // UserDetails 인터페이스를 구현한 UserDetailsDto 객체를 반환합니다.
        // username 필드에는 EMP_ID가 사용됩니다.
        // password 필드에는 데이터베이스에 저장된 암호화된 비밀번호가 사용됩니다.
        // enabled 필드에는 계정 활성화 상태가 사용됩니다.
        return UserDetailsDto.builder()
        		.empId(userFromDb.getEmpId()) // ***** 이 부분이 누락되었을 가능성이 매우 높습니다! *****
                .username(userFromDb.getUsername())
                .password(userFromDb.getPassword())
                .authorities(authorities)
                .enabled(userFromDb.isEnabled())
                // .rawAuthoritiesString(userFromDb.getRawAuthoritiesString()) // 최종 Principal에는 보통 불필요
                .build();
    }

    /**
     * 직원 고유 ID(EMP_ID)를 기반으로 사용자 정보를 데이터베이스에서 조회합니다.
     * {@link #loadUserByUsername(String)} 메소드와 유사하게 동작하지만, 조회 기준이 EMP_ID입니다.
     * 이 메소드는 UserDetailsService 인터페이스의 표준 메소드는 아니며, 필요에 따라 내부적으로 또는 다른 서비스에서 호출될 수 있습니다.
     * 조회된 사용자 정보를 바탕으로 UserDetails 객체를 생성하여 반환합니다.
     * 사용자의 권한 정보(authorities)는 데이터베이스에서 쉼표로 구분된 문자열로 가져와 파싱하며,
     * 권한이 없거나 비어있을 경우 기본 "ROLE_USER" 권한을 부여합니다.
     *
     * @param empId 직원의 고유 ID (데이터베이스의 EMP_ID 컬럼에 해당).
     * @return Spring Security가 사용하는 사용자 상세 정보 객체 (UserDetails).
     * @throws UsernameNotFoundException 해당 empId를 가진 사용자를 찾을 수 없을 경우 발생합니다.
     */
    public UserDetails loadUserByEmpId(Long empId) throws UsernameNotFoundException {
        log.info("Attempting to load user by EMP_ID: {}", empId);
        // 데이터베이스에서 empId를 기준으로 사용자 정보를 조회합니다.
        UserDetailsDto userFromDb = employeeMapper.findByEmpId(empId)
                .orElseThrow(() -> { // 사용자가 존재하지 않을 경우 예외를 발생시킵니다.
                    log.warn("User not found with EMP_ID: {}", empId);
                    return new UsernameNotFoundException("User not found with EMP_ID: " + empId);
                });

        // 권한 처리 로직 (loadUserByUsername 메소드와 동일)
        Collection<? extends GrantedAuthority> authorities;
        String rawAuthorities = userFromDb.getRawAuthoritiesString();
        log.info("Raw authorities string from DB for user {}: {}", userFromDb.getUsername(), rawAuthorities);

        if (rawAuthorities != null && !rawAuthorities.trim().isEmpty()) {
            authorities = Arrays.stream(rawAuthorities.split(","))
                                  .map(String::trim)
                                  .filter(role -> !role.isEmpty())
                                  .map(SimpleGrantedAuthority::new)
                                  .collect(Collectors.toList());
        } else {
            log.warn("No authorities found for user {}, granting default ROLE_USER.", userFromDb.getUsername());
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        if (authorities.isEmpty()) {
            log.warn("Authorities list is empty for user {} after processing, granting default ROLE_USER.", userFromDb.getUsername());
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        log.info("Authorities for user {}: {}", userFromDb.getUsername(), authorities);

        // 조회된 사용자 정보와 처리된 권한 정보를 사용하여 UserDetailsDto 객체를 생성합니다.
        return UserDetailsDto.builder()
                .username(userFromDb.getUsername()) // EMP_ID
                .password(userFromDb.getPassword()) // 암호화된 비밀번호
                .authorities(authorities)           // 처리된 권한 목록
                .enabled(userFromDb.isEnabled())    // 계정 활성화 상태
                .build();
    }
}