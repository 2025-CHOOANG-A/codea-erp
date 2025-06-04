package kr.co.codea.mrp;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class MrpDTO {
    String mrpId;             // MRP 결과 고유번호 (예: MM2405290001)
    String planId;            // 생산계획번호
    Long itemId;              // ITEM 번호 (완제품)
    String itemCode;          // 제품 코드 (표기용)
    String bomHeaderId;
    String itemName;          // 제품명 (표기용)
    String itemType;          // 제품 타입 (완제품/원자재 등)
    BigDecimal displayPrice;  // 표시 단가 (판매가/매입가)
    String unit;              // 단위 코드 (EA, BOX 등)
    Integer leadTime;         // 리드타임
    String childId;           // BOM상 자재 코드 (하위자재)
    BigDecimal quantity;      // BOM 소요 계수
    BigDecimal price;         // 자재 단가
    BigDecimal planQty;       // 계획 생산량
    BigDecimal requiredQty;   // 소요량 (생산수량 * BOM수량)
    String empNo;             // 담당자 사번
    String status;            // 상태
    BigDecimal currentQty;    // 현재고
    LocalDate startDate;      // 생산계획 시작일
    LocalDate dueDate;        // 생산계획 종료일
    LocalDate requiredDate;   // 계획발주입고일(생산시작 - 1일)
    LocalDate plannedOrderDate; // 계획주문발주일(생산시작-1-LEAD_TIME)
    LocalDate ddueDate;       // 납기일(생산완료 + LEAD_TIME)
    BigDecimal expectedAmount; // 예상 금액 (자재가격 * 소요량)
    LocalDate selectTime;     // 계산된 시간(SYSDATE)
    String remark;            // 비고
    LocalDate createdAt;      // 생성일시
    LocalDate updatedAt;      // 마지막 수정일
    
    private boolean mrpSaved;
    
    private LocalDate endDate;  // ★ 추가!
}