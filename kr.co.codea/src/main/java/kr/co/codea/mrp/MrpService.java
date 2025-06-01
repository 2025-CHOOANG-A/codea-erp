package kr.co.codea.mrp;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import kr.co.codea.productplan.ProductPlanDTO;

public interface MrpService {
	List<ProductPlanDTO> ProductPlanList(ProductPlanDTO dto); //생산계획 리스트
	List<MrpDTO> SelectCalcData(@Param("planIds") List<String> planIds);  //생산계획ID 값을 받은 계산된 값
	int insertMrp(MrpDTO dto); //MRP TABLE에 들어가는 값
}
