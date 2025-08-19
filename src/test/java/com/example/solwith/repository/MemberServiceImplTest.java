package com.example.solwith.repository;

import com.example.solwith.common.NotFoundException;
import com.example.solwith.domain.Member;
import com.example.solwith.service.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

//목표: 비즈니스 로직만 검증 (스프링 컨테이너 X)
public class MemberServiceImplTest {
    @Mock MemberRepository repo; //가짜 저장소
    @InjectMocks MemberServiceImpl sut;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void join_이름으_가입하면_저장_멤버_반환() {
        //given
        Member m = new Member("Kim");
        given(repo.save(any(Member.class))).willAnswer(inv -> {
            Member mm = inv.getArgument(0);
            return mm;
        });
        //when
        Member saved = sut.join(m);

        //then
        assertThat(saved.getName()).isEqualTo("Kim");
        then(repo).should().save(any(Member.class));
    }

//    @Test
//    void findOne_존재하지_않으면_NotFoundException() {
//        // given
//        given(repo.findById(999L)).willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> sut.findOne(999L))
//                .isInstanceOf(NotFoundException.class)
//                .hasMessageContaining("999");
//    }퓨

    @Test
    void findMembers_전체조회() {
        given(repo.findAll()).willReturn(List.of(new Member("A"), new Member("B")));
        assertThat(sut.findMembers()).hasSize(2);
        then(repo).should().findAll();
    }
}
