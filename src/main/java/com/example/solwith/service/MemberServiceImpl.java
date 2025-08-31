package com.example.solwith.service;

import com.example.solwith.aop.LogExecutionTime;
import com.example.solwith.domain.Member;
import com.example.solwith.repository.MemberRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    @Override
    public Member join(Member member) {
        //중복 이름 체크 같은 비즈니스 룰은 여기서
        return memberRepository.save(member);
    }

    @Override
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('MANGER', 'ADMIN')") // MANAGER 이상
    @LogExecutionTime
    @Override
    public Member findOne(Long id) {
        return memberRepository.findById(id).orElse(null);
    }

    @PreAuthorize("isAuthenticated()") // 로그인 사용자면 OK
    @Override
    public Member update(Long id, String name) {
        Member m = findOne(id);
        m.setName(name);
        return m;
    }

    @PreAuthorize("hasRole('ADMIN')") //ADMIN만 접근 가능
    @Override
    public void delete(Long id) {
        Member m = findOne(id);
        m.setName("[DELETED]");
    }


}
