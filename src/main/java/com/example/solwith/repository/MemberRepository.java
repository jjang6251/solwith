package com.example.solwith.repository;

import com.example.solwith.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);

    //값이 있을 수도 있고 없을 수도 있는 컨테이너 클래스
    Optional<Member> findById(Long id);

    //같은 값을 중복해서 넣을 수 있고, 인덱스(0부터 시작)로 접근할 수 있습니다.
    List<Member> findAll();

    void deleteById(Long id);


}
