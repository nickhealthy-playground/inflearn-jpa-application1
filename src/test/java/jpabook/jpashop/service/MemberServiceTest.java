package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(value = false)
public class MemberServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberService memberService;

    @Test
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setUsername("USER");

        //when
        Long saveId = memberService.join(member);

        //then
        assertThat(member).isEqualTo(memberRepository.findOne(saveId));
    }

    @org.junit.Test(expected = IllegalStateException.class)
    @Rollback
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setUsername("USER");

        Member member2 = new Member();
        member2.setUsername("USER");

        //when
        memberService.join(member1);
        memberService.join(member2);

        //then
        fail("예외 발생");

    }
}