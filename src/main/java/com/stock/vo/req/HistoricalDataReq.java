package com.stock.vo.req;

import com.stock.vo.ContractVO;
import lombok.Data;

@Data
public class HistoricalDataReq {
    private ContractVO contract;
    private String endDateTime;
    private String durationStr;
    private String barSizeSetting;
    private String whatToShow;
}
