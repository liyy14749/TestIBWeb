package com.stock.vo;

import com.ib.client.ContractDetails;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Data
public class TickerVO {
    private int tickerId;
    private String symbol;
    private int errorCode;
    private String errorMsg;
    private HistoryVO history;
    private CountDownLatch countDown;
    private List<ContractDetailsVO> contractDetails;

    public TickerVO(int tickerId, String symbol) {
        this.tickerId = tickerId;
        this.symbol = symbol;
    }
}
