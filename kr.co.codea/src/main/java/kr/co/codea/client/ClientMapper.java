package kr.co.codea.client;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClientMapper {
    List<ClientDTO> selectAllPartners();
    ClientDTO selectPartnerWithContacts(Integer bpId);
    List<CommonCodeDTO> findCommonCode(@Param("codeType") String codeType, @Param("query") String query);
    CommonCodeDTO findCommonCodeById(@Param("codeId") Integer codeId);
    //거래처 추가 
    int insertClient(ClientDTO client);
   
    //거래처 업데이트
    int updateClient(ClientDTO client);
    
    //거래처 담당자 업데이트
    int updateContact(ContactDTO contact);
    
    //거래처 담당자 추가
    int insertContact(ContactDTO contact);
    
    //담당자 삭제 
    int deleteContact(@Param("bcId") Integer bcId, @Param("bpId") Integer bpId); // bpId도 함께 받도록
    
    List<ClientDTO> searchClientbyKeyword(@Param("dto") ClientDTO dto);    






}



