package jpabook.jpashop.member;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
    @Rollback(false)
    public void memberTest() {
        //given
        Member member = new Member();
        member.setUsername("memberA");

        //when
        memberRepository.save(member);
        Member findMember = memberRepository.findById(member.getId()).orElse(null);

        //then
        assertThat(findMember.getId()).isEqualTo(member.getId()) ;
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername()) ;
        assertThat(findMember).isEqualTo(member);
    }
}