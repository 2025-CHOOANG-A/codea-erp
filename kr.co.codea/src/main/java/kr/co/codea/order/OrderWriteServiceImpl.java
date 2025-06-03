package kr.co.codea.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import kr.co.codea.order.OrderWriteDto.FormOrderCreateRequest;
import kr.co.codea.order.OrderWriteDto.FormOrderDetailRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderWriteServiceImpl implements OrderWriteService {

    private final OrderWriteMapper writeMapper;

    @Override
    @Transactional  // 헤더 INSERT + 상세 INSERT를 하나의 트랜잭션으로 처리
    public void createOrder(FormOrderCreateRequest request) throws Exception {
        log.info("▶ 주문 등록 요청 시작: orderDate={}, bpId={}, empId={}",
                request.getOrderDate(), request.getBpId(), request.getEmpId());

        // 1) ORD_HEADER INSERT
        int headerRows = writeMapper.insertOrderHeader(request);
        if (headerRows != 1) {
            log.error("ORD_HEADER INSERT 실패: {}", request);
            throw new Exception("주문 헤더 저장에 실패했습니다.");
        }

        // INSERT 후 request.getOrdId()에 Generated Key(ORD_ID)가 채워진다
        Long newOrdId = request.getOrdId();
        log.info("▷ ORD_HEADER INSERT 성공 (생성된 ordId={})", newOrdId);

        // 2) FormOrderDetailRequest 리스트를 순회하며 ORD_DETAIL INSERT
        List<FormOrderDetailRequest> details = request.getDetailList();
        if (details == null || details.isEmpty()) {
            log.error("주문 상세가 비어있습니다. ordId={}", newOrdId);
            throw new IllegalArgumentException("최소 한 건의 주문 상세를 입력해야 합니다.");
        }

        for (FormOrderDetailRequest detail : details) {
            // 헤더 PK(ordId)를 각 detail DTO에 세팅
            detail.setOrdId(newOrdId);

            int detailRows = writeMapper.insertOrderDetail(detail);
            if (detailRows != 1) {
                log.error("ORD_DETAIL INSERT 실패: ordId={}, detail={}", newOrdId, detail);
                throw new Exception("주문 상세 저장 중 오류가 발생했습니다.");
            }
            log.info("▷ ORD_DETAIL INSERT 성공: ordId={}, itemId={}, orderQty={}",
                    newOrdId, detail.getItemId(), detail.getOrderQty());
        }

        log.info("▶ 주문 등록 완료: ordId={}", newOrdId);
    }
}
