package com.market.jw.domain.customer;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@ToString
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Embedded
    private Address address;

    /**
     * 고객 객체를 생성하는 팩토리 메서드
     *
     * @param name    고객 이름
     * @param address 주소 정보
     * @return 생성된 Customer 객체
     */
    public static Customer create(String name, Address address) {
        return Customer.builder()
                .name(name)
                .address(address)
                .build();
    }
}
