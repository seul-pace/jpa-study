package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

// 값 타입: immutable 해야 함
@Embeddable // 어딘가에 내장이 될 수 있다
@Getter
public class Address {

    private String city;

    private String street;

    private String zipcode;

    // 함부로 생성하면 안 되겠다는 느낌으로..
    protected Address() {
    }

    public Address(String city, String street, String zipcode) { // command + n 단축키 외워라
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
