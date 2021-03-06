package com.stock.vo;

import lombok.Data;

import java.util.concurrent.CountDownLatch;

@Data
public class TickerOrderVO {
    private int tickerId;
    private String symbol;
    private boolean result;
    private int errorCode;
    private String errorMsg;
    private HistoryVO history;
    private CountDownLatch countDown;

    public TickerOrderVO(int tickerId, String symbol) {
        this.tickerId = tickerId;
        this.symbol = symbol;
    }
}
