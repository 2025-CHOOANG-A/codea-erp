package kr.co.codea.notice;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeServiceImpl implements NoticeService {
	
    private final NoticeMapper noticeMapper;
    
    @Override
    @Transactional(readOnly = true)
    public PageInfo<NoticeDTO> getPages(NoticeDTO dto, int page, int size) {
        PageHelper.startPage(page, size);
        List<NoticeDTO> list = noticeMapper.selectNoticeList(dto);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeDTO getNoticeById(Long noticeId) {
        return noticeMapper.selectNoticeById(noticeId);
    }

    @Override
    public boolean insertNotice(NoticeDTO dto) {
        return noticeMapper.insertNotice(dto) > 0;
    }

    @Override
    public boolean updateNotice(NoticeDTO dto) {
        return noticeMapper.updateNotice(dto) > 0;
    }

    @Override
    public boolean deleteNotice(Long noticeId) {
        return noticeMapper.deleteNotice(noticeId) > 0;
    }

    @Override
    public boolean increaseViews(Long noticeId) {
        return noticeMapper.updateViews(noticeId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDTO> getRecentNotices(int limit) {
        return noticeMapper.selectRecentNotices(limit);
    }

}
