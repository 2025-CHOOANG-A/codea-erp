package kr.co.codea.auth.mapper;

import kr.co.codea.auth.dto.UserDetailsDto; // 또는 Employee DTO
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface EmployeeMapper {
    // loginId를 기준으로 사원 정보(특히 암호화된 비밀번호, 권한, 활성화 여부, EMP_ID)를 조회
    // 반환 타입은 UserDetailsDto를 직접 만들거나, Employee 정보를 담은 DTO/VO 사용 후 서비스에서 변환
    Optional<UserDetailsDto> findByLoginId(@Param("loginId") String loginId);

    // EMP_ID(PK)로 사원 정보 조회 (UserDetailsDto 구성 시 필요할 수 있음)
    Optional<UserDetailsDto> findByEmpId(@Param("empId") Long empId);
}