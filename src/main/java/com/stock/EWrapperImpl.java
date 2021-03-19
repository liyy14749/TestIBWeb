/* Copyright (C) 2019 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.stock;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.ib.client.*;
import com.stock.cache.DataCache;
import com.stock.core.util.RedisUtil;
import com.stock.utils.KeyUtil;
import com.stock.vo.*;
import com.stock.vo.rsp.PnlRsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

@Service
public class EWrapperImpl implements EWrapper {

	private static Logger log = LoggerFactory.getLogger(EWrapperImpl.class);

	@Autowired
	private RedisTemplate<String, String> template;
	@Autowired
	private KeyUtil keyUtil;
	@Autowired
	private RedisUtil redisUtil;

	private EReaderSignal readerSignal;
	private EClientSocket clientSocket;
	protected int currentOrderId = -1;

	public EWrapperImpl() {
		readerSignal = new EJavaSignal();
		clientSocket = new EClientSocket(this, readerSignal);
	}
	public EClientSocket getClient() {
		return clientSocket;
	}
	
	public EReaderSignal getSignal() {
		return readerSignal;
	}
	
	public int getCurrentOrderId() {
		return currentOrderId;
	}
	
	@Override
	public void tickPrice(int tickerId, int field, double price, TickAttrib attribs) {
		log.debug("Tick Price. Ticker Id:"+tickerId+", Field: "+field+", Price: "+price+", CanAutoExecute: "+ attribs.canAutoExecute()
		+ ", pastLimit: " + attribs.pastLimit() + ", pre-open: " + attribs.preOpen());
	}
	
	@Override
	public void tickSize(int tickerId, int field, int size) {
		log.debug("Tick Size. Ticker Id:" + tickerId + ", Field: " + field + ", Size: " + size);
	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation,
							   int side, double price, int size) {
		log.debug("UpdateMarketDepth. "+tickerId+" - Position: "+position+", Operation: "+operation+", Side: "+side+", Price: "+price+", Size: "+size+"");
	}

	@Override
	public void updateMktDepthL2(int tickerId, int position,
								 String marketMaker, int operation, int side, double price, int size, boolean isSmartDepth) {
		log.debug("UpdateMarketDepthL2. "+tickerId+" - Position: "+position+", Operation: "+operation+", Side: "+side+", Price: "+price+", Size: "+size+", isSmartDepth: "+isSmartDepth);
	}

	@Override
	public void orderStatus(int orderId, String status, double filled,
							double remaining, double avgFillPrice, int permId, int parentId,
							double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
		log.debug("OrderStatus. Id: "+orderId+", Status: "+status+", Filled"+filled+", Remaining: "+remaining
                +", AvgFillPrice: "+avgFillPrice+", PermId: "+permId+", ParentId: "+parentId+", LastFillPrice: "+lastFillPrice+
                ", ClientId: "+clientId+", WhyHeld: "+whyHeld+", MktCapPrice: "+mktCapPrice);
		long time = System.currentTimeMillis()/1000;
		OrderStatusVO vo = new OrderStatusVO();
		vo.setOrderId(orderId);
		vo.setStatus(status);
		vo.setFilled(filled);
		vo.setRemaining(remaining);
		vo.setAvgFillPrice(avgFillPrice);
		vo.setPermId(permId);
		vo.setParentId(parentId);
		vo.setLastFillPrice(lastFillPrice);
		vo.setClientId(clientId);
		vo.setWhyHeld(whyHeld);
		vo.setMktCapPrice(mktCapPrice);
		vo.setTime(time);
		vo.setDate(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
		if(redisUtil.hashGet(DataCache.ORDER_MAP_KEY, orderId, OrderInfo.class)==null){
			OrderInfo o = new OrderInfo();
			o.setTime(System.currentTimeMillis()/1000);
			o.setStatusVO(vo);
			redisUtil.hashPut(DataCache.ORDER_MAP_KEY, orderId, o);
		} else {
			OrderInfo o =redisUtil.hashGet(DataCache.ORDER_MAP_KEY, orderId, OrderInfo.class);
			o.setStatusVO(vo);
			redisUtil.hashPut(DataCache.ORDER_MAP_KEY, orderId, o);
		}
		if(permId!=0){
			redisUtil.hashPut(DataCache.PERM_ID_MAP_KEY, permId, orderId);
		}
		template.opsForZSet().add(keyUtil.getKeyWithPrefix("order_msg_queue"), JSON.toJSONString(vo), time);
	}


	@Override
	public void openOrder(int orderId, Contract contract, Order order,
						  OrderState orderState) {
		log.debug(EWrapperMsgGenerator.openOrder(orderId, contract, order, orderState));
		List<OrderDetail> orderDetails = DataCache.orderCache.get(DataCache.ORDER_KEY);
		if(orderDetails !=null){
			orderDetails.add(new OrderDetail(order, orderState, contract));
		}
		if(order.orderId()!=0){
			if(redisUtil.hashGet(DataCache.ORDER_MAP_KEY, orderId, OrderInfo.class)==null){
				OrderInfo o = new OrderInfo();
				o.setTime(System.currentTimeMillis()/1000);
				o.setOrderDetail(new OrderDetail(order, orderState, contract));
				redisUtil.hashPut(DataCache.ORDER_MAP_KEY, orderId, o);
			} else {
				OrderInfo o =redisUtil.hashGet(DataCache.ORDER_MAP_KEY, orderId, OrderInfo.class);
				o.setOrderDetail(new OrderDetail(order, orderState, contract));
				redisUtil.hashPut(DataCache.ORDER_MAP_KEY, orderId, o);
			}
			if(order.permId()!=0){
				redisUtil.hashPut(DataCache.PERM_ID_MAP_KEY,order.permId(), orderId);
			}
		}
	}

	@Override
	public void openOrderEnd() {
		log.debug("OpenOrderEnd");
		CountDownLatch countDownLatch = DataCache.latchMap.get(DataCache.ORDER_KEY);
		if(countDownLatch !=null){
			countDownLatch.countDown();
		}
	}

	@Override
	public void completedOrder(Contract contract, Order order, OrderState orderState) {
		log.debug(EWrapperMsgGenerator.completedOrder(contract, order, orderState));
		List<OrderDetail> orderDetails = DataCache.orderCache.get(DataCache.ORDER_KEY);
		if(orderDetails !=null){
			orderDetails.add(new OrderDetail(order, orderState, contract));
		}
		int orderId = order.orderId();
		if(orderId != 0){
			if(redisUtil.hashGet(DataCache.ORDER_MAP_KEY, orderId, OrderInfo.class)==null){
				OrderInfo o = new OrderInfo();
				o.setTime(System.currentTimeMillis()/1000);
				o.setOrderDetail(new OrderDetail(order, orderState, contract));
				redisUtil.hashPut(DataCache.ORDER_MAP_KEY, orderId, o);
			} else {
				OrderInfo o =redisUtil.hashGet(DataCache.ORDER_MAP_KEY, orderId, OrderInfo.class);
				o.setOrderDetail(new OrderDetail(order, orderState, contract));
				redisUtil.hashPut(DataCache.ORDER_MAP_KEY, orderId, o);
			}
			if(order.permId()!=0){
				redisUtil.hashPut(DataCache.PERM_ID_MAP_KEY,order.permId(), orderId);
			}
		}
	}

	@Override
	public void completedOrdersEnd() {
		log.debug(EWrapperMsgGenerator.completedOrdersEnd());
		CountDownLatch countDownLatch = DataCache.latchMap.get(DataCache.ORDER_KEY);
		if(countDownLatch !=null){
			countDownLatch.countDown();
		}
	}

	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		log.debug(EWrapperMsgGenerator.contractDetails(reqId, contractDetails));
		TickerVO ticker = DataCache.tickerCache.get(reqId);
		if(ticker ==null){
			return;
		}
		ContractDetailsVO vo = new ContractDetailsVO();
		vo.setContractDetails(contractDetails);
		ticker.getContractDetails().add(vo);
	}
	@Override
	public void contractDetailsEnd(int reqId) {
		log.debug("ContractDetailsEnd. "+reqId+"\n");
		TickerVO ticker = DataCache.tickerCache.get(reqId);
		if(ticker ==null || ticker.getCountDown()== null){
			return;
		}
		ticker.getCountDown().countDown();
	}

	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		log.debug(EWrapperMsgGenerator.bondContractDetails(reqId, contractDetails));
	}

	@Override
	public void tickOptionComputation(int tickerId, int field,
			double impliedVol, double delta, double optPrice,
			double pvDividend, double gamma, double vega, double theta,
			double undPrice) {
		log.debug("TickOptionComputation. TickerId: "+tickerId+", field: "+field+", ImpliedVolatility: "+impliedVol+", Delta: "+delta
                +", OptionPrice: "+optPrice+", pvDividend: "+pvDividend+", Gamma: "+gamma+", Vega: "+vega+", Theta: "+theta+", UnderlyingPrice: "+undPrice);
	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		log.debug("Tick Generic. Ticker Id:" + tickerId + ", Field: " + TickType.getField(tickType) + ", Value: " + value);
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
		log.debug("Tick string. Ticker Id:" + tickerId + ", Type: " + tickType + ", Value: " + value);
	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureLastTradeDate, double dividendImpact,
			double dividendsToLastTradeDate) {
		log.debug("TickEFP. "+tickerId+", Type: "+tickType+", BasisPoints: "+basisPoints+", FormattedBasisPoints: "+
			formattedBasisPoints+", ImpliedFuture: "+impliedFuture+", HoldDays: "+holdDays+", FutureLastTradeDate: "+futureLastTradeDate+
			", DividendImpact: "+dividendImpact+", DividendsToLastTradeDate: "+dividendsToLastTradeDate);
	}


	@Override
	public void updateAccountValue(String key, String value, String currency,
			String accountName) {
		log.debug("UpdateAccountValue. Key: " + key + ", Value: " + value + ", Currency: " + currency + ", AccountName: " + accountName);
	}

	

	@Override
	public void updatePortfolio(Contract contract, double position,
			double marketPrice, double marketValue, double averageCost,
			double unrealizedPNL, double realizedPNL, String accountName) {
		log.debug("UpdatePortfolio. "+contract.symbol()+", "+contract.secType()+" @ "+contract.exchange()
                +": Position: "+position+", MarketPrice: "+marketPrice+", MarketValue: "+marketValue+", AverageCost: "+averageCost
                +", UnrealizedPNL: "+unrealizedPNL+", RealizedPNL: "+realizedPNL+", AccountName: "+accountName);
	}

	

	@Override
	public void updateAccountTime(String timeStamp) {
		log.debug("UpdateAccountTime. Time: " + timeStamp+"\n");
	}

	

	@Override
	public void accountDownloadEnd(String accountName) {
		log.debug("Account download finished: "+accountName+"\n");
	}

	

	@Override
	public void nextValidId(int orderId) {
		log.debug("Next Valid Id: ["+orderId+"]");
		DataCache.nextOrderId = orderId;
	}

	

	

	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
		log.debug("ExecDetails. "+reqId+" - ["+contract.symbol()+"], ["+contract.secType()+"], ["+contract.currency()+"], ["+execution.execId()+
		        "], ["+execution.orderId()+"], ["+execution.shares()+"]"  + ", [" + execution.lastLiquidity() + "]");
	}

	

	@Override
	public void execDetailsEnd(int reqId) {
		log.debug("ExecDetailsEnd. "+reqId+"\n");
	}

	

	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message,
			String origExchange) {
		log.debug("News Bulletins. "+msgId+" - Type: "+msgType+", Message: "+message+", Exchange of Origin: "+origExchange+"\n");
	}

	

	@Override
	public void managedAccounts(String accountsList) {
		log.debug("Account list: " +accountsList);
	}



	@Override
	public void receiveFA(int faDataType, String xml) {
		log.debug("Receiving FA: "+faDataType+" - "+xml);
	}

	

	@Override
	public void historicalData(int reqId, Bar bar) {
		//log.debug("HistoricalData. "+reqId+" - Date: "+bar.time()+", Open: "+bar.open()+", High: "+bar.high()+", Low: "+bar.low()+", Close: "+bar.close()+", Volume: "+bar.volume()+", Count: "+bar.count()+", WAP: "+bar.wap());
		TickerVO ticker = DataCache.tickerCache.get(reqId);
		if(ticker ==null){
			return;
		}
		HistoryBarVO vo = new HistoryBarVO();
		vo.setTime(bar.time());
		vo.setOpen(bar.open());
		vo.setHigh(bar.high());
		vo.setLow(bar.low());
		vo.setClose(bar.close());
		vo.setVolume(bar.volume());
		vo.setCount(bar.count());
		vo.setWap(bar.wap());
		ticker.getHistory().getBars().add(vo);
	}

	

	@Override
	public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {
		//log.debug("HistoricalDataEnd. "+reqId+" - Start Date: "+startDateStr+", End Date: "+endDateStr);
		TickerVO ticker = DataCache.tickerCache.get(reqId);
		if(ticker ==null || ticker.getCountDown()== null){
			return;
		}
		ticker.getHistory().setEndDate(endDateStr);
		ticker.getHistory().setStartDate(startDateStr);
		ticker.getCountDown().countDown();
	}

	
	

	@Override
	public void scannerParameters(String xml) {
		log.debug("ScannerParameters. "+xml+"\n");
	}

	

	@Override
	public void scannerData(int reqId, int rank,
			ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr) {
		log.debug("ScannerData. "+reqId+" - Rank: "+rank+", Symbol: "+contractDetails.contract().symbol()+", SecType: "+contractDetails.contract().secType()+", Currency: "+contractDetails.contract().currency()
                +", Distance: "+distance+", Benchmark: "+benchmark+", Projection: "+projection+", Legs String: "+legsStr);
	}

	

	@Override
	public void scannerDataEnd(int reqId) {
		log.debug("ScannerDataEnd. "+reqId);
	}

	

	@Override
	public void realtimeBar(int reqId, long time, double open, double high,
			double low, double close, long volume, double wap, int count) {
		log.debug("RealTimeBars. " + reqId + " - Time: " + time + ", Open: " + open + ", High: " + high + ", Low: " + low + ", Close: " + close + ", Volume: " + volume + ", Count: " + count + ", WAP: " + wap);
	}

	@Override
	public void currentTime(long time) {
		log.debug("currentTime");
	}

	@Override
	public void fundamentalData(int reqId, String data) {
		log.debug("FundamentalData. ReqId: ["+reqId+"] - Data: ["+data+"]");
	}

	@Override
	public void deltaNeutralValidation(int reqId, DeltaNeutralContract deltaNeutralContract) {
		log.debug("deltaNeutralValidation");
	}

	@Override
	public void tickSnapshotEnd(int reqId) {
		log.debug("TickSnapshotEnd: "+reqId);
	}

	

	@Override
	public void marketDataType(int reqId, int marketDataType) {
		log.debug("MarketDataType. ["+reqId+"], Type: ["+marketDataType+"]\n");
	}

	

	@Override
	public void commissionReport(CommissionReport commissionReport) {
		log.debug("CommissionReport. ["+commissionReport.execId()+"] - ["+commissionReport.commission()+"] ["+commissionReport.currency()+"] RPNL ["+commissionReport.realizedPNL()+"]");
	}

	

	@Override
	public void position(String account, Contract contract, double pos,
			double avgCost) {
		log.debug("Position. "+account+" - Symbol: "+contract.symbol()+", SecType: "+contract.secType()+", Currency: "+contract.currency()+", Position: "+pos+", Avg cost: "+avgCost);
	}

	

	@Override
	public void positionEnd() {
		log.debug("PositionEnd \n");
	}

	@Override
	public void accountSummary(int reqId, String account, String tag,
			String value, String currency) {
		log.debug("Acct Summary. ReqId: " + reqId + ", Acct: " + account + ", Tag: " + tag + ", Value: " + value + ", Currency: " + currency);
		TickerVO ticker = DataCache.tickerCache.get(reqId);
		if(ticker ==null || ticker.getAccountSummary() == null){
			return;
		}
		AccountSummaryVO vo = new AccountSummaryVO();
		vo.setTag(tag);
		vo.setValue(value);
		vo.setCurrency(currency);
		ticker.getAccountSummary().getSummarys().add(vo);
		ticker.getAccountSummary().setAccount(account);
	}

	@Override
	public void accountSummaryEnd(int reqId) {
		log.debug("AccountSummaryEnd. Req Id: "+reqId+"\n");
		TickerVO ticker = DataCache.tickerCache.get(reqId);
		if(ticker ==null || ticker.getCountDown()== null){
			return;
		}
		ticker.getCountDown().countDown();
	}

	@Override
	public void verifyMessageAPI(String apiData) {
		log.debug("verifyMessageAPI");
	}

	@Override
	public void verifyCompleted(boolean isSuccessful, String errorText) {
		log.debug("verifyCompleted");
	}

	@Override
	public void verifyAndAuthMessageAPI(String apiData, String xyzChallenge) {
		log.debug("verifyAndAuthMessageAPI");
	}

	@Override
	public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
		log.debug("verifyAndAuthCompleted");
	}

	@Override
	public void displayGroupList(int reqId, String groups) {
		log.debug("Display Group List. ReqId: "+reqId+", Groups: "+groups+"\n");
	}

	@Override
	public void displayGroupUpdated(int reqId, String contractInfo) {
		log.debug("Display Group Updated. ReqId: "+reqId+", Contract info: "+contractInfo+"\n");
	}

	@Override
	public void error(Exception e) {
		log.error("Exception: ", e);
	}

	@Override
	public void error(String str) {
		log.debug("Error STR");
	}

	@Override
	public void error(int id, int errorCode, String errorMsg) {
		log.debug("Error. Id: " + id + ", Code: " + errorCode + ", Msg: " + errorMsg + "\n");
		if(id == -1 && (errorCode ==2104|| errorCode ==2106|| errorCode==2158)){
			DataCache.SERVER_OK = true;
		} else if(id == -1 && errorCode == 504){
			DataCache.SERVER_OK = false;
			for(Integer key: DataCache.tickerOrderCache.keySet()){
				TickerOrderVO tickerOrderVO = DataCache.tickerOrderCache.get(key);
				tickerOrderVO.setErrorCode(504);
				tickerOrderVO.setErrorMsg(errorMsg);
				if(tickerOrderVO.getCountDown()!=null){
					tickerOrderVO.getCountDown().countDown();
				}
			}
			for(Integer key: DataCache.tickerCache.keySet()){
				TickerVO tickerVO = DataCache.tickerCache.get(key);
				tickerVO.setErrorCode(504);
				tickerVO.setErrorMsg(errorMsg);
				if(tickerVO.getCountDown()!=null){
					tickerVO.getCountDown().countDown();
				}
			}
			return;
		} else if(id == -1 && errorCode == 507){
			DataCache.SERVER_OK = false;
		}
		TickerOrderVO ticker = DataCache.tickerOrderCache.get(id);
		if(ticker !=null && ticker.getCountDown()!=null){
			ticker.setErrorCode(errorCode);
			ticker.setErrorMsg(errorMsg);
			ticker.setResult(true);
			ticker.getCountDown().countDown();
			return;
		}
		TickerVO tickerVO = DataCache.tickerCache.get(id);
		if(tickerVO !=null && tickerVO.getCountDown()!=null){
			tickerVO.setErrorCode(errorCode);
			tickerVO.setErrorMsg(errorMsg);
			tickerVO.getCountDown().countDown();
			return;
		}
	}

	@Override
	public void connectionClosed() {
		log.debug("Connection closed");
	}

	@Override
	public void connectAck() {
		if (clientSocket.isAsyncEConnect()) {
			log.debug("Acknowledging connection");
			clientSocket.startAPI();
		}
	}

	@Override
	public void positionMulti(int reqId, String account, String modelCode,
			Contract contract, double pos, double avgCost) {
		log.debug("Position Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " + modelCode + ", Symbol: " + contract.symbol() + ", SecType: " + contract.secType() + ", Currency: " + contract.currency() + ", Position: " + pos + ", Avg cost: " + avgCost + "\n");
		TickerVO ticker = DataCache.tickerCache.get(reqId);
		if(ticker ==null || ticker.getPositions() == null){
			return;
		}
		PositionVO vo = new PositionVO();
		vo.setAccount(account);
		vo.setModelCode(modelCode);
		vo.setContract(new ContractVO().parseContract(contract));
		ticker.getPositions().add(vo);
	}

	@Override
	public void positionMultiEnd(int reqId) {
		log.debug("Position Multi End. Request: " + reqId + "\n");
		TickerVO ticker = DataCache.tickerCache.get(reqId);
		if(ticker ==null || ticker.getCountDown()== null){
			return;
		}
		ticker.setFinish(true);
		ticker.getCountDown().countDown();
	}

	@Override
	public void accountUpdateMulti(int reqId, String account, String modelCode,
			String key, String value, String currency) {
		log.debug("Account Update Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " + modelCode + ", Key: " + key + ", Value: " + value + ", Currency: " + currency + "\n");
	}

	@Override
	public void accountUpdateMultiEnd(int reqId) {
		log.debug("Account Update Multi End. Request: " + reqId + "\n");
	}

	@Override
	public void securityDefinitionOptionalParameter(int reqId, String exchange,
			int underlyingConId, String tradingClass, String multiplier,
			Set<String> expirations, Set<Double> strikes) {
		log.debug("Security Definition Optional Parameter. Request: "+reqId+", Trading Class: "+tradingClass+", Multiplier: "+multiplier+" \n");
	}

	@Override
	public void securityDefinitionOptionalParameterEnd(int reqId) {
		log.debug("Security Definition Optional Parameter End. Request: " + reqId);
	}

	@Override
	public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
		for (SoftDollarTier tier : tiers) {
			log.debug("tier: " + tier.toString() + ", ");
		}
	}

    @Override
    public void familyCodes(FamilyCode[] familyCodes) {
        for (FamilyCode fc : familyCodes) {
            log.debug("Family Code. AccountID: " + fc.accountID() + ", FamilyCode: " + fc.familyCodeStr());
        }
    }

    @Override
    public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {
        log.debug("Contract Descriptions. Request: " + reqId + "\n");
        for (ContractDescription  cd : contractDescriptions) {
            Contract c = cd.contract();
            StringBuilder derivativeSecTypesSB = new StringBuilder();
            for (String str : cd.derivativeSecTypes()) {
                derivativeSecTypesSB.append(str);
                derivativeSecTypesSB.append(",");
            }
            log.debug("Contract. ConId: " + c.conid() + ", Symbol: " + c.symbol() + ", SecType: " + c.secType() + 
                    ", PrimaryExch: " + c.primaryExch() + ", Currency: " + c.currency() + 
                    ", DerivativeSecTypes:[" + derivativeSecTypesSB.toString() + "]");
        }
    }

	@Override
	public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {
		for (DepthMktDataDescription depthMktDataDescription : depthMktDataDescriptions) {
			log.debug("Depth Mkt Data Description. Exchange: " + depthMktDataDescription.exchange() +
			", ListingExch: " + depthMktDataDescription.listingExch() + 
			", SecType: " + depthMktDataDescription.secType() +
			", ServiceDataType: " + depthMktDataDescription.serviceDataType() +
			", AggGroup: " + depthMktDataDescription.aggGroup()
			);
		}
	}

	@Override
	public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline, String extraData) {
		log.debug("Tick News. TickerId: " + tickerId + ", TimeStamp: " + timeStamp + ", ProviderCode: " + providerCode + ", ArticleId: " + articleId + ", Headline: " + headline + ", ExtraData: " + extraData + "\n");
	}



	@Override
	public void smartComponents(int reqId, Map<Integer, Entry<String, Character>> theMap) {
		log.debug("smart components req id:" + reqId);
		
		for (Map.Entry<Integer, Entry<String, Character>> item : theMap.entrySet()) {
			log.debug("bit number: " + item.getKey() + 
					", exchange: " + item.getValue().getKey() + ", exchange letter: " + item.getValue().getValue());			
		}
	}

	@Override
	public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
		log.debug("Tick req params. Ticker Id:" + tickerId + ", Min tick: " + minTick + ", bbo exchange: " + bboExchange + ", Snapshot permissions: " + snapshotPermissions);
	}

	@Override
	public void newsProviders(NewsProvider[] newsProviders) {
		for (NewsProvider np : newsProviders) {
			log.debug("News Provider. ProviderCode: " + np.providerCode() + ", ProviderName: " + np.providerName() + "\n");
		}
	}

	@Override
	public void newsArticle(int requestId, int articleType, String articleText) {
		log.debug("News Article. Request Id: " + requestId + ", ArticleType: " + articleType + 
				", ArticleText: " + articleText);
	}

	@Override
	public void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {
		log.debug("Historical News. RequestId: " + requestId + ", Time: " + time + ", ProviderCode: " + providerCode + ", ArticleId: " + articleId + ", Headline: " + headline + "\n");
	}

	@Override
	public void historicalNewsEnd(int requestId, boolean hasMore) {
		log.debug("Historical News End. RequestId: " + requestId + ", HasMore: " + hasMore + "\n");
	}

	@Override
	public void headTimestamp(int reqId, String headTimestamp) {
		log.debug("Head timestamp. Req Id: " + reqId + ", headTimestamp: " + headTimestamp);
	}

	@Override
	public void histogramData(int reqId, List<HistogramEntry> items) {
		log.debug(EWrapperMsgGenerator.histogramData(reqId, items));
	}

	@Override
    public void historicalDataUpdate(int reqId, Bar bar) {
        log.debug("HistoricalDataUpdate. "+reqId+" - Date: "+bar.time()+", Open: "+bar.open()+", High: "+bar.high()+", Low: "+bar.low()+", Close: "+bar.close()+", Volume: "+bar.volume()+", Count: "+bar.count()+", WAP: "+bar.wap());
    }

	

	@Override
	public void rerouteMktDataReq(int reqId, int conId, String exchange) {
		log.debug(EWrapperMsgGenerator.rerouteMktDataReq(reqId, conId, exchange));
	}

	

	@Override
	public void rerouteMktDepthReq(int reqId, int conId, String exchange) {
		log.debug(EWrapperMsgGenerator.rerouteMktDepthReq(reqId, conId, exchange));
	}

	

	@Override
	public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {
		DecimalFormat df = new DecimalFormat("#.#");
		df.setMaximumFractionDigits(340);
		log.debug("Market Rule Id: " + marketRuleId);
		for (PriceIncrement pi : priceIncrements) {
			log.debug("Price Increment. Low Edge: " + df.format(pi.lowEdge()) + ", Increment: " + df.format(pi.increment()));
		}
	}

	

    @Override
    public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {
        log.debug(EWrapperMsgGenerator.pnl(reqId, dailyPnL, unrealizedPnL, realizedPnL));
		TickerVO ticker = DataCache.tickerCache.get(reqId);
		if(ticker ==null || ticker.getCountDown()== null){
			return;
		}
		PnlRsp pnl = new PnlRsp();
		pnl.setDailyPnL(dailyPnL);
		pnl.setUnrealizedPnL(unrealizedPnL);
		pnl.setRealizedPnL(realizedPnL);
		ticker.setPnl(pnl);
		ticker.getCountDown().countDown();
    }

	

    @Override
    public void pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {
        log.debug(EWrapperMsgGenerator.pnlSingle(reqId, pos, dailyPnL, unrealizedPnL, realizedPnL, value));                
    }

	

    @Override
    public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done) {
        for (HistoricalTick tick : ticks) {
            log.debug(EWrapperMsgGenerator.historicalTick(reqId, tick.time(), tick.price(), tick.size()));
        }
    }

	

    @Override
    public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {
        for (HistoricalTickBidAsk tick : ticks) {
            log.debug(EWrapperMsgGenerator.historicalTickBidAsk(reqId, tick.time(), tick.tickAttribBidAsk(), tick.priceBid(), tick.priceAsk(), tick.sizeBid(),
                    tick.sizeAsk()));
        }
    }   

	
    @Override

    public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {
        for (HistoricalTickLast tick : ticks) {
            log.debug(EWrapperMsgGenerator.historicalTickLast(reqId, tick.time(), tick.tickAttribLast(), tick.price(), tick.size(), tick.exchange(), 
                tick.specialConditions()));
        }
    }



   @Override
    public void tickByTickAllLast(int reqId, int tickType, long time, double price, int size, TickAttribLast tickAttribLast,
            String exchange, String specialConditions) {
        log.debug(EWrapperMsgGenerator.tickByTickAllLast(reqId, tickType, time, price, size, tickAttribLast, exchange, specialConditions));
    }



    @Override
    public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize,
            TickAttribBidAsk tickAttribBidAsk) {
        log.debug(EWrapperMsgGenerator.tickByTickBidAsk(reqId, time, bidPrice, askPrice, bidSize, askSize, tickAttribBidAsk));
    }

    

    @Override
    public void tickByTickMidPoint(int reqId, long time, double midPoint) {
        log.debug(EWrapperMsgGenerator.tickByTickMidPoint(reqId, time, midPoint));
    }



    @Override
    public void orderBound(long orderId, int apiClientId, int apiOrderId) {
        log.debug(EWrapperMsgGenerator.orderBound(orderId, apiClientId, apiOrderId));
    }


}
