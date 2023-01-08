package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * xToOne (ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        // 이렇게 부르면
        // Order에서 Member를 부르고,
        // Member에서 또 다시 Order를 부르고 있음 -> 무한 루프

        // 결론: Member에 Order를 부르는 곳에서 @JsonIgnore 붙이기
        List<Order> all = orderRepository.findAllByString(new OrderSearch()); // 다 들고 오기
        for (Order order : all) {
            order.getMember().getName(); // 실제 name을 끌고 와야 해서 Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        // 근데 이렇게 가져오니까 필요 없는 정보들이 노출.. -> DTO로 변환하는 게 낫다
        return all;
    }

}
