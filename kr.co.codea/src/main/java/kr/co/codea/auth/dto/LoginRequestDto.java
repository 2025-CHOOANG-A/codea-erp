package kr.co.codea.auth.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String loginId; // application.properties에서 hong1234! 계정의 ID 필드명과 일치해야 함
    private String password;
}