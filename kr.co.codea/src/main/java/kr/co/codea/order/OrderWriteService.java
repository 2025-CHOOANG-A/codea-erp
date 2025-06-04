package kr.co.codea.order;

import java.util.List;

public interface OrderWriteService {
    /** (1) 주문 등록 (헤더+상세) */
    void createOrder(OrderWriteDto.FormOrderCreateRequest request) throws Exception;

    /** (3) 품목 검색 */
    List<OrderWriteDto.ItemSimple> searchItems(String keyword);

    /** (4) 발주처 검색 */
    List<OrderWriteDto.PartnerSimple> searchPartners(String keyword);

    /** (5) 담당자 검색 */
    List<OrderWriteDto.EmployeeSimple> searchEmployees(String keyword);
}
