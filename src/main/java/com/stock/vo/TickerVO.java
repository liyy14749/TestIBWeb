package com.stock.vo;

public class TickerVO {
    private int tickerId;
    private String symbol;
    private boolean result;
    private HistoryVO history;

    public TickerVO(int tickerId, String symbol) {
        this.tickerId = tickerId;
        this.symbol = symbol;
    }

    public TickerVO(int tickerId, String symbol, HistoryVO history) {
        this.tickerId = tickerId;
        this.symbol = symbol;
        this.history = history;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public HistoryVO getHistory() {
        return history;
    }

    public void setHistory(HistoryVO history) {
        this.history = history;
    }

    public int getTickerId() {
        return tickerId;
    }

    public void setTickerId(int tickerId) {
        this.tickerId = tickerId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
