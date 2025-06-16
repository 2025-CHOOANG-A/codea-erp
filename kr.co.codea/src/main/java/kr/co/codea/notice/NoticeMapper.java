package kr.co.codea.notice;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NoticeMapper {
    // 공지사항 목록 조회 (페이징)
    List<NoticeDTO> selectNoticeList(NoticeDTO dto);
    
    // 공지사항 총 개수
    int selectNoticeCount(NoticeDTO dto);
    
    // 공지사항 상세 조회
    NoticeDTO selectNoticeById(@Param("noticeId") Long noticeId);
    
    // 공지사항 등록
    int insertNotice(NoticeDTO dto);
    
    // 공지사항 수정
    int updateNotice(NoticeDTO dto);
    
    // 공지사항 삭제 (논리삭제)
    int deleteNotice(@Param("noticeId") Long noticeId);
    
    // 조회수 증가
    int updateViews(@Param("noticeId") Long noticeId);
    
    // 메인페이지용 최근 공지사항 조회 (상위 5개)
    List<NoticeDTO> selectRecentNotices(@Param("limit") int limit);
    

}
