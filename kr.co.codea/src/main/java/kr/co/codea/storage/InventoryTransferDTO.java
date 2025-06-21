package kr.co.codea.storage;

import java.util.Date;
import lombok.Data;

@Data
public class InventoryTransferDTO {
    // 기본 테이블 필드
    private Integer transferId;
    private String transferNo;
    private Integer itemId;
    private Integer fromWhId;
    private Integer toWhId;
    private Integer quantity;
    private Date transferDate;
    private Integer empId;
    private String remark;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    
    // 조인으로 가져오는 추가 정보
    private String itemCode;
    private String itemName;
    private String itemUnit;
    private String fromWhName;
    private String toWhName;
    private String empName;
    private String empNo;
    
    // 재고 정보 필드 추가
    private Integer currentStock;      // 현재고
    private Integer expectedStock;     // 가입고
    private Integer allocatedStock;    // 가출고
    private Integer availableStock;    // 가용재고
    
    // 페이징 관련 필드
    private Integer page = 1;           // 현재 페이지 (기본값 1)
    private Integer size = 10;          // 페이지당 항목 수 (기본값 10)
    private Integer offset;             // SQL OFFSET 값
    private Integer totalCount;         // 전체 데이터 개수
    private Integer totalPages;         // 전체 페이지 수
    
    // 검색 관련 필드
    private String keyword;             // 검색어 (이동번호, 품목명, 품목코드)
    
    // offset 계산 메서드
    public Integer getOffset() {
        if (page == null) page = 1;
        if (size == null) size = 10;
        return (page - 1) * size;
    }
    
    // 총 페이지 수 계산 메서드
    public void calculateTotalPages() {
        if (totalCount == null) totalCount = 0;
        if (size == null) size = 10;
        this.totalPages = (int) Math.ceil((double) totalCount / size);
    }
}