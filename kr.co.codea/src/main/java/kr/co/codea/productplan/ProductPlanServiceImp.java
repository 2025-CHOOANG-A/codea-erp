package kr.co.codea.productplan;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service

public class ProductPlanServiceImp implements ProductPlanService {
	private final ProductPlanMapper mapper;
	
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
	@Transactional // 여러 건을 업데이트하므로 트랜잭션 처리
	public int changePlansStatus(List<String> planIds, String targetStatus, String mrpok) {
	        int updatedCount = 0;
	        
	        if(targetStatus.equals("작업지시")) {
	        	
	        	
	        } else {
	        	
	        	
	        }
	        
	        for (String planId : planIds) {

	            ProductPlanDTO plan = mapper.findByPlanIdAndStatus(planId, targetStatus); // 예시 메소드

	            if (plan != null) {
	                // 상태 변경
	                int result = mapper.updateStatus(planId, mrpok);
	                if (result > 0) {
	                    updatedCount++;
	                }
	            }
	        }
	        return updatedCount;
	    }
	   

}
