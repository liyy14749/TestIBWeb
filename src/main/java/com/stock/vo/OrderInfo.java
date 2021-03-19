package com.stock.vo;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import lombok.Data;

@Data
public class OrderInfo {
    private Long time;
    private OrderDetail orderDetail;
    private OrderStatusVO statusVO;
}
