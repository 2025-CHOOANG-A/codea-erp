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
                    
                    // 3. 재고 확인 - 🔥 getMaterialRequirements API와 정확히 동일한 로직 사용
                    List<String> materialErrors = new ArrayList<>();
                    log.info("=== 자재 소요량 정보 디버깅 ===");

                    for (MaterialRequirementDTO material : materials) {
                        // ✅ 모든 Material 필드 디버깅
                        log.info("자재 정보: 이름={}, itemId={}, materialId={}, empId={}, whId={}, requiredQty={}", 
                                material.getItemName(), 
                                material.getItemId(),        // 🔥 이 값이 문제!
                                material.getMaterialId(), 
                                material.getEmpId(), 
                                material.getWhId(), 
                                material.getRequiredQty());
                        
                        // ✅ 핵심 수정: getMaterialRequirements API와 동일한 메소드 호출
                        int availableQty = getAvailableInventory(material.getMaterialId());
                        
                        log.info("작업지시 생성 재고 확인: Plan={}, Material={}(ID={}), Required={}, Available={}", 
                                planId, material.getItemName(), material.getMaterialId(), 
                                material.getRequiredQty(), availableQty);
                        
                        if (availableQty < material.getRequiredQty()) {
                            materialErrors.add(String.format(
                                "품목 %s: 필요수량 %d, 가용재고 %d (부족: %d)",
                                material.getItemName(),
                                material.getRequiredQty(),
                                availableQty,
                                material.getRequiredQty() - availableQty
                            ));
                        }
                        if (material.getEmpId() == 0 ) {
                            log.error("⚠️ empId가 null 또는 0입니다! Material: {}", material.getItemName());
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
                        // 자재 + 계획정보를 외부에서 전달 
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
                    
                    // ✅ Shipment 패키지에 취소 로직이 있다면 그것 사용
                    // 없다면 현재 방식대로 직접 처리
                    
                    // 2. 가출고 내역 조회
                    List<Map<String, Object>> outbounds = mapper.getOutboundsByPlanId(planId);
                    log.info("조회된 가출고 내역 수: {} (Plan ID: {})", outbounds.size(), planId);
                    
                    // 3. 가출고 내역 삭제 (Shipment 패키지 로직 또는 직접 처리)
                    int deletedOutbounds = mapper.deleteOutboundsByPlanId(planId);
                    log.info("삭제된 가출고 내역 수: {} (Plan ID: {})", deletedOutbounds, planId);
                    
                    // 4. 상태를 "자재계획완료"로 되돌림
                    int updateResult = mapper.updateStatus(planId, "자재계획완료");
                    
                    if (updateResult > 0) {
                        canceledCount++;
                        log.info("작업지시 취소 완료 - Plan ID: {}", planId);
                    } else {
                        errorMessages.add("계획 " + planId + ": 상태 변경에 실패했습니다.");
                    }
                    
                } catch (Exception e) {
                    log.error("개별 작업지시 취소 중 오류 - Plan ID: {}, Error: {}", planId, e.getMessage(), e);
                    errorMessages.add("계획 " + planId + ": 취소 처리 중 오류가 발생했습니다.");
                }
            }
            
            result.put("success", errorMessages.isEmpty());
            result.put("canceledCount", canceledCount);
            result.put("totalCount", planIds.size());
            
            if (!errorMessages.isEmpty()) {
                result.put("errorMessages", errorMessages);
                result.put("message", "일부 작업지시 취소에 실패했습니다:\n" + String.join("\n", errorMessages));
            } else {
                result.put("message", canceledCount + "개의 작업지시가 성공적으로 취소되었습니다.");
            }
            
            log.info("작업지시 취소 완료 - 성공: {}, 실패: {}", canceledCount, planIds.size() - canceledCount);
            
        } catch (Exception e) {
            log.error("작업지시 취소 중 전체 오류 발생: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "작업지시 취소 중 오류가 발생했습니다: " + e.getMessage());
            result.put("canceledCount", 0);
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
 // ProductPlanServiceImp.java의 processOutbound 메소드 수정

    private boolean processOutbound(MaterialRequirementDTO material, Integer planNo, String planId) {
        try {
            log.info("=== processOutbound 시작 ===");
            log.info("Material 정보: 이름={}, materialId={}, empId={}, whId={}", 
                    material.getItemName(), material.getMaterialId(), 
                    material.getEmpId(), material.getWhId());
            
            if (shipmentDAO != null) {
                // ShipmentDAO 활용하여 가출고 처리
                ShipmentDTO shipmentDto = new ShipmentDTO();
                shipmentDto.setInoutType(24); // 가출고
                shipmentDto.setItemId(material.getMaterialId()); // materialId 사용
                shipmentDto.setWhId(material.getWhId());
                shipmentDto.setQuantity(material.getRequiredQty());
                shipmentDto.setItemUnitCost(material.getUnitCost());
                shipmentDto.setSourceDocType(43); // 생산지시
                shipmentDto.setSourceDocHeaderId(planNo);
                shipmentDto.setEmpNo(material.getEmpNo());
                
                if (material.getEmpId() == 0) {
                    log.warn("Material의 empId가 0입니다. 기본값 1 사용");
                    shipmentDto.setEmpId(1); 
                } else {
                    shipmentDto.setEmpId(material.getEmpId()); 
                }
                
                
                shipmentDto.setRemark("작업지시에 의한 자재 가출고 - 목적지: 생산공장01 - 계획번호: " + planId);

                log.info("가출고 처리: Plan No={}, Material ID={}, Quantity={}, EMP_ID={}", 
                        planNo, material.getMaterialId(), material.getRequiredQty(), shipmentDto.getEmpId());

                int result = shipmentDAO.ship_insert(shipmentDto);

                if (result > 0) {
                    log.info("가출고 등록 성공 - Shipment 패키지가 알아서 재고 처리합니다");
                    // updateInventoryAllocation 호출 제거 - Shipment 패키지에서 처리
                    return true;
                }
                return false;
            } else {
                // ShipmentDAO가 없으면 직접 처리
                if (material.getEmpId() == 0) {
                    log.warn("insertOutbound: empId가 0이므로 기본값 1 설정");
                    material.setEmpId(1);
                }
                
                log.info("직접 가출고 처리: Plan ID={}, Material ID={}, EMP_ID={}", 
                        planId, material.getMaterialId(), material.getEmpId());
                
                boolean outboundSuccess = mapper.insertOutbound(material, planId) > 0;
                
                if (outboundSuccess) {
                    // 직접 처리일 때만 재고 할당 업데이트
                    return updateInventoryAllocation(material.getMaterialId(), material.getWhId(), 
                        material.getRequiredQty(), true);
                }
                return false;
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
            log.info("재고 할당 업데이트: Item ID={}, WH ID={}, Quantity={}, Allocate={}", 
                    itemId, whId, quantity, isAllocate);
            
            if (isAllocate) {
                // 할당 증가 (가출고시)
                int result = mapper.updateInventoryAllocated(itemId, whId, quantity);
                log.info("할당 증가 결과: {}", result);
                return result > 0;
            } else {
                // 할당 감소 (가출고 취소시)
                int result = mapper.updateInventoryAllocated(itemId, whId, -quantity);
                log.info("할당 감소 결과: {}", result);
                return result > 0;
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
    
    
    //가용재고 조회
    @Override
    public int getAvailableInventory(int itemId) {
        try {
            // 🔥 이 부분이 정확히 어떻게 되어 있는지 확인!
            // 만약 getAvailableInventoryInternal을 호출하고 있다면 문제
            
            log.info("=== getAvailableInventory 호출 시작 ===");
            log.info("Item ID: {}", itemId);
            
            // ✅ 직접 mapper 호출로 수정
            int result = mapper.getAvailableInventory(itemId);
            
            log.info("Mapper 결과: {}", result);
            log.info("=== getAvailableInventory 호출 완료 ===");
            
            return result;
            
            // ❌ 만약 이런 식으로 되어 있다면 문제:
            // return getAvailableInventoryInternal(itemId);
            
        } catch (Exception e) {
            log.error("가용 재고 조회 중 오류 발생: {}", e.getMessage(), e);
            return 0;
        }
    }

    
    @Override
    public Map<String, Object> checkMaterialAvailability(List<String> planIds) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> planMaterials = new ArrayList<>();
        boolean hasAnyShortage = false;
        
        try {
            for (String planId : planIds) {
                // ✅ getMaterialRequirements와 동일한 방식으로 자료 조회
                List<MaterialRequirementDTO> materials = mapper.getMaterialRequirements(planId);
                ProductPlanDTO plan = mapper.productPlanDetail(planId);
                
                if (materials == null || materials.isEmpty()) {
                    continue;
                }
                
                // ✅ getMaterialRequirements와 동일한 로직 사용
                List<Map<String, Object>> materialStatus = new ArrayList<>();
                boolean planHasShortage = false;
                
                for (MaterialRequirementDTO material : materials) {
                    // 🔥 핵심: getMaterialRequirements API와 정확히 동일한 메소드 호출
                    int availableQty = getAvailableInventory(material.getMaterialId());
                    
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