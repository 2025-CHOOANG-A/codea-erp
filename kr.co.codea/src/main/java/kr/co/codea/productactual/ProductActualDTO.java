package kr.co.codea.productactual;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProductActualDTO {
	
    // PRODUCTION_DAILY 테이블 관련
    private String dailyId;
    private Date actualDate;      // 작업일자
    private Integer actualQty;    // 일일 생산수량
    private Integer defectQty;    // 일일 불량수량
    
    // PRODUCTION_PLAN 테이블 관련
    private String planId;
    private Integer planNo; 
    private String itemCode;
    private Integer itemId;       // 입고 처리용 Item ID
    private LocalDate startDate;  // 계획 시작일
    private LocalDate dueDate;    // 계획 완료일
    private LocalDate actualStartDate; // 실제 생산 시작일 (새로 추가)
    private LocalDate endDate;    // 검색용 종료일
    private LocalDate completionDate; // 실제 생산 완료일
    private Integer planQty;      // 계획 수량
    private String status;        // 상태
    private String empNo;         // 담당자 사번
    private String remark;        // 비고
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 조인 데이터
    private String itemName;      // 품목명
    private String empName;       // 담당자명
    private Integer price;        // 품목 단가 (입고 처리용)
    
    // 검색 조건
    private String keyword;       // 검색어
    
    // UI 표시용
    private String createdAtFormatted; // 포맷된 등록일시


}
