package com.example.solwith.controller;

import com.example.solwith.domain.Member;
import com.example.solwith.dto.MemberResponse;
import com.example.solwith.service.MemberService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/members")
public class MemberQueryController {
    private final MemberService memberService;

    public MemberQueryController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/search")
    public List<MemberResponse> search (
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort // ex) "name, desc" 또는 "id, asc"
    ) {
        // 1) 기본 리스트 조회
        List<Member> all = memberService.findMembers();

        // 2) 필터(검색)
        if (name != null && !name.isBlank()) {
            String keyword = name.toLowerCase();
            all = all.stream()
                    .filter(m -> m.getName() != null && m.getName().toLowerCase().contains(keyword))
                    .toList();
        }

        // 3) 정렬
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String field = parts[0];
            boolean desc = parts.length > 1 && parts[1].equalsIgnoreCase("desc");
            Comparator<Member> comp = switch (field) {
                case "name" -> Comparator.comparing(m -> m.getName() == null ? "" : m.getName());
                case "id" -> Comparator.comparing(m -> m.getId() == null ? Long.MIN_VALUE : m.getId());
                default -> Comparator.comparing(m -> m.getId() == null ? Long.MIN_VALUE : m.getId());
            };
            if (desc) comp = comp.reversed();
            all = all.stream().sorted(comp).toList();
        }
        // 4) 페이징(0-based)
        int from = Math.max(page, 0) * Math.max(size, 1);
        int to = Math.min(from + Math.max(size, 1), all.size());
        if (from >= all.size()) return List.of(); // 빈 페이지

        return all.subList(from, to).stream()
                .map(m -> new MemberResponse(m.getId(), m.getName()))
                .toList();
    }
}
