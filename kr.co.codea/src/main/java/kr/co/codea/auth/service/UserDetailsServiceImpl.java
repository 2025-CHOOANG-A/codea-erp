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
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final EmployeeMapper employeeMapper;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        log.info("Attempting to load user by loginId (EMP_USER_ID): {}", loginId);

        UserDetailsDto userFromDb = employeeMapper.findByLoginId(loginId)
                .orElseThrow(() -> {
                    log.warn("User not found with loginId (EMP_USER_ID): {}", loginId);
                    return new UsernameNotFoundException("User not found with loginId (EMP_USER_ID): " + loginId);
                });

        log.info("User found: {} (EMP_ID), Enabled from DB (as boolean in DTO): {}", userFromDb.getUsername(), userFromDb.isEnabled());

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

        if (authorities.isEmpty()) { // 최종적으로 권한이 없으면 안됨
            log.warn("Authorities list is empty for user {} after processing, granting default ROLE_USER.", userFromDb.getUsername());
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        log.info("Authorities for user {}: {}", userFromDb.getUsername(), authorities);

        // UserDetailsDto 생성자를 통해 최종 UserDetails 객체 반환
        // EmployeeMapper.xml에서 이미 UserDetailsDto 형태로 필요한 값을 다 가져오므로,
        // 필드값으로 새 객체를 만들거나, 가져온 객체를そのまま使うか.
        // 여기서는 가져온 userFromDb의 authorities만 위에서 처리한 것으로 교체하여 새 객체 생성.
        return UserDetailsDto.builder()
                .username(userFromDb.getUsername()) // EMP_ID (String)
                .password(userFromDb.getPassword()) // 암호화된 비밀번호
                .authorities(authorities)           // 처리된 권한 목록
                .enabled(userFromDb.isEnabled())    // DB에서 가져온 활성화 상태 (boolean)
                // rawAuthoritiesString은 UserDetailsDto 내부용이었으므로 최종 UserDetails에는 없어도 됨.
                .build();
    }

    public UserDetails loadUserByEmpId(Long empId) throws UsernameNotFoundException {
        log.info("Attempting to load user by EMP_ID: {}", empId);
        UserDetailsDto userFromDb = employeeMapper.findByEmpId(empId)
                .orElseThrow(() -> {
                    log.warn("User not found with EMP_ID: {}", empId);
                    return new UsernameNotFoundException("User not found with EMP_ID: " + empId);
                });
        // 여기도 위와 유사하게 권한 처리 로직 추가 필요
        Collection<? extends GrantedAuthority> authorities;
        String rawAuthorities = userFromDb.getRawAuthoritiesString();
         if (rawAuthorities != null && !rawAuthorities.trim().isEmpty()) {
            authorities = Arrays.stream(rawAuthorities.split(","))
                                .map(String::trim)
                                .filter(role -> !role.isEmpty())
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
        } else {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        if (authorities.isEmpty()) {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return UserDetailsDto.builder()
                .username(userFromDb.getUsername())
                .password(userFromDb.getPassword())
                .authorities(authorities)
                .enabled(userFromDb.isEnabled())
                .build();
    }
}