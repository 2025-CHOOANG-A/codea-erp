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

}
