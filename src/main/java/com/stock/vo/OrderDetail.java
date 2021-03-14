package com.stock.vo;

import com.ib.client.Order;
import com.ib.client.OrderState;
import lombok.Data;

@Data
public class OrderDetail {
    private int clientId;
    private int orderId;
    private int permId;
    private int parentId;
    private String action = "BUY";
    private double totalQuantity;
    private int displaySize;
    private String orderType = "LMT";
    private String tif = "DAY";
    private String account;
    private String status;

    public OrderDetail(Order order, OrderState orderState) {
        this.clientId = order.clientId();
        this.orderId = order.orderId();
        this.permId = order.permId();
        this.parentId = order.parentId();
        this.action = order.getAction();
        this.totalQuantity = order.totalQuantity();
        this.displaySize = order.displaySize();
        this.orderType = order.getOrderType();
        this.tif = order.getTif();
        this.account = order.account();
        this.status = orderState.getStatus();
    }
}
