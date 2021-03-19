package com.stock.vo;

import lombok.Data;

@Data
public class OrderVO {

    private String action;
    private String orderType;
    private Double totalQuantity;
    private Double lmtPrice;
    private String tif;
}
