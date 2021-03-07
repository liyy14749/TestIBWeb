package com.stock.vo;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.TagValue;
import lombok.Data;

import java.util.List;

@Data
public class ContractDetailsVO {
    private Contract contract;
    private String marketName;
    private double minTick;
    private int priceMagnifier;
    private String orderTypes;
    private String validExchanges;
    private int underConid;
    private String longName;
    private String contractMonth;
    private String industry;
    private String category;
    private String subcategory;
    private String timeZoneId;
    private String tradingHours;
    private String liquidHours;
    private String evRule;
    private double evMultiplier;
    private int mdSizeMultiplier;
    private List<TagValue> secIdList;
    private int aggGroup;
    private String underSymbol;
    private String underSecType;
    private String marketRuleIds;
    private String realExpirationDate;
    private String lastTradeTime;
    private String cusip;
    private String ratings;
    private String descAppend;
    private String bondType;
    private String couponType;
    private boolean callable = false;
    private boolean putable = false;
    private double coupon = 0.0D;
    private boolean convertible = false;
    private String maturity;
    private String issueDate;
    private String nextOptionDate;
    private String nextOptionType;
    private boolean nextOptionPartial = false;
    private String notes;

    public void setContractDetails(ContractDetails cd){
        this.contract = cd.contract();
        this.marketName = cd.marketName();
        this.minTick = cd.minTick();
        this.priceMagnifier = cd.priceMagnifier();
        this.orderTypes = cd.orderTypes();
        this.validExchanges = cd.validExchanges();
        this.underConid = cd.underConid();
        this.longName = cd.longName();
        this.contractMonth = cd.contractMonth();
        this.industry = cd.industry();
        this.category = cd.category();
        this.subcategory = cd.subcategory();
        this.timeZoneId = cd.timeZoneId();
        this.tradingHours = cd.tradingHours();
        this.liquidHours = cd.liquidHours();
        this.evRule = cd.evRule();
        this.evMultiplier = cd.evMultiplier();
        this.mdSizeMultiplier = cd.mdSizeMultiplier();
        this.secIdList = cd.secIdList();
        this.aggGroup = cd.aggGroup();
        this.underSymbol = cd.underSymbol();
        this.underSecType = cd.underSecType();
        this.marketRuleIds = cd.marketRuleIds();
        this.realExpirationDate = cd.realExpirationDate();
        this.lastTradeTime = cd.lastTradeTime();
        this.cusip = cd.cusip();
        this.ratings = cd.ratings();
        this.descAppend = cd.descAppend();
        this.bondType = cd.bondType();
        this.couponType = cd.couponType();
        this.callable = cd.callable();
        this.putable = cd.putable();
        this.coupon = cd.coupon();
        this.convertible = cd.convertible();
        this.maturity = cd.maturity();
        this.issueDate = cd.issueDate();
        this.nextOptionDate = cd.nextOptionDate();
        this.nextOptionType = cd.nextOptionType();
        this.nextOptionPartial = cd.nextOptionPartial();
        this.notes = cd.notes();
    }
}
