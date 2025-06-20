package kr.co.codea.storage;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InventoryTransferMapper {
    
    // 재고 이동 목록 조회
    List<InventoryTransferDTO> selectTransferList(InventoryTransferDTO dto);
    
    // 재고 이동 총 개수
    int selectTransferCount(InventoryTransferDTO dto);
    
    // 재고 이동 상세 조회
    InventoryTransferDTO selectTransferDetail(Integer transferId);
    
    // 재고 이동 요청 등록
    int insertTransfer(InventoryTransferDTO dto);
    
    // 재고 이동 요청 취소
    int cancelTransfer(@Param("transferId") Integer transferId, @Param("empId") Integer empId);
    
    // 창고별 품목 재고 조회
    List<InventoryTransferDTO> selectWarehouseItems(@Param("whId") Integer whId, @Param("keyword") String keyword);
    
    // 특정 품목의 가용 재고 조회
    Integer getItemAvailableStock(@Param("itemId") Integer itemId, @Param("whId") Integer whId);
    
    // updateInventoryForTransfer 메소드 삭제
}
