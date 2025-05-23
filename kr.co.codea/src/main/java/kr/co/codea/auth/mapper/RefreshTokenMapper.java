package kr.co.codea.auth.mapper;

import kr.co.codea.auth.dto.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface RefreshTokenMapper {

    /**
     * Refresh Token 저장
     */
    int insert(RefreshToken refreshToken);

    /**
     * EMP_ID로 Refresh Token 조회
     */
    Optional<RefreshToken> findByEmpId(@Param("empId") Long empId);

    /**
     * Refresh Token 문자열로 조회
     */
    Optional<RefreshToken> findByToken(@Param("token") String token);

    /**
     * Refresh Token 수정 (토큰 값 갱신 시 사용)
     */
    int update(RefreshToken refreshToken);

    /**
     * EMP_ID 기준으로 Refresh Token 삭제
     */
    int deleteByEmpId(@Param("empId") Long empId);

    /**
     * 토큰 문자열 기준으로 Refresh Token 삭제 (만료 등)
     */
    int deleteByToken(@Param("token") String token);
}
