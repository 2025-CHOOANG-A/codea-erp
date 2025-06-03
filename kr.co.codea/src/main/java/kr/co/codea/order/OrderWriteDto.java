package kr.co.codea.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class OrderWriteDto {

    // ────────────────────────────────────────────────────────
    // 기존 FormOrderCreateRequest / FormOrderDetailRequest 그대로 유지
    // ────────────────────────────────────────────────────────

    /**
     * (1) 주문 헤더 등록용 DTO (폼에서 바인딩)
     */
    @Data
    public static class FormOrderCreateRequest {
        private LocalDate orderDate;                // name="orderDate"
        private Long bpId;                          // name="bpId"
        private Long empId;                         // name="empId"
        private String remark;                      // name="remark"
        private List<FormOrderDetailRequest> detailList; // name="detailList[*]"
        private Long ordId;                         // INSERT 후 MyBatis가 채워줄 GENERATED KEY
    }

    /**
     * (2) 주문 상세(라인) 등록용 DTO
     */
    @Data
    public static class FormOrderDetailRequest {
        private Long ordId;                         // ServiceImpl에서 헤더 INSERT 후 세팅
        private Long itemId;                        // name="detailList[i].itemId"
        private Integer orderQty;                   // name="detailList[i].orderQty"
        private BigDecimal unitPrice;               // name="detailList[i].unitPrice"
        private LocalDate requiredDate;             // name="detailList[i].requiredDate"
        private String remark;                      // name="detailList[i].remark"
    }


    // ────────────────────────────────────────────────────────
    // 여기에 “자동완성 결과 바인딩용” 간단한 DTO 클래스를 추가
    // ────────────────────────────────────────────────────────

    /**
     * (3) 품목 자동완성 결과 바인딩용 DTO
     *  resultType="kr.co.codea.order.OrderWriteDto$ItemSimple"
     */
    @Data
    public static class ItemSimple {
        private Long itemId;         // ITEM.ITEM_ID
        private String itemName;     // ITEM.ITEM_NAME
        private String itemCode;     // ITEM.ITEM_CODE
        private String unitName;     // COMMON_CODE.CODE_DESC (단위)
        private BigDecimal unitPrice; // ITEM.PRICE
    }

    /**
     * (4) 거래처(발주처) 자동완성 결과 바인딩용 DTO
     *  resultType="kr.co.codea.order.OrderWriteDto$PartnerSimple"
     */
    @Data
    public static class PartnerSimple {
        private Long bpId;           // BUSINESS_PARTNER.BP_ID
        private String bpName;       // BUSINESS_PARTNER.BP_NAME
    }

    /**
     * (5) 직원(담당자) 자동완성 결과 바인딩용 DTO
     *  resultType="kr.co.codea.order.OrderWriteDto$EmployeeSimple"
     */
    @Data
    public static class EmployeeSimple {
        private Long empId;          // EMPLOYEE.EMP_ID
        private String empName;      // EMPLOYEE.EMP_NAME
    }
}
