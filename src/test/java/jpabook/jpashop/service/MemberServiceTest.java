package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.Test;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * 테스트 요구사항
 * <p>
 * - 회원가입을 성공해야 한다.
 * - 회원가입 할 때 같은 이름이 있으면 예외가 발생해야 한다.
 */
@RunWith(SpringRunner.class)    //스프링과 테스트 통합
@SpringBootTest // 스프링 부트 띄우고 테스트(이게 없으면 `@Autowired` 다 실패)
@Transactional  // 테스트에서 해당 애노테이션을 사용하면 기본적으로 테스트 이후 ROLLBACK을 진행한다.
public class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    @Test
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("JOO");

        //when
        Long saveId = memberService.join(member);

        //then
        assertEquals(member, memberRepository.findOne(saveId));
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("JOO");

        Member member2 = new Member();
        member2.setName("JOO");

        //when
        memberService.join(member1);
        memberService.join(member2);    // 예외가 발생해야 함

        //then
        fail("예외가 발생해야 한다.");

    }

}