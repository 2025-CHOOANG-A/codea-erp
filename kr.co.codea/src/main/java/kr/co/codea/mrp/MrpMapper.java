package kr.co.codea.mrp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.co.codea.productplan.ProductPlanDTO; 
@Mapper
public interface MrpMapper {
	List<ProductPlanDTO> ProductPlanList(ProductPlanDTO dto); //생산계획 리스트
	List<MrpDTO> SelectCalcData(@Param("planIds") List<String> planIds); //생산계획ID 값을 받은 계산된 값
    int insertMrp(MrpDTO dto); //MRP TABLE 에 들어가는값
    int countMrpByPlanId(@Param("planId") String planId);
    int updateMrpStatusToDone(@Param("planId") String planId);
    int insertMrpAndUpdateStatus(MrpDTO dto);
}
