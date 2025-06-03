package kr.co.codea.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 주문 등록 화면에서 폼 데이터를 바인딩하기 위한 DTO 모음
 */
public class OrderWriteDto {

    /**
     * (1) 주문 헤더 입력용 DTO
     */
    @Data
    public static class FormOrderCreateRequest {
        private LocalDate orderDate;                    // name="orderDate"
        private Long bpId;                              // name="bpId"
        private Long empId;                             // name="empId"
        private String remark;                          // name="remark"
        private List<FormOrderDetailRequest> detailList; // name="detailList[*]"

        // INSERT 후 MyBatis가 이 필드를 채워줌(ORD_HEADER.ORD_ID)
        private Long ordId;
    }

    /**
     * (2) 주문 상세(라인) 입력용 DTO
     */
    @Data
    public static class FormOrderDetailRequest {
        private Long ordId;           // ServiceImpl에서 헤더 INSERT 후 세팅
        private Long itemId;          // name="detailList[i].itemId"
        private Integer orderQty;     // name="detailList[i].orderQty"
        private BigDecimal unitPrice; // name="detailList[i].unitPrice"
        private LocalDate requiredDate; // name="detailList[i].requiredDate"
        private String remark;        // name="detailList[i].remark"
    }
}
