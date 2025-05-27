package kr.co.codea.purchase;

import java.util.List;
import java.util.Map;


public interface PurchaseService {
	List<PurchaseDto> getPurchaseList(Map<String, Object> params);
    int getPurchaseListCount(Map<String, Object> params);
}
