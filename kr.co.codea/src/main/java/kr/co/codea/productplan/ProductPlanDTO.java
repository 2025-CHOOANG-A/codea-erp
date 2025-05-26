package kr.co.codea.productplan;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProductPlanDTO {
    private String planId;
    private String itemCode;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate endDate;

    private LocalDate completionDate;
    private int planQty;
    private int actualQty;
    private String status;
    private String empNo;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    //조인
    private String itemName;
    
    //검색관련
    private String keyword;

}
