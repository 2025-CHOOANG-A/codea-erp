package kr.co.codea.purchase;

import java.util.List;
import java.util.Map;

public interface PurchaseService {
    List<PurchaseDto> getPurchaseList(Map<String, Object> params);
    int getPurchaseListCount(Map<String, Object> params); // 페이징을 위한 전체 개수
    
    PurchaseDto getPurchaseDetail(int purchaseId);
    
    List<PurchaseDto.ItemSimple> searchItems(String keyword);
    List<PurchaseDto.SupplierSimple> searchSuppliers(String keyword);
    List<PurchaseDto.EmployeeSimple> searchEmployees(String keyword);
    
    void registerPurchase(PurchaseDto purchaseDto);
	void processPurchase(Long purchaseId);

}