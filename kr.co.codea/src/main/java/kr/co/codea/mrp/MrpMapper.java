package kr.co.codea.mrp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import kr.co.codea.productplan.ProductPlanDTO; 
@Mapper
public interface MrpMapper {
	List<ProductPlanDTO> ProductPlanList(ProductPlanDTO dto); //생산계획 리스트
}
