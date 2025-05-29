package kr.co.codea.mrp;

import java.util.List;

import kr.co.codea.productplan.ProductPlanDTO;

public interface MrpService {
	List<ProductPlanDTO> ProductPlanList(ProductPlanDTO dto);
}
