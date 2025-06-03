package kr.co.codea.order;

import org.apache.ibatis.annotations.Mapper;
import kr.co.codea.order.OrderWriteDto.FormOrderCreateRequest;
import kr.co.codea.order.OrderWriteDto.FormOrderDetailRequest;

/**
 * MyBatis 매퍼 인터페이스
 * - mapper XML 파일: resources/mapper/order/OrderWriteMapper.xml 에 매핑되어야 합니다.
 */
@Mapper
public interface OrderWriteMapper {

    /**
     * (1) ORD_HEADER에 새 주문을 INSERT
     *     - useGeneratedKeys="true", keyProperty="ordId" 로 ordId에 PK(ORD_ID) 채워줌
     */
    int insertOrderHeader(FormOrderCreateRequest request);

    /**
     * (2) ORD_DETAIL에 상세 라인을 INSERT
     */
    int insertOrderDetail(FormOrderDetailRequest detail);
}
