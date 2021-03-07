package com.stock.vo;

import lombok.Data;

@Data
public class PositionVO {
    private String account;
    private String modelCode;
    private ContractVO contract;
    private double pos;
    private double avgCost;
}
