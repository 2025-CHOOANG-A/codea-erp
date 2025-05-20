package kr.co.codea.client;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClientMapper {
    List<ClientDTO> selectAllPartners();
    ClientDTO selectPartnerWithContacts(Integer bpId);
    List<CommonCodeDTO> findCommonCode(@Param("codeType") String codeType, @Param("query") String query);
    int insertClient(ClientDTO dto);


}



