package kr.co.codea.order;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class OrderDto {

    @Data
    public static class OrderDetailView {
        private Long ordId;
        private String ordCode;
        private LocalDate orderDate;
        private String remark;
        private String status;
        private String bpName;
        private String productName; // ITEM.ITEM_NAME에서 가져옴
        private int orderQty;
        private int stockQty; // INVENTORY.CURRENT_QTY에서 가져옴
        private LocalDate requiredDate; // REQUIRED_DELIVERY_DATE로 변경됨
    }

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
}