package jpabook.jpashop.service;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateItemDto {
    // itemController > itemService 넘길 때 쓰면 좋음

    private String name;
    private int price;
    private int stockQuantity;
}
