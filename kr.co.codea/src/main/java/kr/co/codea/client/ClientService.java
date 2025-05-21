package kr.co.codea.client;


import java.util.List;

import org.apache.ibatis.annotations.Param;



public interface ClientService {
    List<ClientDTO> getAllPartners();
    ClientDTO getPartnerDetails(Integer  bpId);
    List<CommonCodeDTO> findCommonCode(@Param("codeType") String codeType, @Param("query") String query);
    
    //거래처 추가 
    int insertClient(ClientDTO client);
   
    //거래처 업데이트
    int updateclinet(ClientDTO client);
    
    //거래처 담당자 업데이트
    int updateContact(ContactDTO contact);
    
    //거래처 담당자 추가
    int insertContact(ContactDTO contact);
    
    //담당자 삭제 
    int deleteContact(@Param("bcId") Integer bcId, @Param("bpId") Integer bpId); 



    
}