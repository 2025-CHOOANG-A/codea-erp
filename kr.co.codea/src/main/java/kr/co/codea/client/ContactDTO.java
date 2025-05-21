package kr.co.codea.client;

import lombok.Data;

@Data
public class ContactDTO {
	private Integer bpId;
    private Integer bcId;
    private String bcName;
    private String bcPosition;
    private String tel;
    private String fax;
    private String remark;
    private String hp;
    private String email;
    
}
