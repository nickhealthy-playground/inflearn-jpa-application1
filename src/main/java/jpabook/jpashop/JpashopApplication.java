package jpabook.jpashop;

import jakarta.annotation.PostConstruct;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.service.MemberService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
public class JpashopApplication {

	private final MemberService memberService;

	public JpashopApplication(MemberService memberService) {
		this.memberService = memberService;
	}

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	@PostConstruct
	public void initialize() {
		Member member1 = new Member();
		member1.setUsername("USER1");
		member1.setAddress(new Address("서울", "노원", "123-123"));

		Member member2 = new Member();
		member2.setUsername("USER2");
		member2.setAddress(new Address("서울", "노원", "123-123"));

		memberService.join(member1);
		memberService.join(member2);
	}
}
