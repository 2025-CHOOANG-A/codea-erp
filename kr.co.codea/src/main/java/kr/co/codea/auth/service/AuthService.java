package kr.co.codea.auth.service;

import kr.co.codea.auth.dto.LoginRequestDto;
import kr.co.codea.auth.dto.TokenDto;

public interface AuthService {
    TokenDto login(LoginRequestDto loginRequestDto);
    void logout(Long empId); // 사용자 식별자로 empId (PK)를 사용
    TokenDto reissueToken(String refreshTokenValue);
}