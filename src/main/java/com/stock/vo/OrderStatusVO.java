package com.stock.vo;

import lombok.Data;

@Data
public class OrderStatusVO {
    private int orderId;
    private String status;
    private double filled;
    private double remaining;
    private double avgFillPrice;
    private int permId;
    private int parentId;
    private double lastFillPrice;
    private int clientId;
    private String whyHeld;
    private double mktCapPrice;
}
