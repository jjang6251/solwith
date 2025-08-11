package com.example.solwith.service;

import com.example.solwith.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberService {
    Member join(Member member);
    List<Member> findMembers();
    Member findOne(Long id);
    Member update(Long id, String name);
    void delete(Long id);
}
