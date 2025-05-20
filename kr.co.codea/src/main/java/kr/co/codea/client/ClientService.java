package kr.co.codea.client;


import java.util.List;

import org.apache.ibatis.annotations.Param;



public interface ClientService {
    List<ClientDTO> getAllPartners();
    ClientDTO getPartnerDetails(Integer  bpId);
    List<CommonCodeDTO> findCommonCode(@Param("codeType") String codeType, @Param("query") String query);
    int insertClient(ClientDTO dto);



    
}