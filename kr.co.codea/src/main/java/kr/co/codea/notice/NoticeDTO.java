package kr.co.codea.notice;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NoticeDTO {
	  private Long noticeId;          // 공지사항 ID
	    private String title;           // 제목
	    private String content;         // 내용
	    private Long authorEmpId;       // 작성자 ID
	    private String authorName;      // 작성자 이름 (조인으로 가져올 값)
	    private Integer views;          // 조회수
	    private LocalDateTime createdAt; // 작성일
	    private LocalDateTime updatedAt; // 수정일
	    private String isDeleted;       // 삭제 여부
	    
	    // 검색용 필드
	    private String keyword;         // 검색어 (제목, 내용)
	    private String startDate;       // 검색 시작일
	    private String endDate;         // 검색 종료일

}
