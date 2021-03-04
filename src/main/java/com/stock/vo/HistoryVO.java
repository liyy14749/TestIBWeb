package com.stock.vo;

import java.util.List;

public class HistoryVO {
    private String startDate;
    private String endDate;
    private List<HistoryBarVO> bars;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<HistoryBarVO> getBars() {
        return bars;
    }

    public void setBars(List<HistoryBarVO> bars) {
        this.bars = bars;
    }
}
