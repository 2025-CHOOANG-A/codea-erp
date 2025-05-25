package kr.co.codea.storage;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StorageMapper {
	 List<StorageDTO> selectStorageList(@Param("dto") StorageDTO dto);
	 
	 StorageDTO selectStorageDetailById(Integer whId); 
	 
	 int insertStorageList(StorageDTO dto);
	
	 List<StorageDTO> searchEmpNo(String query); //사원검색
	 
	 int updateStorage(StorageDTO dto); //창고업데이트



}
