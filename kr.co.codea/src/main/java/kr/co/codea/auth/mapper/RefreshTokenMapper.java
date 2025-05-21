package kr.co.codea.auth.mapper;

import kr.co.codea.auth.dto.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface RefreshTokenMapper {

    int insert(RefreshToken refreshToken);

    Optional<RefreshToken> findByEmpId(@Param("empId") Long empId); // Long 타입으로 변경

    Optional<RefreshToken> findByToken(@Param("token") String token); // 토큰 값으로 조회

    int update(RefreshToken refreshToken);

    int deleteByEmpId(@Param("empId") Long empId); // Long 타입으로 변경

    int deleteByToken(@Param("token") String token); // 토큰 값으로 삭제 (만료 시 등)
}