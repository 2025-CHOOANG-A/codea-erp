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
}
