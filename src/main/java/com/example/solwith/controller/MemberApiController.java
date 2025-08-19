package com.example.solwith.controller;


import com.example.solwith.common.ApiResponse;
import com.example.solwith.docs.schema.ApiResponseMemberResponse;
import com.example.solwith.domain.Member;
import com.example.solwith.dto.MemberCreateRequest;
import com.example.solwith.dto.MemberResponse;
import com.example.solwith.dto.MemberUpdateRequest;
import com.example.solwith.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.*;
import jakarta.servlet.http.HttpServletRequest;
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
    // API RESPONSE 포맷 개선 전
//    @GetMapping
//    public List<MemberResponse> list(){
//        return memberService.findMembers().stream()
//                .map(m -> new MemberResponse(m.getId(), m.getName()))
//                .toList();
//    }

    //API RESPONSE 포맷 개선 후 1
//    @GetMapping
//    public ApiResponse<List<MemberResponse>> list() {
//        List<MemberResponse> responses = memberService.findMembers().stream()
//                .map(m -> new MemberResponse(m.getId(), m.getName()))
//                .toList();
//        return ApiResponse.success(responses);
//    }



    //API RESPONSE 포맷 개선 2

    private String traceId(HttpServletRequest req){
        Object v = req.getAttribute("traceId");
        return v != null ? v.toString() : null;
    }

    @GetMapping
    public ApiResponse<List<MemberResponse>> list(HttpServletRequest req) {
        var data = memberService.findMembers().stream()
                .map(m -> new MemberResponse(m.getId(), m.getName()))
                .toList();
        return ApiResponse.success(data, req.getRequestURI(), traceId(req));
    }

    // READ one 200
    // API RESPONSE 포맷 개선 전
//    @GetMapping("/{id}")
//    public MemberResponse get(@PathVariable Long id){
//        Member m = memberService.findOne(id);
//        return new MemberResponse(m.getId(), m.getName());
//    }

    // API RESPONSE 포맷 개선 후 1
//    @GetMapping("/{id}")
//    public ApiResponse<MemberResponse> get(@PathVariable Long id) {
//        Member m = memberService.findOne(id);
//        return ApiResponse.success(new MemberResponse(m.getId(), m.getName()));
//    }

    // API RESPONSE 포맷 개선 후 2
    @Operation(summary = "회원 단건 조회", description = "ID로 회원을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseMemberResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseMemberResponse.class)))
    })
    @GetMapping("/{id}")
    public ApiResponse<MemberResponse> get(@PathVariable("id") Long id, HttpServletRequest req) {
        Member m = memberService.findOne(id);
        return ApiResponse.success(new MemberResponse(m.getId(), m.getName()), req.getRequestURI(), traceId(req));
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
