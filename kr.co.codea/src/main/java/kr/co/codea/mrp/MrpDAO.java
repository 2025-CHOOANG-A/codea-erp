package kr.co.codea.mrp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}

