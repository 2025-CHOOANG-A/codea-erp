package kr.co.codea.receiving;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReceivingMapper {
	public List<ReceivingDTO> rec_list();	// 목록 페이지
}