package kr.co.codea.order;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class OrderDto {

    /**
     * 주문 목록 조회 시 사용되는 DTO
     * (OrderMapper.xml의 selectOrderList 결과와 매핑)
     */
    @Data
    public static class OrderDetailView {
        private Long ordId;                 // 주문 ID (ORD_HEADER.ORD_ID)
        private Long ordDetailId;           // 주문 상세 ID (ORD_DETAIL.ORD_DETAIL_ID)
        private Long itemId;                // 품목 ID (ITEM.ITEM_ID)
        private String ordCode;             // 주문 코드 (ORD_HEADER.ORD_CODE)
        private LocalDate orderDate;        // 주문일 (ORD_HEADER.ORDER_DATE)
        private String remark;              // 주문 헤더 비고 (ORD_HEADER.REMARK)
        private String orderDetailRemark;   // 주문 상세 비고 (ORD_DETAIL.REMARK)
        private String status;              // 주문 상태 (ORD_HEADER.STATUS)
        private String bpName;              // 거래처명 (BUSINESS_PARTNER.BP_NAME)
        private String productName;         // 제품명 (ITEM.ITEM_NAME)
        private int orderQty;               // 주문 수량 (ORD_DETAIL.ORDER_QTY)
        private Integer stockQty;           // 보유 수량 (INVENTORY.CURRENT_QTY)
        private Integer whId;               // 창고 ID (INVENTORY.WH_ID)
        private LocalDate requiredDate;     // 납기일 (ORD_DETAIL.REQUIRED_DELIVERY_DATE)
    }

    /**
     * 페이징 결과를 담는 DTO
     */
    @Data
    public static class PagingResult<T> {
        private List<T> content;
        private int currentPage;
        private int totalPages;

        public PagingResult(List<T> content, int currentPage, int totalPages) {
            this.content = content;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
        }
    }

    /**
     * 검색 파라미터를 담는 DTO
     */
    @Data
    public static class SearchParam {
        private String keyword;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;

        public SearchParam(String keyword, String status, LocalDate startDate, LocalDate endDate) {
            this.keyword = keyword;
            this.status = status;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    /**
     * 주문 등록/수정용 DTO
     */
    @Data
    public static class OrderCreateRequest {
        private String bpId; // 거래처 ID
        private String empId; // 담당자 ID
        private String remark; // 헤더 비고
        private List<OrderDetailRequest> details; // 주문 상세 목록
    }

    /**
     * 주문 상세 등록/수정용 DTO
     */
    @Data
    public static class OrderDetailRequest {
        private Long itemId; // 품목 ID
        private int orderQty; // 주문 수량
        private double unitPrice; // 단가
        private LocalDate requiredDeliveryDate; // 희망 납기일
        private String remark; // 상세 비고
    }

    /**
     * 가출고 처리 요청 시 사용될 DTO
     * (OrderMapper.xml의 insertProvisionalShipmentToInOut 파라미터와 매핑)
     */
    @Data
    public static class ProvisionalShipmentRequest {
        private Long ordId;             // 주문 ID (ORD_HEADER.ORD_ID)
        private Long orderDetailId;     // 주문 상세 ID (ORD_DETAIL.ORD_DETAIL_ID)
        private Long itemId;            // 품목 ID (ITEM.ITEM_ID)
        private int quantity;           // 가출고 수량 (INOUT.QUANTITY)
        private Long whId;              // 창고 ID (INOUT.WH_ID)
        private Long empId;             // 직원 ID (INOUT.EMP_ID)
        private String remark;          // 비고 (INOUT.REMARK)
    }
}
