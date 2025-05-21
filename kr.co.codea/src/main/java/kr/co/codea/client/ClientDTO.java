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
	private Integer bizCond;
	private String bizType;
	private String bp_tel;
	// 	private String tel;
	private String fax;
	private String address;
	private String addressDetail;
	private String postCode;
	private String bp_remark;
	private String bizCondCode;


    
    // 연관된 담당자 목록
    private List<ContactDTO> contacts;
}