package com.stock.vo.rsp;

import lombok.Data;

@Data
public class PnlRsp {
    private double dailyPnL;
    private double unrealizedPnL;
    private double realizedPnL;
}
