package kr.co.codea.purchase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class PurchaseDto {

    // 발주 헤더 정보
    private int purchaseId;
    private String purchaseCode;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date orderDate;
    private int bpId;
    private String supplierName;
    private double totalAmount;
    private String status;
    private int empId;
    private String employeeName;
    private Date createdAt;
    private String remark;

    // 발주 상세 항목 리스트
    private List<PurchaseDetail> detailList = new ArrayList<>();

    // 내부 클래스 (혹은 별도 클래스로 관리 가능)
    @Data
    public static class PurchaseDetail {
    	private int purchaseId;
        private int purchaseDetailId;
        private int itemId;
        private String itemCode;
        private String itemName;
        private String spec;
        private int orderQty;
        private double unitPrice;
        private double amount;
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private Date requiredDeliveryDate;
        private String remark;
        private String unitCode;   // 'EA', 'BOX' 등
        private String unitName;   // '개', '박스' 등
    }
    
    @Data
    public static class ItemSimple {
        private int itemId;
        private String itemName;
        private String itemCode;
        private String unitCode;
        private String unitName;  // '개' 같은 설명
        private double unitPrice;
    }

    @Data
    public static class SupplierSimple {
        private int bpId;
        private String bpName;
    }

    @Data
    public static class EmployeeSimple {
        private int empId;
        private String empName;
    }
}
