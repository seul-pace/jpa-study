package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems(); // orderItem 초기화
            orderItems.stream().forEach(o -> o.getItem().getName()); // item 초기화
            // 강제 초기화 해준 거임! hibernate5로 초기화가 안 되는데,
            // 이렇게 따로 불러와서 강제 초기화를 ...!
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        // Entity를 외부로 노출하지 않게 하기 위해 이런 식으로 dto로 포장해서 보여주기
        // Order랑 OrderItem을 다 dto로 변환함
        // 이렇게 하면,
        // order 1번, member 1번, delivery 1번, orderItem 1번, item n번을 조회한다 => 최소 5번을 조회하게 됨
        return result;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        // DB 입장에서는 join을 했기 때문에 똑같은 주문이 2건 나온다
        // JPA는 판단하지 못 하고, DB가 전해준대로 보여준다
        // 1:n 관계가 있어 쿼리가 많이 날아가니까 개선하자..!

        // => 그래서~~~ 쿼리에 distinct를 넣는다
        // 근데 db에서 distinct는 뭐 하나라도 다르면 중복이 아니라고 생각하거든요
        // 근데 JPA에서 자체적으로 distinct가 있으면 Order가 같은 id 값이면 중복을 제거해준다

        // 근데 1:n을 페치조인 하는 순간
        // ** 페이징이 안 된다 **
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
//        private List<OrderItem> orderItems; // 이렇게 똑같이 entity를 노출하면 안 된다
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
//            order.getOrderItems().stream().forEach(o -> o.getItem().getName()); // 초기화 하고 돌리기
//            orderItems = order.getOrderItems();
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {

        private String itemName; // 상품명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
