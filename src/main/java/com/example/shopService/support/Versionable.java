package com.example.shopService.support;

import lombok.Data;

import java.io.Serializable;

@Data
public abstract class Versionable  implements Serializable {
    private static final long serialVersionUID = -3379543750853323374L;
    private Long version;
}
