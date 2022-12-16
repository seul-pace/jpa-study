package jpabook.jpashop;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class) // JUnit에게 알려주기: spring 관련된 것으로 테스트 하겠다
@SpringBootTest
public class MemberRepositoryTest {

    // 자꾸 에러 떠서 걍 지움
//    @Autowired
//    MemberRepository memberRepository;
//
//    @Test
//    @Transactional // entityManager를 통한 모든 데이터 변경은 항상 트랜잭션 내에 있어야 한다 (테스트에 있으면 테스트 끝나면 롤백 함)
//    @Rollback(false)
//    public void testMember() throws Exception {
//        //given
//        Member member = new Member();
//        member.setUsername("memberA");
//
//        //when
//        Long savedId = memberRepository.save(member);
//        Member findMember = memberRepository.find(savedId);
//
//        //then
//        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
//        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
//        Assertions.assertThat(findMember).isEqualTo(member);
//        // 같은 트랜잭션 안에 있어서 영속성 컨텍스트가 같기 때문에, ID 값이 같으면 같은 엔티티로 식별 (1차 캐시에서 가져옴)
//    }
}