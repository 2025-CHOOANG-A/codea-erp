package kr.co.codea.receiving;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReceivingMapper {
	public List<ReceivingDTO> rec_list();	// 목록 페이지
	
	public ReceivingDTO rec_detail(@Param("inoutId") int inoutId);	// 상세 페이지
}