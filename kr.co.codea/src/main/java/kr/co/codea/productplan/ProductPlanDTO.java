package kr.co.codea.productplan;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ProductPlanDTO {
    private String planId;
    private String itemCode;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate endDate;

    private LocalDate completionDate;
    private Integer planQty;
    private Integer actualQty;
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
    
    //생산계획 상태 변경을 위한 아이디값
    private List<String> planIds;
    
    //작업지시/취소 확인을 위함
    private String actionType;

    //mrp 저장여부 확인
    private boolean mrpSaved;
    public boolean isMrpSaved() { return mrpSaved; }
    public void setMrpSaved(boolean mrpSaved) { this.mrpSaved = mrpSaved; }
    private String saveStatus;
    public String getSaveStatus() { return saveStatus; }
    public void setSaveStatus(String saveStatus) { this.saveStatus = saveStatus; }
}
