package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /*
    흐름
    1) entity 조회, orderItem을 지연 로딩으로 가져와서 Order로 돌려줌
    2) 그건 위험해! Dto로 감싸자~ 이렇게 했더니 연결된 거 다 따로 쿼리를 더 돌리네...
    3) 그럼 fetchJoin으로 한번에 가져오자 -> 이렇게 했더니 1:n은 n을 기준으로 가져와서 중복된 데이터가 나오네? 머여 페이징도 못 하네
    4) 하... 그럼 fetchJoin으로 일단 1:1만 가져와! 그리고 default_batch_fetch_size를 이용해서 한꺼번에 쿼리 날리자
     */

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
                .collect(toList());
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
                .collect(toList());
        return result;
    }

    /**
     * 많은 데이터를 페이징 처리 하면서 가져와야 하는데,
     * 다(N)를 기준으로 row가 생성된다
     * 하이버네이트는 DB데이터를 기준으로 메모리에서 페이징을 시도
     * -> 개선 필요
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit)
    {
        // ToOne 관계는 다 fetchJoin으로 가져오기 -> 페이징에 영향을 주지 않음
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        // 그리고 이거 부를 때 application.yml 내 betchSize 옵션을 줘서, 가져올 때 in 쿼리로 가져와서 적게 쿼리 날림
        // 하이버네이트 대단한 놈임
        // 1:n:m을 1:1:1로 바꿔주는~
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;

        // 참고로 batchSize는 100~1000으로 추천하는데,
        // 1000으로 하면 한번에 가져올 때 부하가 많이 온다...
        // 그렇다고 너무 적게 돌면 시간이 오래 걸리겠지?
        // 적정한.. 개수를 하자 (was랑 db가 순간 부하를 버틸 수 있다면 1000)
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
        /*
        최초 쿼리(=루트) 1번, 컬렉션 n번 실행
        toMany 관계는 최적화가 어려워서 별도의 메소드로 해서 조회한다~
         */
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        // 중복 되게 한번에 조회해오고 OrderQueryDto에 맞게 다 발라내기~

        // 발라내기 굉장히 힘들어 보이네요
        // 페이징이 안 된대요
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(toList());
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
                    .collect(toList());
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
