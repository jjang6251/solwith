package com.example.solwith.controller;

import com.example.solwith.domain.Member;
import com.example.solwith.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//(RestController -> Controller + ResponseBody) -> 리턴값이 객체일 경우 스프링이 바로 문자열로 찍지 않고 → 메시지 컨버터로 변환
@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    //회원 생성
    @PostMapping
    public Member createMember(@RequestBody Member member) {
        return memberService.join(member);
    }

    //전체 회원 조회
    @GetMapping
    public List<Member> getMembers() {
        return memberService.findMembers();
    }

    //단일 회원 조회
    @GetMapping("/{id}")
    public Member getMember(@PathVariable Long id) {
        return memberService.findOne(id);
    }
}
