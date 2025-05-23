package kr.co.codea.storage;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StorageMapper {
	 List<StorageDTO> selectStorageList(@Param("dto") StorageDTO dto);
	 
	 StorageDTO selectStorageDetailById(Integer whId); 


}
