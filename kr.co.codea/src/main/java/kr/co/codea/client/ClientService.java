package kr.co.codea.client;


import java.util.List;

import org.apache.ibatis.annotations.Param;



public interface ClientService {
    List<ClientDTO> getAllPartners();
    ClientDTO getPartnerDetails(Integer  bpId);
    
    //공통코드
    List<CommonCodeDTO> findCommonCode(@Param("codeType") String codeType, @Param("query") String query);
    CommonCodeDTO findCommonCodeById(@Param("codeId") Integer codeId); //코드id로 조회
    
    //거래처 추가 
    int insertClient(ClientDTO client);
   
    //거래처 업데이트
    int updateClient(ClientDTO client);
    
    //거래처 담당자 업데이트
    int updateContact(ContactDTO contact);
    
    //거래처 담당자 추가
    int insertContact(ContactDTO contact);
    
    //담당자 삭제 
    int deleteContact(@Param("bcId") Integer bcId, @Param("bpId") Integer bpId); 



    
}