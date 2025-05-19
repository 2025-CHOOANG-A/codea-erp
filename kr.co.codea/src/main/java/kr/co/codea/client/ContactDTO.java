package kr.co.codea.client;

import lombok.Data;

@Data
public class ContactDTO {
    private int BC_ID;
    private String BP_CODE;
    private String BP_TYPE;
    private String BC_NAME;
    private String BC_POSITION;
    private String TEL;
    private String FAX;
    private String REMARK;
}
