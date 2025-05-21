package kr.co.codea.client;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ClientDTO {
	private Integer bpId;
	private String bpCode;
	private String bpType;
	private String bpName;
	private String ceoName;
	private String bizNo;
	private int bizCond;
	private String bizType;
	private String tel;
	private String fax;
	private String address;
	private String addressDetail;
	private String postCode;
	private String remark;


    
    // 연관된 담당자 목록
    private List<ContactDTO> contacts;
}