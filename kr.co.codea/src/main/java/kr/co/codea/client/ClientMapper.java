package kr.co.codea.client;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClientMapper {
    
     //모든 거래처 조회
    List<ClientDTO> getAllPartners();
    
    //거래처 상세 정보 조회 (담당자 포함)
    ClientDTO selectPartnerWithContacts(Integer bpId);
    
    //거래처 검색 
    List<ClientDTO> searchClientbyKeyword(ClientDTO dto);
    
    //거래처 검색 (담당자 수 포함)
    List<ClientDTO> searchClientbyKeywordWithContactCount(ClientDTO dto);

    //거래처 상세 정보만 조회 (담당자 제외)
    ClientDTO getPartnerDetails(Integer bpId);

    //거래처 등록
    int insertClient(ClientDTO client);
    
     //거래처 수정
    int updateClient(ClientDTO client);
    
    //거래처 삭제
    int deleteClient(Integer bpId);
    
    //거래처 삭제 시 관련 담당자들도 함께 삭제
    int deleteContactsByBpId(Integer bpId);
    
    //거래처별 담당자 목록 조회
    List<ContactDTO> getContactsByBpId(Integer bpId);
    
    //담당자 등록
    int insertContact(ContactDTO contact);
    
    //담당자 수정
    int updateContact(ContactDTO contact);
    
    //담당자 삭제
    int deleteContact(@Param("bcId") Integer bcId, @Param("bpId") Integer bpId);
    
    //공통코드 검색 (업태 등)
    List<CommonCodeDTO> findCommonCode(@Param("codeType") String codeType, @Param("query") String query);
    
    //공통코드 ID로 조회
    CommonCodeDTO findCommonCodeById(@Param("codeId") Integer codeId);
    
     //업태 코드명 조회
    String getBizCondCodeName(Integer bizCond);
}