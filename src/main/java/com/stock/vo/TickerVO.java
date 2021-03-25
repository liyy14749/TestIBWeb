package com.stock.vo;

import com.stock.vo.rsp.AccountSummaryRsp;
import com.stock.vo.rsp.PnlRsp;
import lombok.Data;

import java.util.List;
import java.util.Map;
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
    private AccountSummaryRsp accountSummary;
    private PnlRsp pnl;
    private Map<Integer, PositionVO> positions;
    private boolean finish = false;

    public TickerVO(int tickerId) {
        this.tickerId = tickerId;
    }

    public TickerVO(int tickerId, String symbol) {
        this.tickerId = tickerId;
        this.symbol = symbol;
    }
}
