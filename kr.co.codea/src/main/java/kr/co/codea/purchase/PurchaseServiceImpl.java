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
        // params 맵에는 컨트롤러에서 추가한 startRowForQuery와 endRowForQuery가 이미 포함되어 있습니다.
        return purchaseMapper.selectPurchaseList(params);
    }

    @Override
    public int getPurchaseListCount(Map<String, Object> params) {
        // 이 메소드는 전체 아이템 수를 가져오므로 페이지네이션 파라미터가 필요 없습니다.
        return purchaseMapper.countPurchaseList(params);
    }
}