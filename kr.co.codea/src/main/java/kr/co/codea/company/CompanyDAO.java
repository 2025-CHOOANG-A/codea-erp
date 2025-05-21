package kr.co.codea.company;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyDAO implements CompanyService {
	@Autowired
	private CompanyMapper mp;
	
	
	@Override
	public List<CompanyDTO> alldata() {
		List<CompanyDTO> all = this.mp.alldata();
		return all;
	}
	
	@Override
	public int insertCompany(CompanyDTO dto) {
		int result = this.mp.insertCompany(dto);
		return result;
	}
	@Override
	public int updateCompany(CompanyDTO dto) {
		int result = this.mp.updateCompany(dto);
		return result;
	}
}
