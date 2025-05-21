package kr.co.codea.item;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface item_mapper {
	public void insert_item(item_DTO dto);            // 등록용
    List<item_DTO> item_select();     // 목록 조회용
}
