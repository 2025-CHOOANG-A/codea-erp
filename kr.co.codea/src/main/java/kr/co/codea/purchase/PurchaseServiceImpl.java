package kr.co.codea.purchase;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

	 private final PurchaseMapper purchaseMapper;

	@Override
	public List<PurchaseDto> getPurchaseList(Map<String, Object> params) {
		return purchaseMapper.selectPurchaseList(params);
	}

	@Override
	public int getPurchaseListCount(Map<String, Object> params) {
		return purchaseMapper.countPurchaseList(params);
	}

}
