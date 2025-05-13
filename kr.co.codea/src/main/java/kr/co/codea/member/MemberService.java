package kr.co.codea.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;

    public List<Member> getAllMembers() {
        return memberMapper.findAllMembers();
    }
}