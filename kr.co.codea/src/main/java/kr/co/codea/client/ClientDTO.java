package kr.co.codea.client;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ClientDTO {
    private int BP_ID;
    private String BP_CODE;
    private String BP_TYPE;
    private String BP_NAME;
    private String CEO_NAME;
    private String BIZ_NO;
    private Integer BIZ_COND;
    private String BIZ_TYPE;
    private String TEL;
    private String FAX;
    private String ADDRESS ;
    private String ADDRESS_DETAIL;
    private String POST_CODE;
    private String REMARK;

    
    // 연관된 담당자 목록
    private List<ContactDTO> contacts;
}