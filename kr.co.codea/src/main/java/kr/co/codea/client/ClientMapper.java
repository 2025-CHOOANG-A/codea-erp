package kr.co.codea.client;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClientMapper {
    List<ClientDTO> selectAllPartners();
    ClientDTO selectPartnerWithContacts(int BP_ID);

}
