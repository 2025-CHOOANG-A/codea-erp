package kr.co.codea.company;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CompanyMapper {
	List<CompanyDTO> alldata();
	int insertCompany(CompanyDTO dto);
	int updateCompany(CompanyDTO dto);
}
