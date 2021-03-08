package com.stock.service;

import com.ib.client.*;
import com.stock.SocketTask;
import com.stock.cache.DataMap;
import com.stock.constants.CommonConstants;
import com.stock.core.common.Result;
import com.stock.core.common.StatusCode;
import com.stock.core.util.AssertUtil;
import com.stock.core.util.ThreadPool;
import com.stock.vo.ContractDetailsVO;
import com.stock.vo.ContractVO;
import com.stock.vo.TickerOrderVO;
import com.stock.vo.TickerVO;
import com.stock.vo.req.PlaceOrderReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xjp
 * @since 2020-07-12
 */
@Service
public class OrderServiceImpl {

    @Autowired SocketTask socketTask;

    private static Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    public Result placeOrder(PlaceOrderReq req) {
        AssertUtil.validateEmpty(req.getUniqueId(), "uniqueId");
        AssertUtil.validateEmpty(req.getContract(), "contract");
        AssertUtil.validateEmpty(req.getContract().getSymbol(), "symbol");
        AssertUtil.validateEmpty(req.getContract().getSecType(), "secType");
        AssertUtil.validateEmpty(req.getContract().getCurrency(), "currency");
        AssertUtil.validateEmpty(req.getContract().getExchange(), "exchange");

        AssertUtil.validateEmpty(req.getOrder(), "order");
        AssertUtil.validateEmpty(req.getOrder().getAction(), "action");
        AssertUtil.validateEmpty(req.getOrder().getOrderType(), "orderType");
        AssertUtil.validateEmpty(req.getOrder().getTotalQuantity(), "totalQuantity");

        Contract contract = new Contract();
        contract.symbol(req.getContract().getSymbol());
        contract.secType(req.getContract().getSecType());
        contract.currency(req.getContract().getCurrency());
        contract.exchange(req.getContract().getExchange());
        contract.primaryExch(req.getContract().getPrimaryExch());

        Order order = new Order();
        order.action(req.getOrder().getAction());
        order.orderType(req.getOrder().getOrderType());
        order.totalQuantity(req.getOrder().getTotalQuantity());
        order.lmtPrice(req.getOrder().getLmtPrice()); // optional

        int tid = req.getUniqueId();
        TickerOrderVO tickerVO = new TickerOrderVO(tid,req.getContract().getSymbol());
        tickerVO.setCountDown(new CountDownLatch(1));
        DataMap.tickerOrderCache.put(tid,tickerVO);
        socketTask.getClientSocket().placeOrder(tid, contract, order);
        reqOpenOrders();
        try {
            tickerVO.getCountDown().await(CommonConstants.ORDER_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("placeOrder timeout");
        }
        DataMap.tickerOrderCache.remove(tid);
        if(tickerVO.getErrorCode() == 0 && DataMap.orderCache.get(req.getUniqueId())!= null){
            Result result = new Result();
            if(DataMap.orderCache.get(req.getUniqueId())!=null){
                result.put("status", DataMap.orderCache.get(req.getUniqueId()).getStatus());
            }
            return result;
        } else if(tickerVO.getErrorCode() != 0){
            return Result.fail(tickerVO.getErrorCode(),tickerVO.getErrorMsg());
        }
        return Result.fail(StatusCode.TIME_OUT,"timeout");
    }

    private void reqOpenOrders(){
        ThreadPool.getExecutorService().submit(new Thread(){
            @Override
            public void run() {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("reqOpenOrders InterruptedException");
                }
                if(!DataMap.reqOpenOrders.getAndSet(true)){
                    socketTask.getClientSocket().reqOpenOrders();
                }
            }
        });
    }

    public Result cancelOrder(PlaceOrderReq req) {
        AssertUtil.validateEmpty(req.getUniqueId(), "uniqueId");

        int tid = req.getUniqueId();
        TickerOrderVO tickerVO = new TickerOrderVO(tid, null);
        tickerVO.setCountDown(new CountDownLatch(1));
        DataMap.tickerOrderCache.put(tid,tickerVO);
        socketTask.getClientSocket().cancelOrder(tid);
        reqOpenOrders();
        try {
            tickerVO.getCountDown().await(CommonConstants.SEARCH_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("placeOrder timeout");
        }
        DataMap.tickerOrderCache.remove(tid);
        if(tickerVO.getErrorCode() == 0 && DataMap.orderCache.get(req.getUniqueId())!=null){
            Result result = new Result();
            result.put("status", DataMap.orderCache.get(req.getUniqueId()).getStatus());
            return result;
        } else if(tickerVO.getErrorCode() != 0){
            return Result.fail(tickerVO.getErrorCode(),tickerVO.getErrorMsg());
        }
        return Result.fail(StatusCode.TIME_OUT,"timeout");
    }

    public Result orderStatus(PlaceOrderReq req) {
        AssertUtil.validateEmpty(req.getUniqueId(), "uniqueId");
        OrderState orderState = DataMap.orderCache.get(req.getUniqueId());
        Result result = new Result();
        if(orderState!=null){
            result.put("status",orderState.getStatus());
        }
        return result;
    }

    public Result reqOrderId() {
        Result result = new Result();
        result.put("nextOrderId", DataMap.nextOrderId);
        return result;
    }

    public Result reqContractDetails(ContractVO req) {
        AssertUtil.validateAllEmpty("symbol and conid",req.getSymbol(),req.getConid());

        int tid = SocketTask.tickerId.incrementAndGet();
        TickerVO tickerVO = new TickerVO(tid,req.getSymbol());
        tickerVO.setCountDown(new CountDownLatch(1));
        List<ContractDetailsVO> details = new ArrayList<>();
        tickerVO.setContractDetails(details);
        DataMap.tickerCache.put(tid,tickerVO);

        socketTask.getClientSocket().reqContractDetails(tid, req.toContract());
        try {
            tickerVO.getCountDown().await(CommonConstants.SEARCH_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("reqContractDetails timeout");
        }
        DataMap.tickerCache.remove(tid);
        if(tickerVO.getErrorCode() == 0 && details.size()>0){
            Result result = new Result();
            result.put("contractDetails",tickerVO.getContractDetails());
            return result;
        } else if(tickerVO.getErrorCode() != 0){
            return Result.fail(tickerVO.getErrorCode(),tickerVO.getErrorMsg());
        }
        return Result.fail(StatusCode.TIME_OUT,"timeout");
    }
}

