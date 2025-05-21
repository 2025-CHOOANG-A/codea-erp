package kr.co.codea.company;

import java.util.List;

public interface CompanyService {
	List<CompanyDTO> alldata();
	int insertCompany(CompanyDTO dto);
	int updateCompany(CompanyDTO dto);
}
