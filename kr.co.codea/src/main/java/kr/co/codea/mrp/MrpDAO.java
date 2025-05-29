package kr.co.codea.mrp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}

