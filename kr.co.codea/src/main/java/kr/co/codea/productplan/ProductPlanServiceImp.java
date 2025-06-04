package kr.co.codea.productplan;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import kr.co.codea.inventory.InventoryDAO;
import kr.co.codea.inventory.InventoryDTO;
import kr.co.codea.shipment.ShipmentDAO;
import kr.co.codea.shipment.ShipmentDTO;

@Service
public class ProductPlanServiceImp implements ProductPlanService {
    private static final Logger log = LoggerFactory.getLogger(ProductPlanServiceImp.class);

    private final ProductPlanMapper mapper;
    
    @Autowired(required = false)
    private InventoryDAO inventoryDAO;
    
    @Autowired(required = false) 
    private ShipmentDAO shipmentDAO;
    
    public ProductPlanServiceImp(ProductPlanMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public List<ProductPlanDTO> ProductPlanList(ProductPlanDTO dto) {
        return mapper.ProductPlanList(dto);
    }
    
    @Override
    public int insertProductPlan(ProductPlanDTO dto) {
        return mapper.insertProductPlan(dto);
    }
    
    @Override
    public List<ProductPlanDTO> searchItem(String query) {
        return mapper.searchItem(query);
    }
    
    @Override
    public ProductPlanDTO productPlanDetail(String planId) {
        return mapper.productPlanDetail(planId);
    }
    
    @Override
    public int productPlanUpdate(ProductPlanDTO dto) {
        return mapper.productPlanUpdate(dto);
    }
    
    @Override
    @Transactional
    public Map<String, Object> changePlansStatus(List<String> planIds, String targetStatus, String mrpok) {
        Map<String, Object> result = new HashMap<>();
        int updatedCount = 0;
        List<String> errorMessages = new ArrayList<>();
        
        try {
            if ("작업지시".equals(targetStatus)) {
                // 작업지시 생성 시 자재 가출고 처리
                for (String planId : planIds) {
                    // 1. 계획 정보 조회 (planNo 포함)
                    ProductPlanDTO plan = mapper.findByPlanIdAndStatus(planId, "자재계획완료");
                    if (plan == null) {
                        errorMessages.add("계획 " + planId + ": 자재계획완료 상태가 아니거나 존재하지 않습니다.");
                        continue;
                    }
                    
                    Integer planNo = plan.getPlanNo(); // 계획에서 숫자 ID 가져옴
                    
                    // 2. 자재 소요량 조회 (순수한 자재 정보만)
                    List<MaterialRequirementDTO> materials = mapper.getMaterialRequirements(planId);
                    if (materials == null || materials.isEmpty()) {
                        errorMessages.add("계획 " + planId + ": 자재 소요량 데이터가 없습니다.");
                        continue;
                    }
                    
                    // 3. 재고 확인
                    List<String> materialErrors = new ArrayList<>();
                    for (MaterialRequirementDTO material : materials) {
                        int availableQty = getAvailableInventoryInternal(material.getItemId());
                        if (availableQty < material.getRequiredQty()) {
                            materialErrors.add(String.format(
                                "품목 %s: 필요수량 %d, 가용재고 %d (부족: %d)",
                                material.getItemName(),
                                material.getRequiredQty(),
                                availableQty,
                                material.getRequiredQty() - availableQty
                            ));
                        }
                    }
                    
                    if (!materialErrors.isEmpty()) {
                        errorMessages.add("계획 " + planId + " (" + plan.getItemName() + "):\n" 
                                       + String.join("\n", materialErrors));
                        continue;
                    }
                    
                    // 4. 가출고 처리 (계획 정보를 외부에서 전달)
                    boolean allOutboundSuccess = true;
                    for (MaterialRequirementDTO material : materials) {
                        // 자재 + 계획정보를 외부에서 전달 → 논리적으로 깔끔!
                        boolean outboundResult = processOutbound(material, planNo, planId);
                        if (!outboundResult) {
                            allOutboundSuccess = false;
                            break;
                        }
                    }
                    
                    if (!allOutboundSuccess) {
                        errorMessages.add("계획 " + planId + ": 자재 가출고 처리 중 오류가 발생했습니다.");
                        continue;
                    }
                    
                    // 5. 상태 변경
                    int updateResult = mapper.updateStatus(planId, mrpok);
                    if (updateResult > 0) {
                        updatedCount++;
                    }
                }
            } else {
                // 일반적인 상태 변경
                for (String planId : planIds) {
                    int updateResult = mapper.updateStatus(planId, mrpok);
                    if (updateResult > 0) {
                        updatedCount++;
                    }
                }
            }
            
            result.put("success", errorMessages.isEmpty());
            result.put("updatedCount", updatedCount);
            result.put("totalCount", planIds.size());
            
            if (!errorMessages.isEmpty()) {
                result.put("errorMessages", errorMessages);
                result.put("message", "일부 계획에서 오류가 발생했습니다:\n" + String.join("\n\n", errorMessages));
            } else {
                result.put("message", updatedCount + "개의 " + targetStatus + "가 성공적으로 처리되었습니다.");
            }
            
        } catch (Exception e) {
            log.error("작업 처리 중 오류 발생: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "작업 처리 중 오류가 발생했습니다: " + e.getMessage());
            result.put("updatedCount", 0);
        }
        
        return result;
    }
    private Integer convertToInteger(Object value) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            try {
                return Integer.valueOf(value.toString());
            } catch (NumberFormatException e) {
                log.error("숫자 변환 실패: {}", value);
                return null;
            }
        }
    }
    
    @Override
    @Transactional
    public Map<String, Object> cancelWorkOrders(List<String> planIds) {
        Map<String, Object> result = new HashMap<>();
        int canceledCount = 0;
        List<String> errorMessages = new ArrayList<>();
        
        try {
            for (String planId : planIds) {
                try {
                    // 1. 작업지시 상태 확인
                    ProductPlanDTO plan = mapper.findByPlanIdAndStatus(planId, "작업지시");
                    if (plan == null) {
                        errorMessages.add("계획 " + planId + ": 작업지시 상태가 아니거나 존재하지 않습니다.");
                        continue;
                    }
                    
                    log.info("작업지시 취소 시작 - Plan ID: {}, Plan NO: {}", planId, plan.getPlanNo());
                    
                    // 2. 가출고 내역 조회 (삭제하기 전에 먼저 조회)
                    List<Map<String, Object>> outbounds = mapper.getOutboundsByPlanId(planId);
                    log.info("조회된 가출고 내역 수: {} (Plan ID: {})", outbounds.size(), planId);
                    
                    if (outbounds.isEmpty()) {
                        log.warn("가출고 내역이 없습니다 - Plan ID: {}", planId);
                    }
                    
                    // 3. 재고 할당 해제 (가출고 삭제 전에 먼저 실행)
                    boolean allocationSuccess = true;
                    for (Map<String, Object> outbound : outbounds) {
                        try {
                            // BigDecimal을 안전하게 Integer로 변환
                            Integer itemId = convertToInteger(outbound.get("ITEM_ID"));
                            Integer whId = convertToInteger(outbound.get("WH_ID"));
                            Integer quantity = convertToInteger(outbound.get("QUANTITY"));
                            
                            if (itemId == null || whId == null || quantity == null) {
                                log.error("가출고 데이터가 불완전합니다: {}", outbound);
                                continue;
                            }
                            
                            log.info("재고 할당 해제 - Item ID: {}, WH ID: {}, Quantity: {}", itemId, whId, quantity);
                            
                            // 할당 수량 감소 (음수로 전달)
                            int updateResult = mapper.updateInventoryAllocated(itemId, whId, -quantity);
                            
                            if (updateResult <= 0) {
                                log.error("재고 할당 해제 실패 - Item ID: {}, WH ID: {}, Quantity: {}", itemId, whId, quantity);
                                allocationSuccess = false;
                            } else {
                                log.info("재고 할당 해제 성공 - Item ID: {}, WH ID: {}, Quantity: {}", itemId, whId, quantity);
                            }
                            
                        } catch (Exception e) {
                            log.error("재고 할당 해제 중 오류 - Plan ID: {}, Outbound: {}, Error: {}", planId, outbound, e.getMessage());
                            allocationSuccess = false;
                        }
                    }
                    
                    if (!allocationSuccess) {
                        errorMessages.add("계획 " + planId + ": 재고 할당 해제 중 일부 오류가 발생했습니다.");
                    }
                    
                    // 4. 가출고 내역 삭제
                    int deletedOutbounds = mapper.deleteOutboundsByPlanId(planId);
                    log.info("삭제된 가출고 내역 수: {} (Plan ID: {})", deletedOutbounds, planId);
                    
                    // 5. 상태를 "자재계획완료"로 되돌림
                    int updateResult = mapper.updateStatus(planId, "자재계획완료");
                    
                    if (updateResult > 0) {
                        canceledCount++;
                        log.info("작업지시 취소 완료 - Plan ID: {}", planId);
                    } else {
                        errorMessages.add("계획 " + planId + ": 상태 변경에 실패했습니다.");
                        log.error("상태 변경 실패 - Plan ID: {}", planId);
                    }
                    
                } catch (Exception e) {
                    log.error("개별 작업지시 취소 중 오류 - Plan ID: {}, Error: {}", planId, e.getMessage(), e);
                    errorMessages.add("계획 " + planId + ": 취소 처리 중 오류가 발생했습니다. (" + e.getMessage() + ")");
                }
            }
            
            result.put("success", errorMessages.isEmpty());
            result.put("canceledCount", canceledCount);
            result.put("totalCount", planIds.size());
            
            if (!errorMessages.isEmpty()) {
                result.put("errorMessages", errorMessages);
                result.put("message", "일부 작업지시 취소에 실패했습니다:\n" + String.join("\n", errorMessages));
            } else {
                result.put("message", canceledCount + "개의 작업지시가 성공적으로 취소되었습니다.\n자재 할당이 해제되고 재고가 복구되었습니다.");
            }
            
            log.info("작업지시 취소 완료 - 성공: {}, 실패: {}", canceledCount, planIds.size() - canceledCount);
            
        } catch (Exception e) {
            log.error("작업지시 취소 중 전체 오류 발생: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "작업지시 취소 중 오류가 발생했습니다: " + e.getMessage());
            result.put("canceledCount", 0);
            result.put("errorMessages", Arrays.asList("전체 처리 중 오류: " + e.getMessage()));
        }
        
        return result;
    }
    
    /**
     * 가용 재고 수량 조회 (내부용)
     */
    private int getAvailableInventoryInternal(int itemId) {
        try {
            return mapper.getAvailableInventory(itemId);
        } catch (Exception e) {
            log.error("가용 재고 조회 중 오류 발생: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 자재 가출고 처리 - PLAN_NO 사용으로 Integer overflow 해결!
     */
    private boolean processOutbound(MaterialRequirementDTO material,Integer planNo, String planId) {
        try {
            if (shipmentDAO != null) {
                // ShipmentDAO 활용하여 가출고 처리
                ShipmentDTO shipmentDto = new ShipmentDTO();
                shipmentDto.setInoutType(24); // 가출고
                shipmentDto.setItemId(material.getItemId());
                shipmentDto.setWhId(material.getWhId());
                shipmentDto.setQuantity(material.getRequiredQty());
                shipmentDto.setItemUnitCost(material.getUnitCost());
                shipmentDto.setSourceDocType(43); // 생산지시

                // ✅ PLAN_NO 사용으로 Integer overflow 완전 해결!
                shipmentDto.setSourceDocHeaderId(planNo);
                shipmentDto.setEmpId(material.getEmpId());
                shipmentDto.setRemark("작업지시에 의한 자재 가출고 - 목적지: 생산공장01 - 계획번호: " + planId);

                int result = shipmentDAO.ship_insert(shipmentDto);

                if (result > 0) {
                    // 가출고 성공 시 재고의 할당 수량 업데이트
                    return updateInventoryAllocation(material.getItemId(), material.getWhId(), 
                        material.getRequiredQty(), true);
                }
                return false;
            } else {
                // ShipmentDAO가 없으면 직접 처리 (PLAN_NO 기준)
                return mapper.insertOutbound(material, planId) > 0;
            }
        } catch (Exception e) {
            log.error("자재 가출고 처리 중 오류 발생 (Plan No: {}, ID: {}): {}", 
                    planNo, planId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 재고 할당 수량 업데이트
     */
    private boolean updateInventoryAllocation(int itemId, int whId, int quantity, boolean isAllocate) {
        try {
            if (isAllocate) {
                // 할당 증가 (가출고시)
                return mapper.updateInventoryAllocated(itemId, whId, quantity) > 0;
            } else {
                // 할당 감소 (가출고 취소시)
                return mapper.updateInventoryAllocated(itemId, whId, -quantity) > 0;
            }
        } catch (Exception e) {
            log.error("재고 할당 수량 업데이트 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public PageInfo<ProductPlanDTO> getpages(ProductPlanDTO dto, int page, int size) {
        PageHelper.startPage(page, size);
        List<ProductPlanDTO> list = mapper.ProductPlanList(dto);
        return new PageInfo<>(list);
    }
    
    @Override
    public List<MaterialRequirementDTO> getMaterialRequirements(String planId) {
        return mapper.getMaterialRequirements(planId);
    }
    
    @Override
    public int getAvailableInventory(int itemId) {
        return mapper.getAvailableInventory(itemId);
    }
    
    @Override
    public Map<String, Object> checkMaterialAvailability(List<String> planIds) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> planMaterials = new ArrayList<>();
        boolean hasAnyShortage = false;
        
        try {
            for (String planId : planIds) {
                List<MaterialRequirementDTO> materials = mapper.getMaterialRequirements(planId);
                ProductPlanDTO plan = mapper.productPlanDetail(planId);
                
                if (materials == null || materials.isEmpty()) {
                    continue;
                }
                
                List<Map<String, Object>> materialStatus = new ArrayList<>();
                boolean planHasShortage = false;
                
                for (MaterialRequirementDTO material : materials) {
                    int availableQty = getAvailableInventoryInternal(material.getItemId());
                    
                    Map<String, Object> materialInfo = new HashMap<>();
                    materialInfo.put("itemCode", material.getItemCode());
                    materialInfo.put("itemName", material.getItemName());
                    materialInfo.put("requiredQty", material.getRequiredQty());
                    materialInfo.put("availableQty", availableQty);
                    materialInfo.put("unit", material.getUnit());
                    materialInfo.put("shortage", Math.max(0, material.getRequiredQty() - availableQty));
                    materialInfo.put("sufficient", availableQty >= material.getRequiredQty());
                    
                    if (availableQty < material.getRequiredQty()) {
                        planHasShortage = true;
                        hasAnyShortage = true;
                    }
                    
                    materialStatus.add(materialInfo);
                }
                
                Map<String, Object> planInfo = new HashMap<>();
                planInfo.put("planId", planId);
                planInfo.put("itemName", plan.getItemName());
                planInfo.put("planQty", plan.getPlanQty());
                planInfo.put("materials", materialStatus);
                planInfo.put("hasShortage", planHasShortage);
                
                planMaterials.add(planInfo);
            }
            
            result.put("success", true);
            result.put("plans", planMaterials);
            result.put("hasAnyShortage", hasAnyShortage);
            result.put("canIssueWorkOrder", !hasAnyShortage);
            
        } catch (Exception e) {
            log.error("자재 가용성 확인 중 오류 발생: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "자재 가용성 확인 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public boolean validateMaterialStock(String planId) {
        try {
            List<MaterialRequirementDTO> materials = mapper.getMaterialRequirements(planId);
            if (materials == null || materials.isEmpty()) {
                return false;
            }
            
            for (MaterialRequirementDTO material : materials) {
                int availableQty = getAvailableInventoryInternal(material.getItemId());
                if (availableQty < material.getRequiredQty()) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("자재 재고 검증 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }
}