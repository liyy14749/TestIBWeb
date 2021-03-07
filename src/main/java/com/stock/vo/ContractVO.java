package com.stock.vo;

import com.ib.client.Contract;
import io.swagger.models.auth.In;
import lombok.Data;

@Data
public class ContractVO {
    private String symbol;
    private String secType;
    private String currency;
    private String exchange;
    private String primaryExch;
    private String lastTradeDateOrContractMonth;
    private int conid;

    public ContractVO(){
    }

    public ContractVO(String symbol, String secType, String currency, String exchange) {
        this.symbol = symbol;
        this.secType = secType;
        this.currency = currency;
        this.exchange = exchange;
    }

    public ContractVO(String symbol, String secType, String currency, String exchange, String primaryExch) {
        this.symbol = symbol;
        this.secType = secType;
        this.currency = currency;
        this.exchange = exchange;
        this.primaryExch = primaryExch;
    }

    public Contract toContract(){
        Contract contract = new Contract();
        contract.symbol(this.getSymbol());
        contract.secType(this.getSecType());
        contract.currency(this.getCurrency());
        contract.exchange(this.getExchange());
        contract.primaryExch(this.getPrimaryExch());
        contract.conid(this.conid);
        contract.lastTradeDateOrContractMonth(lastTradeDateOrContractMonth);
        return contract;
    }
}
