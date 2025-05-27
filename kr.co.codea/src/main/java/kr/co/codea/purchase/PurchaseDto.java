package kr.co.codea.purchase;

import java.util.Date;

import lombok.Data;

@Data
public class PurchaseDto {
	private int purchaseId;        // 발주 ID (PURCHASE_HEADER.PURCHASE_ID)
    private String purchaseCode;   // 발주 코드 (PURCHASE_HEADER.PURCHASE_CODE)
    private Date orderDate;        // 발주일 (PURCHASE_HEADER.ORDER_DATE)
    private String supplierName;   // 공급처명 (JOIN된 BUSINESS_PARTNER.BP_NAME)
    private double totalAmount;  // 총 발주금액 (PURCHASE_DETAIL 합산 계산 결과)
    private String status;         // 발주상태 (PURCHASE_HEADER.STATUS)
    private String employeeName;   // 담당자명 (JOIN된 EMPLOYEE.EMP_NAME)
    private Date createdAt;        // 등록일 (PURCHASE_HEADER.CREATED_AT)
    private String remark;         // 비고 (PURCHASE_HEADER.REMARK)
    
    
}
