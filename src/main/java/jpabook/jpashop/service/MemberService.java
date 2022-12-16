package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // 읽기 전용으로 조회 성능 최적화 // 데이터 변경은 꼭 트랜잭션 있어야 함
//@AllArgsConstructor
@RequiredArgsConstructor // final 있는 필드만 있는 생성자를 만들어줌
public class MemberService {

//    @Autowired // 필드 인젝션
    private final MemberRepository memberRepository; // 변경할 일 없어서 final

//    @Autowired // setter 인젝션 // 테스트 코드에서 mock 주입 가능?
//    public void setMemberRepository(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

//    @Autowired // 생성자 인젝션 (요즘 좋아져서 생성자 하나만 있으면 자동으로 autowired 해줌)
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//        // 생성할 때 완성됨
//    }
    // 요거 대신해서 AllArgsConstructor

    /**
     * 회원 가입
     */
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member); // 중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        // 이렇게 하더라도 서버 여러 대고 멀티스레드면 동시에 조회했을 때 조회 가능함
        // so, name -> 유니크 제약 조건 걸어서 제어 (??)
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 회원 전체 조회
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /**
     * 회원 단건 조회
     */
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
