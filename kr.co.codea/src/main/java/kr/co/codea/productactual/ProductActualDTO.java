package kr.co.codea.productactual;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProductActualDTO {
	//PA
    private String dailyId;
    private Date actualDate;
    private Integer actualQty;
    private Integer defectQty;
    
    //PP
    private String planId;
    private String itemCode;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate endDate;

    private LocalDate completionDate;
    private Integer planQty;

    private String status;
    private String empNo;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    //조인
    private String itemName;
    
    //검색관련
    private String keyword;
    
    //ui
    private String empName;


}
