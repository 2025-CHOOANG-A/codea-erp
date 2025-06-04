package kr.co.codea.mrp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.codea.company.CompanyDTO;
import kr.co.codea.productplan.ProductPlanDTO;



@Service
public class MrpDAO implements MrpService {
	@Autowired
	private MrpMapper mp;
	
	/*
	public MrpDAO(MrpMapper mapper) {
		this.mp=mapper;
	}*/
	
	@Override
	public List<ProductPlanDTO> ProductPlanList(ProductPlanDTO dto) {
		return mp.ProductPlanList(dto);
	}
	
	
	
	@Override
	public List<MrpDTO> SelectCalcData(List<String> planIds) {
		List<MrpDTO> result = this.mp.SelectCalcData(planIds);
		return result;
	}
	@Override
	public int insertMrp(MrpDTO dto) {
		int result = this.mp.insertMrp(dto);
		return result;
	}
	
	@Override
	public int countMrpByPlanId(String planId) {
	    return mp.countMrpByPlanId(planId);
	}
	@Override
	public int updateMrpStatusToDone(String planId) {
		 return mp.updateMrpStatusToDone(planId);
	}
	
	@Override
	@Transactional
	public int insertMrpAndUpdateStatus(MrpDTO dto) {
	    int insertResult = this.mp.insertMrp(dto); // 1. insert
	    int updateResult = this.mp.updateMrpStatusToDone(dto.getPlanId()); // 2. update
	
	    // 둘 다 성공시 1, 아니면 0 또는 예외 발생
	    if (insertResult > 0 && updateResult > 0) {
	        return 1;
	    } else {
	        // 원하는 방식대로 처리 (예: 롤백)
	        throw new RuntimeException("MRP 저장 혹은 상태 변경 실패");
	    }
	}
	@Override
	public List<MrpDTO> selectMrpList(MrpDTO dto) {
		List<MrpDTO> result = this.mp.selectMrpList(dto);
		return result;
	}
}

