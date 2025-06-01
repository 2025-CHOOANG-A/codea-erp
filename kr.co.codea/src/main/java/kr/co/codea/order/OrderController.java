package kr.co.codea.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes; // 현재 사용되지 않음

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 주문 관련 웹 요청을 처리하는 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 목록 페이지를 조회합니다.
     *
     * @param keyword 검색 키워드 (거래처명 또는 제품명)
     * @param status 주문 상태 (진행중, 완료 등)
     * @param startDate 검색 시작일
     * @param endDate 검색 종료일
     * @param page 페이지 번호 (0부터 시작)
     * @param model 뷰에 전달할 모델 데이터
     * @return 주문 목록 뷰 이름
     */
    @GetMapping
    public String getOrderList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model
    ) {
        log.info("Order list requested - keyword: {}, status: {}, startDate: {}, endDate: {}, page: {}",
                keyword, status, startDate, endDate, page);

        try {
            // 페이지 크기 설정
            int size = 10; // 필요시 설정 파일에서 읽어오거나 상수로 관리

            // 검색 조건 유효성 검증
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                log.warn("Invalid date range: startDate {} is after endDate {}", startDate, endDate);
                model.addAttribute("errorMessage", "시작일이 종료일보다 늦을 수 없습니다.");
                // 날짜 조건을 무시하고 조회 (또는 에러 페이지로 리다이렉트)
                startDate = null;
                endDate = null;
            }

            // 주문 목록 조회
            List<OrderDto.OrderDetailView> content = orderService.getPagedOrders(
                    keyword, status, startDate, endDate, page, size
            );

            // 전체 개수 조회
            int totalCount = orderService.getOrderCount(keyword, status, startDate, endDate);
            int totalPages = (totalCount == 0) ? 0 : (int) Math.ceil((double) totalCount / size);


            // 페이지 번호 유효성 검증 (데이터가 있을 때만)
            if (totalPages > 0 && page >= totalPages ) {
                 log.warn("Invalid page number: {}. Total pages: {}. Redirecting to last page.", page, totalPages);
                 return "redirect:/order?page=" + (totalPages - 1) +
                        buildQueryString(keyword, status, startDate, endDate);
            } else if (page < 0) {
                log.warn("Invalid page number: {}. Redirecting to first page.", page);
                return "redirect:/order?page=0" +
                        buildQueryString(keyword, status, startDate, endDate);
            }


            // 모델에 데이터 추가
            model.addAttribute("pagedOrderDetails",
                    new OrderDto.PagingResult<>(content, page, totalPages));
            model.addAttribute("param",
                    new OrderDto.SearchParam(keyword, status, startDate, endDate));

            // 검색 결과 통계
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("currentPageSize", content.size());

            log.info("Order list loaded successfully - {} items found, page {}/{} (Total items: {})",
                    content.size(), page + 1, totalPages == 0 ? 1 : totalPages, totalCount);


            return "order/order_list"; // Thymeleaf 템플릿 경로

        } catch (RuntimeException e) { // 서비스에서 RuntimeException을 던질 수 있으므로 구체화
            log.error("Error occurred while loading order list: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "주문 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("pagedOrderDetails",
                    new OrderDto.PagingResult<>(List.of(), 0, 0)); // 빈 결과 표시
            model.addAttribute("param",
                    new OrderDto.SearchParam(keyword, status, startDate, endDate));
            return "order/order_list";
        } catch (Exception e) { // 그 외 예외 처리
            log.error("Unexpected error occurred while loading order list", e);
            model.addAttribute("errorMessage", "예상치 못한 오류로 주문 목록을 불러올 수 없습니다. 관리자에게 문의하세요.");
            model.addAttribute("pagedOrderDetails",
                    new OrderDto.PagingResult<>(List.of(), 0, 0));
            model.addAttribute("param",
                    new OrderDto.SearchParam(keyword, status, startDate, endDate));
            return "order/order_list";
        }
    }

    /**
     * 주문 등록 페이지로 이동합니다.
     *
     * @return 주문 등록 뷰 이름
     */
    @GetMapping("/register")
    public String registerOrderForm(Model model) {
        log.info("Order registration form requested");

        // TODO: 주문 등록에 필요한 데이터 설정 (거래처 목록, 제품 목록 등)
        // model.addAttribute("businessPartners", businessPartnerService.getAllBusinessPartners());
        // model.addAttribute("products", productService.getAllProducts());

        return "order/order_register"; // Thymeleaf 템플릿 경로
    }

    /**
     * 가출고 처리를 위한 API 엔드포인트입니다.
     * 주문 목록 화면의 '출고' 버튼 클릭 시 호출됩니다.
     *
     * @param request 가출고 요청 정보를 담은 DTO (JSON 바디)
     * @return 처리 결과 (성공 또는 실패 메시지)
     */
    @PostMapping("/api/shipment") // 기존 프론트엔드 JavaScript의 fetch URL과 일치
    @ResponseBody // 이 어노테이션은 반환 값을 HTTP 응답 본문으로 직접 사용하도록 합니다 (주로 JSON)
    public ResponseEntity<?> handleProvisionalShipment(@RequestBody OrderDto.ProvisionalShipmentRequest request) {
        log.info("가출고 처리 요청 수신: ordId={}, orderDetailId={}, itemId={}, quantity={}, whId={}",
                request.getOrdId(), request.getOrderDetailId(), request.getItemId(), request.getQuantity(), request.getWhId());

        try {
            // empId는 서비스 레이어나 Security Context에서 설정하는 것이 더 일반적입니다.
            // 클라이언트에서 직접 empId를 보내는 것은 보안상 위험할 수 있습니다.
            // 예시: request.setEmpId(SecurityUtil.getCurrentUserId());

            if (request.getWhId() == null) {
                 log.warn("가출고 요청 시 창고 ID(whId)가 누락되었습니다. DTO: {}", request);
                 // 기본 창고 ID를 설정하거나, 클라이언트에게 명시적인 입력을 요구할 수 있습니다.
                 // 여기서는 예시로 특정 ID를 설정하거나, 필수 값으로 간주하고 에러 처리합니다.
                 // request.setWhId(1L); // 예시: 기본 창고 ID
                 return ResponseEntity.badRequest().body(Map.of("success", false, "message", "창고 ID(whId)가 필요합니다."));
            }


            orderService.processProvisionalShipment(request);
            log.info("가출고 처리 및 주문 완료 처리 성공: ordId={}", request.getOrdId());
            return ResponseEntity.ok(Map.of("success", true, "message", "가출고 처리 및 주문 완료 처리가 성공적으로 완료되었습니다."));

        } catch (IllegalArgumentException e) { // 서비스에서 whId 누락 등으로 발생 가능
            log.error("가출고 처리 중 유효하지 않은 파라미터: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (RuntimeException e) { // 서비스에서 DB 작업 중 발생 가능 (데이터 무결성 등)
            log.error("가출고 처리 중 런타임 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("success", false, "message", "가출고 처리 중 오류가 발생했습니다: " + e.getMessage()));
        } catch (Exception e) { // 그 외 모든 예외
            log.error("가출고 처리 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("success", false, "message", "가출고 처리 중 예상치 못한 오류가 발생했습니다. 관리자에게 문의하세요."));
        }
    }


    /**
     * 쿼리 스트링을 생성하는 헬퍼 메서드
     *
     * @param keyword 검색 키워드
     * @param status 주문 상태
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 쿼리 스트링
     */
    private String buildQueryString(String keyword, String status, LocalDate startDate, LocalDate endDate) {
        StringBuilder query = new StringBuilder();

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append("&keyword=").append(keyword);
        }
        if (status != null && !status.trim().isEmpty()) {
            query.append("&status=").append(status);
        }
        if (startDate != null) {
            query.append("&startDate=").append(startDate.toString());
        }
        if (endDate != null) {
            query.append("&endDate=").append(endDate.toString());
        }

        return query.toString();
    }
}