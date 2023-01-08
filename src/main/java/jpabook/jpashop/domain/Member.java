package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded // Embeddable 둘 중에 하나만 있어도 됨
    private Address address;

    @JsonIgnore // 양방향 호출을 막기 위해
    @OneToMany(mappedBy = "member") // orders 테이블에 있는 member에 의해 매핑된 거울일 뿐이야
    private List<Order> orders = new ArrayList<>();
}
