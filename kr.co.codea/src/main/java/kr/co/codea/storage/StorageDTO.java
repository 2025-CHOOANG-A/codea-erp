// StorageDTO.java - 페이징 필드 타입을 Integer로 통일
package kr.co.codea.storage;
import java.util.Date;
import lombok.Data;

@Data
public class StorageDTO {
    // WAREHOUSE 테이블 필드
    private Integer whId;
    private String whCode;
    private String whName;
    private String address;
    private String addressDetail;
    private String remark;
    private Date createdAt;
    private Date updatedAt;
    private String empNo;
    private String postCode;
    private String tel; // 확인 필요
    
    // 조인으로 가져오는 사원 정보
    private String empName;
    private String empTel;
    
    // 검색관련
    private String keyword;
    private String searchType;
    
    // 페이징 관련 - Integer 타입으로 통일
    private Integer page = 1;           // 현재 페이지 (기본값 1)
    private Integer size = 10;          // 페이지당 항목 수 (기본값 10)
    private Integer offset;             // SQL OFFSET 값
    private Integer totalCount;         // 전체 데이터 개수
    private Integer totalPages;         // 전체 페이지 수
    
    // 재고 현황 관련 (INVENTORY 테이블에서 집계)
    private Integer totalItems;      // 해당 창고의 총 품목 수
    private Long currentStock;       // 현재고 총합 (CURRENT_QTY 합계)
    private Long expectedStock;      // 가입고 총합 (EXPECTED_QTY 합계)
    private Long allocatedStock;     // 가출고 총합 (ALLOCATED_QTY 합계)
    private Long availableStock;     // 가용재고 (현재고 + 가입고 - 가출고)
    
    // offset 계산
    public Integer getOffset() {
        if (page == null) page = 1;
        if (size == null) size = 10;
        return (page - 1) * size;
    }
    
    // 총 페이지 수 계산
    public void calculateTotalPages() {
        if (totalCount == null) totalCount = 0;
        if (size == null) size = 10;
        this.totalPages = (int) Math.ceil((double) totalCount / size);
    }
}