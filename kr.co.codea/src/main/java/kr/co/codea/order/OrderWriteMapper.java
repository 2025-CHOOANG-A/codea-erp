package kr.co.codea.order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.co.codea.order.OrderWriteDto.FormOrderCreateRequest;
import kr.co.codea.order.OrderWriteDto.FormOrderDetailRequest;

import java.util.List;

@Mapper
public interface OrderWriteMapper {
    /** (1) 헤더 INSERT */
    int insertOrderHeader(FormOrderCreateRequest request);

    /** (2) 상세 INSERT */
    int insertOrderDetail(FormOrderDetailRequest detail);

    // ────────────────────────────────────────────────────
    // (3) 품목 자동완성용
    List<OrderWriteDto.ItemSimple> searchItems(@Param("keyword") String keyword);

    // (4) 발주처(거래처) 자동완성용
    List<OrderWriteDto.PartnerSimple> searchPartners(@Param("keyword") String keyword);

    // (5) 담당자 자동완성용
    List<OrderWriteDto.EmployeeSimple> searchEmployees(@Param("keyword") String keyword);
}
