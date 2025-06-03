package kr.co.codea.order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 주문 목록 조회 (페이징)
     * @param keyword 검색 키워드 (거래처명 또는 제품명)
     * @param status 주문 상태 (진행중, 완료 등)
     * @param startDate 시작일 (yyyy-MM-dd 형식)
     * @param endDate 종료일 (yyyy-MM-dd 형식)
     * @param offset 페이징 오프셋
     * @param limit 페이징 리미트
     * @return 주문 상세 정보 리스트
     */
    List<OrderDto.OrderDetailView> selectOrderList(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 주문 총 개수 조회
     * @param keyword 검색 키워드 (거래처명 또는 제품명)
     * @param status 주문 상태 (진행중, 완료 등)
     * @param startDate 시작일 (yyyy-MM-dd 형식)
     * @param endDate 종료일 (yyyy-MM-dd 형식)
     * @return 조건에 맞는 주문 총 개수
     */
    int orderCount(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * 가출고 정보를 INOUT 테이블에 삽입합니다.
     * (OrderMapper.xml의 insertProvisionalShipmentToInOut 구문과 매핑)
     *
     * @param request 가출고 요청 정보를 담은 DTO
     * @return 삽입 성공 시 1, 실패 시 0 (또는 설정에 따라 다를 수 있음)
     */
    int insertProvisionalShipmentToInOut(OrderDto.ProvisionalShipmentRequest request);

    /**
     * 주문 헤더의 상태를 '완료'로 업데이트합니다.
     * (OrderMapper.xml의 updateOrderHeaderStatus 구문과 매핑)
     *
     * @param ordId 상태를 변경할 주문의 ID (ORD_HEADER.ORD_ID)
     * @return 업데이트 성공 시 1, 실패 시 0 (또는 영향받은 행의 수)
     */
    int updateOrderHeaderStatus(@Param("ordId") Long ordId);
    
    int updateOrderDetailStatus(@Param("ordDetailId") Long ordDetailId);

    /**
     * ② 주어진 ORD_HEADER(ID)에 속한 ORD_DETAIL 중
     *    아직 STATUS <> '완료'인 건의 개수를 조회
     */
    int countPendingDetail(@Param("ordId") Long ordId);
}