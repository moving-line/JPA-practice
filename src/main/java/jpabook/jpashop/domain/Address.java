package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Address {

    private String city;
    private String street;
    private String zipcode;

    public Address(String city, String strret, String zipcode) {
        this.city = city;
        this.street = strret;
        this.zipcode = zipcode;
    }
}
