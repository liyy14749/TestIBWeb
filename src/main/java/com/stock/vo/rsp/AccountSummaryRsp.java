package com.stock.vo.rsp;

import com.stock.vo.AccountSummaryVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AccountSummaryRsp {
    private String account;
    private List<AccountSummaryVO> summarys = new ArrayList<>();
}
