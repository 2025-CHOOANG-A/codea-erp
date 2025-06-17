package kr.co.codea.storage;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StorageMapper {
    List<StorageDTO> selectStorageList(@Param("dto") StorageDTO dto);
    List<StorageDTO> selectStorageWithInventory(@Param("dto") StorageDTO dto); // 재고 현황 포함
    int selectStorageCount(@Param("dto") StorageDTO dto); // 총 개수 조회
    StorageDTO selectStorageDetailById(Integer whId); // 상세 (재고 현황 포함)
    int insertStorageList(StorageDTO dto);
    List<StorageDTO> searchEmpNo(String query); // 사원검색
    int updateStorage(StorageDTO dto); // 창고업데이트
}