package com.stock.vo.req;

import com.stock.vo.ContractVO;
import com.stock.vo.OrderVO;
import lombok.Data;

@Data
public class PlaceOrderReq {
    private Integer uniqueId;
    private OrderVO order;
    private ContractVO contract;
}
