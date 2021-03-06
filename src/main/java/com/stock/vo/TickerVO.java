package com.stock.vo;

import lombok.Data;
import java.util.concurrent.CountDownLatch;

@Data
public class TickerVO {
    private int tickerId;
    private String symbol;
    private boolean result;
    private int errorCode;
    private String errorMsg;
    private HistoryVO history;
    private CountDownLatch countDown;

    public TickerVO(int tickerId, String symbol) {
        this.tickerId = tickerId;
        this.symbol = symbol;
    }
}
