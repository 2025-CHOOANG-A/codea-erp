package kr.co.codea.purchase;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Override
    public PurchaseDto getPurchaseDetail(int purchaseId) {
    	 PurchaseDto header = purchaseMapper.selectPurchaseHeader(purchaseId);
         if (header != null) {
             List<PurchaseDto.PurchaseDetail> details = purchaseMapper.selectPurchaseDetails(purchaseId);
             header.setDetailList(details);
         }
         return header;
    }
    
    @Override
    public List<PurchaseDto.ItemSimple> searchItems(String keyword) {
        return purchaseMapper.searchItems(keyword);
    }

    @Override
    public List<PurchaseDto.SupplierSimple> searchSuppliers(String keyword) {
        return purchaseMapper.searchSuppliers(keyword);
    }

    @Override
    public List<PurchaseDto.EmployeeSimple> searchEmployees(String keyword) {
        return purchaseMapper.searchEmployees(keyword);
    }
    
    @Transactional
    @Override
    public void registerPurchase(PurchaseDto purchaseDto) {
        purchaseMapper.insertPurchaseHeader(purchaseDto);

        int purchaseId = purchaseDto.getPurchaseId(); // mapper에서 SELECT KEY로 세팅됨
        for (PurchaseDto.PurchaseDetail detail : purchaseDto.getDetailList()) {
            detail.setPurchaseId(purchaseId);
            purchaseMapper.insertPurchaseDetail(detail);
        }
    }

}







