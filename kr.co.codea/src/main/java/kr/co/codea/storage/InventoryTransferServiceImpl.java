package kr.co.codea.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryTransferServiceImpl implements InventoryTransferService {
    private static final Logger log = LoggerFactory.getLogger(InventoryTransferServiceImpl.class);
    
    private final InventoryTransferMapper mapper;
    
    public InventoryTransferServiceImpl(InventoryTransferMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public List<InventoryTransferDTO> getTransferList(InventoryTransferDTO dto) {
        return mapper.selectTransferList(dto);
    }
    
    @Override
    public int getTransferCount(InventoryTransferDTO dto) {
        return mapper.selectTransferCount(dto);
    }
    
    @Override
    public InventoryTransferDTO getTransferDetail(Integer transferId) {
        return mapper.selectTransferDetail(transferId);
    }
    
    @Override
    @Transactional
    public Map<String, Object> createTransfer(InventoryTransferDTO dto) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 유효성 검사 (재고 수량 등)
        Map<String, Object> validation = validateTransfer(
            dto.getItemId(), dto.getFromWhId(), dto.getToWhId(), dto.getQuantity());
        
        if (!(Boolean) validation.get("success")) {
            return validation; // 유효성 검사 실패 시 결과 바로 반환
        }
        
        // 2. 재고 이동 '요청' 등록 (상태: '대기')
        // 이 단계에서 INOUT 테이블에 가출고/가입고가 등록되는 로직을 추가할 수 있습니다.
        // 현재는 INVENTORY_TRANSFER 테이블에만 기록합니다.
        int insertResult = mapper.insertTransfer(dto);
        if (insertResult == 0) {
            throw new RuntimeException("재고 이동 요청 등록에 실패했습니다. 트랜잭션이 롤백됩니다.");
        }
        
        result.put("success", true);
        result.put("message", "재고 이동 요청이 성공적으로 등록되었습니다.");
        result.put("transferNo", dto.getTransferNo());
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> cancelTransfer(Integer transferId, Integer empId) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 이동 내역 조회
        InventoryTransferDTO transfer = mapper.selectTransferDetail(transferId);
        if (transfer == null) {
            result.put("success", false);
            result.put("message", "존재하지 않는 재고 이동 내역입니다.");
            return result;
        }
        
        if ("취소".equals(transfer.getStatus())) {
            result.put("success", false);
            result.put("message", "이미 취소된 재고 이동 요청입니다.");
            return result;
        }

        if (!"대기".equals(transfer.getStatus())) {
            result.put("success", false);
            result.put("message", "이미 처리 완료된 요청은 취소할 수 없습니다.");
            return result;
        }
        
        // 2. 상태를 '취소'로 변경
        int cancelResult = mapper.cancelTransfer(transferId, empId);
        if (cancelResult == 0) {
            throw new RuntimeException("재고 이동 요청 취소에 실패했습니다. 트랜잭션이 롤백됩니다.");
        }
        
        result.put("success", true);
        result.put("message", "재고 이동 요청이 성공적으로 취소되었습니다.");
        
        return result;
    }
    
    @Override
    public List<InventoryTransferDTO> getWarehouseItems(Integer whId, String keyword) {
        return mapper.selectWarehouseItems(whId, keyword);
    }
    
    @Override
    public Map<String, Object> validateTransfer(Integer itemId, Integer fromWhId, Integer toWhId, Integer quantity) {
        Map<String, Object> result = new HashMap<>();
        
        if (itemId == null || fromWhId == null || toWhId == null || quantity == null) {
            result.put("success", false);
            result.put("message", "필수 정보(품목, 창고, 수량)가 누락되었습니다.");
            return result;
        }
        
        if (fromWhId.equals(toWhId)) {
            result.put("success", false);
            result.put("message", "출발 창고와 목적지 창고가 같을 수 없습니다.");
            return result;
        }
        
        if (quantity <= 0) {
            result.put("success", false);
            result.put("message", "이동 수량은 1 이상의 값이어야 합니다.");
            return result;
        }
        
        // 2. DB에서 가용 재고 수량 확인
        Integer availableStock = mapper.getItemAvailableStock(itemId, fromWhId);
        if (availableStock == null || availableStock < quantity) {
            result.put("success", false);
            result.put("message", String.format("재고가 부족합니다. (가용재고: %d, 요청수량: %d)",
                availableStock != null ? availableStock : 0, quantity));
            result.put("availableStock", availableStock != null ? availableStock : 0);
            return result;
        }
        
        result.put("success", true);
        result.put("message", "이동 가능");
        result.put("availableStock", availableStock);
        
        return result;
    }
}
