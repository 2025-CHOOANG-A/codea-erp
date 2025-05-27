package kr.co.codea.purchase;

import java.util.List;
import java.util.Map;

public interface PurchaseService {
    List<PurchaseDto> getPurchaseList(Map<String, Object> params);
    int getPurchaseListCount(Map<String, Object> params); // 페이징을 위한 전체 개수
    
    PurchaseDto getPurchaseDetail(int purchaseId);
}