package com.example.shopService.domain;

import com.example.shopService.support.Versionable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShopEntity extends Versionable{

    private static final long serialVersionUID = 2936035955409046843L;
    private String name;
    private ShopAddress shopAddress;

    public ShopEntity() {
        super();
    }

    public ShopEntity(String name, ShopAddress shopAddress) {
        super();
        this.name = name;
        this.shopAddress = shopAddress;
    }

    @Data
    public static class ShopAddress implements Serializable {
        private static final long serialVersionUID = 6210366512436661558L;
        private String number;
        private String postCode;
        private Double longitude;
        private Double latitude;

        public ShopAddress() {
        }

        public ShopAddress(String number, String postCode) {
            this.number = number;
            this.postCode = postCode;
        }
    }
}
