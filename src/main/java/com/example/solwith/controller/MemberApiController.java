package com.example.solwith.controller;

import com.example.solwith.domain.Member;
import com.example.solwith.dto.MemberCreateRequest;
import com.example.solwith.dto.MemberResponse;
import com.example.solwith.dto.MemberUpdateRequest;
import com.example.solwith.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberApiController {
    private final MemberService memberService;
    public MemberApiController(MemberService memberService){ this.memberService = memberService; }

    // CREATE 201 + Location
    @PostMapping
    public ResponseEntity<MemberResponse> create(@RequestBody @Valid MemberCreateRequest req){
        Member saved = memberService.join(new Member(req.getName()));
        URI location = URI.create("/api/members/" + saved.getId());
        return ResponseEntity.created(location)
                .body(new MemberResponse(saved.getId(), saved.getName()));
    }

    // READ all 200
    @GetMapping
    public List<MemberResponse> list(){
        return memberService.findMembers().stream()
                .map(m -> new MemberResponse(m.getId(), m.getName()))
                .toList();
    }

    // READ one 200
    @GetMapping("/{id}")
    public MemberResponse get(@PathVariable Long id){
        Member m = memberService.findOne(id);
        return new MemberResponse(m.getId(), m.getName());
    }

    // UPDATE 200 (또는 204)
    @PutMapping("/{id}")
    public MemberResponse update(@PathVariable Long id,
                                 @RequestBody @Valid MemberUpdateRequest req){
        Member m = memberService.update(id, req.getName());
        return new MemberResponse(m.getId(), m.getName());
    }

    // DELETE 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
