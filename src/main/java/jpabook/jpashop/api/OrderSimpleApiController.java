package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        // 무조건 dto로 변환해서 보낼 것

        // 첫 번째 쿼리에서 N개의 row가 조회됨
        // 근데 생성자로 초기화 했더니 member, delivery를 n번 조회함
        // EAGER로 바꾸면 한꺼번에 들고 와서 추측할 수 없는 쿼리로 가져온다 -> fetch join 필요

       return result;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화 -> 한 번 더 쿼리를 날린다 (영속성 컨텍스트에 없으니까)
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화화
       }
    }

}
