package com.stock.service;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import com.stock.SocketTask;
import com.stock.cache.DataMap;
import com.stock.contracts.ContractSamples;
import com.stock.core.common.Result;
import com.stock.core.common.StatusCode;
import com.stock.core.util.AssertUtil;
import com.stock.core.util.ThreadPool;
import com.stock.orders.OrderSamples;
import com.stock.vo.ContractVO;
import com.stock.vo.HistoryVO;
import com.stock.vo.TickerOrderVO;
import com.stock.vo.TickerVO;
import com.stock.vo.req.PlaceOrderReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
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
        SocketTask.clientSocket.placeOrder(tid, contract, order);
        reqOpenOrders();
        try {
            tickerVO.getCountDown().await(1500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("placeOrder timeout");
            return Result.fail(StatusCode.TIME_OUT,"timeout");
        }
        DataMap.tickerOrderCache.remove(tid);
        if(tickerVO.getErrorCode() == 0){
            Result result = new Result();
            if(DataMap.orderCache.get(req.getUniqueId())!=null){
                result.put("status", DataMap.orderCache.get(req.getUniqueId()).getStatus());
            }
            return result;
        }
        return Result.fail(tickerVO.getErrorCode(),tickerVO.getErrorMsg());
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
                    SocketTask.clientSocket.reqOpenOrders();
                }
            }
        });
    }

    public Result cancelOrder(PlaceOrderReq req) {
        AssertUtil.validateEmpty(req.getUniqueId(), "uniqueId");

        int tid = req.getUniqueId();
        TickerOrderVO tickerVO = new TickerOrderVO(tid,req.getContract().getSymbol());
        tickerVO.setCountDown(new CountDownLatch(1));
        DataMap.tickerOrderCache.put(tid,tickerVO);
        SocketTask.clientSocket.cancelOrder(tid);
        reqOpenOrders();
        try {
            tickerVO.getCountDown().await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("placeOrder timeout");
            return Result.fail(StatusCode.TIME_OUT,"timeout");
        }
        DataMap.tickerOrderCache.remove(tid);
        if(tickerVO.getErrorCode() == 0){
            Result result = new Result();
            result.put("status", DataMap.orderCache.get(req.getUniqueId()).getStatus());
            return result;
        }
        return Result.fail(tickerVO.getErrorCode(),tickerVO.getErrorMsg());
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

}

