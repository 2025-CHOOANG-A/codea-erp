package kr.co.codea.productplan;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductPlanMapper {
	List<ProductPlanDTO> ProductPlanList(ProductPlanDTO dto);

}
