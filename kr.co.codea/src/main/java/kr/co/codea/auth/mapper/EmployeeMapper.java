package kr.co.codea.auth.mapper;

import kr.co.codea.auth.dto.UserDetailsDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface EmployeeMapper {

    /**
     * 로그인 ID(EMP_USER_ID)를 기준으로 사원 정보 조회
     * - 반환 정보: 암호화된 비밀번호, 권한, 계정 상태, EMP_ID 등
     */
    Optional<UserDetailsDto> findByLoginId(@Param("loginId") String loginId);

    /**
     * EMP_ID(PK)를 기준으로 사원 정보 조회
     * - 토큰 재발급, 인증 정보 복원 등에서 사용 가능
     */
    Optional<UserDetailsDto> findByEmpId(@Param("empId") Long empId);
}
