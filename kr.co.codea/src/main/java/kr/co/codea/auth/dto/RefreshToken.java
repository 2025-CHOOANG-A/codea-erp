package kr.co.codea.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    private Long rtId;        // PK (DB 테이블 컬럼명 RT_ID 와 매칭)
    private Long empId;       // 사원 ID (FK, EMPLOYEE 테이블의 EMP_ID 와 매칭)
    private String token;     // 리프레시 토큰 값
    private Date expiration;  // 만료 시간
    private Date createdAt;   // 생성 시간
    private Date updatedAt;   // 마지막 수정 시간

    public void updateTokenInfo(String newToken, Date newExpiration) {
        this.token = newToken;
        this.expiration = newExpiration;
        this.updatedAt = new Date(); // 업데이트 시간 갱신
    }
}