package kr.co.codea.auth.dto;

import lombok.Data;

/*로그인 요청 시 전달되는 사용자 입력 데이터 DTO*/
@Data
public class LoginRequestDto {
    private String loginId;   // 사용자 ID
    private String password;  // 사용자 비밀번호
}
