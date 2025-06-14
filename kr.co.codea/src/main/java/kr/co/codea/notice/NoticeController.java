package kr.co.codea.notice;

import com.github.pagehelper.PageInfo;

import kr.co.codea.auth.dto.UserDetailsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    
    
    /**
     * 현재 로그인한 사용자의 EMP_ID 가져오기
     */
    private Long getCurrentUserEmpId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof UserDetailsDto) {
            UserDetailsDto userDetails = (UserDetailsDto) auth.getPrincipal();
            return userDetails.getEmpId();
        }
        
        return null;
    }

    /**
     * 현재 사용자가 관리자인지 확인
     */
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> 
                    grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }
    

    /**
     * 공지사항 목록 페이지
     */
    @GetMapping("/list")
    public String noticeList(Model model, NoticeDTO dto,
                           @RequestParam(name = "page", defaultValue = "1") int page,
                           @RequestParam(name = "size", defaultValue = "10") int size) {
        
        PageInfo<NoticeDTO> pageInfo = noticeService.getPages(dto, page, size);
        
        model.addAttribute("notices", pageInfo.getList());
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("searchDto", dto);
        model.addAttribute("isAdmin", isAdmin());

        model.addAttribute("templateName", "notice/notice_list");
        model.addAttribute("fragmentName", "contentFragment");
        
        return "notice/notice_default";
    }

    /**
     * 공지사항 상세 보기
     */
    @GetMapping("/{noticeId}")
    public String noticeDetail(@PathVariable (value = "noticeId")Long noticeId, Model model) {
        // 조회수 증가
        noticeService.increaseViews(noticeId);
        
        NoticeDTO notice = noticeService.getNoticeById(noticeId);
        if (notice == null) {
            return "redirect:/notice/list?error=notfound";
        }
        
        model.addAttribute("notice", notice);
        model.addAttribute("templateName", "notice/notice_detail");
        model.addAttribute("fragmentName", "contentFragment");
        
        return "notice/notice_default";
    }

    /**
     * 공지사항 작성 페이지 (관리자만)
     */
    @GetMapping("/write")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String noticeWrite(Model model) {
        model.addAttribute("notice", new NoticeDTO());
        model.addAttribute("templateName", "notice/notice_write");
        model.addAttribute("fragmentName", "contentFragment");
        
        return "notice/notice_default";
    }

    /**
     * 공지사항 등록 처리 (관리자만)
     */
    @PostMapping("/write")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String noticeInsert(NoticeDTO dto) {
        // 현재 로그인한 사용자의 EMP_ID 설정
        Long currentUserEmpId = getCurrentUserEmpId();
        
        if (currentUserEmpId != null) {
            dto.setAuthorEmpId(currentUserEmpId);
        } else {
            // EMP_ID를 가져올 수 없는 경우 기본값 또는 에러 처리
            dto.setAuthorEmpId(1L); // 기본값
        }
        
        if (noticeService.insertNotice(dto)) {
            return "redirect:/notice/list?success=insert";
        } else {
            return "redirect:/notice/write?error=insert";
        }
    }

    /**
     * 공지사항 수정 페이지 (관리자만)
     */
    @GetMapping("/{noticeId}/edit")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String noticeEdit(@PathVariable Long noticeId, Model model) {
        NoticeDTO notice = noticeService.getNoticeById(noticeId);
        if (notice == null) {
            return "redirect:/notice/list?error=notfound";
        }
        
        model.addAttribute("notice", notice);
        model.addAttribute("templateName", "notice/notice_edit");
        model.addAttribute("fragmentName", "contentFragment");
        
        return "notice/notice_default";
    }

    /**
     * 공지사항 수정 처리 (관리자만)
     */
    @PostMapping("/{noticeId}/edit")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String noticeUpdate(@PathVariable Long noticeId, NoticeDTO dto) {
        dto.setNoticeId(noticeId);
        
        if (noticeService.updateNotice(dto)) {
            return "redirect:/notice/" + noticeId + "?success=update";
        } else {
            return "redirect:/notice/" + noticeId + "/edit?error=update";
        }
    }

    /**
     * 공지사항 삭제 처리 (관리자만)
     */
    @PostMapping("/{noticeId}/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> noticeDelete(@PathVariable Long noticeId) {
        Map<String, Object> result = new HashMap<>();
        
        if (noticeService.deleteNotice(noticeId)) {
            result.put("success", true);
            result.put("message", "공지사항이 삭제되었습니다.");
        } else {
            result.put("success", false);
            result.put("message", "삭제에 실패했습니다.");
        }
        
        return ResponseEntity.ok(result);
    }
}