package kr.co.codea.productplan;

import java.util.List;

import org.springframework.stereotype.Service;
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

}
