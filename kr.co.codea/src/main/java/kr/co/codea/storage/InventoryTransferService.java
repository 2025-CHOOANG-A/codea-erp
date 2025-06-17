package kr.co.codea.storage;

import java.util.List;
import java.util.Map;

public interface InventoryTransferService {
     // 재고 이동 목록 조회
    List<InventoryTransferDTO> getTransferList(InventoryTransferDTO dto);
    
    // 재고 이동 총 개수
    int getTransferCount(InventoryTransferDTO dto);
    
    // 재고 이동 상세 조회
    InventoryTransferDTO getTransferDetail(Integer transferId);
    
    // 재고 이동 요청 생성
    Map<String, Object> createTransfer(InventoryTransferDTO dto);
    
    // 재고 이동 요청 취소
    Map<String, Object> cancelTransfer(Integer transferId, Integer empId);
    
    // 창고별 품목 목록 조회
    List<InventoryTransferDTO> getWarehouseItems(Integer whId, String keyword);
    
    // 재고 이동 가능 여부 유효성 검사
    Map<String, Object> validateTransfer(Integer itemId, Integer fromWhId, Integer toWhId, Integer quantity);
}
