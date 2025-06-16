package kr.co.codea.notice;

import java.util.List;

import org.springframework.stereotype.Service;

import com.github.pagehelper.PageInfo;


public interface NoticeService {

    // 공지사항 목록 조회 (페이징)
    PageInfo<NoticeDTO> getPages(NoticeDTO dto, int page, int size);
    
    // 공지사항 상세 조회
    NoticeDTO getNoticeById(Long noticeId);
    
    // 공지사항 등록
    boolean insertNotice(NoticeDTO dto);
    
    // 공지사항 수정
    boolean updateNotice(NoticeDTO dto);
    
    // 공지사항 삭제
    boolean deleteNotice(Long noticeId);
    
    // 조회수 증가
    boolean increaseViews(Long noticeId);
    
    // 메인페이지용 최근 공지사항 조회
    List<NoticeDTO> getRecentNotices(int limit);
    
}
