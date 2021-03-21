package com.stock.vo;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import lombok.Data;
import org.springframework.beans.BeanUtils;

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
    private double lmtPrice;
    private double auxPrice;
    private String symbol;
    private double filled;
    private double remaining;
    private double avgFillPrice;
    private double lastFilledPrice;
    private double commission;
    private double maxCommission;
    private double minCommission;
    private String commissionCurrency;
    private String goodAfterTime;
    //YYYYMMDD hh:mm:ss CCT
    private String goodTillDate;

    public OrderDetail(){
    }
    public OrderDetail(Order order, OrderState orderState, Contract contract) {
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
        this.lmtPrice = order.lmtPrice();
        this.auxPrice = order.auxPrice();
        this.symbol = contract.symbol();
        this.commission = orderState.commission();
        this.commissionCurrency = orderState.commissionCurrency();
        this.minCommission = orderState.minCommission();
        this.maxCommission = orderState.maxCommission();
        this.goodAfterTime = order.goodAfterTime();
        this.goodTillDate = order.goodTillDate();
    }
    public void initOrderDetail(OrderDetail orderDetail) {
        BeanUtils.copyProperties(orderDetail, this);
    }
    public OrderDetail initStatus(OrderStatusVO vo){
        this.filled = vo.getFilled();
        this.remaining = vo.getRemaining();
        this.avgFillPrice = vo.getAvgFillPrice();
        this.lastFilledPrice = vo.getLastFillPrice();
        return this;
    }
}
