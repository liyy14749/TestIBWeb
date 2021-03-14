package com.stock.service;

import com.ib.client.*;
import com.stock.SocketTask;
import com.stock.cache.DataCache;
import com.stock.constants.CommonConstants;
import com.stock.core.common.Result;
import com.stock.core.common.StatusCode;
import com.stock.core.util.AssertUtil;
import com.stock.vo.*;
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
 * 服务实现类
 * </p>
 *
 * @author xjp
 * @since 2020-07-12
 */
@Service
public class OrderServiceImpl {
    private static Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    SocketTask socketTask;

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
        TickerOrderVO tickerVO = new TickerOrderVO(tid, req.getContract().getSymbol());
        tickerVO.setCountDown(new CountDownLatch(1));
        DataCache.tickerOrderCache.put(tid, tickerVO);
        socketTask.getClientSocket().placeOrder(tid, contract, order);
        //reqOpenOrders();
        try {
            tickerVO.getCountDown().await(CommonConstants.ORDER_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("placeOrder timeout");
        }
        DataCache.tickerOrderCache.remove(tid);
        if (tickerVO.getErrorCode() == 0) {
            Result result = new Result();
            return result;
        } else if (tickerVO.getErrorCode() != 0) {
            return Result.fail(tickerVO.getErrorCode(), tickerVO.getErrorMsg());
        }
        return Result.ok();
    }

    /*private void reqOpenOrders(){
        ThreadPool.getExecutorService().submit(new Thread(){
            @Override
            public void run() {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("reqOpenOrders InterruptedException");
                }
                if(!DataCache.reqOpenOrders.getAndSet(true)){
                    socketTask.getClientSocket().reqOpenOrders();
                }
            }
        });
    }*/

    public Result reqOpenOrders() {
        Result result = new Result();
        boolean flag = false;
        try {
            if (DataCache.lockMap.get(DataCache.ORDER_KEY).tryLock()) {
                flag = true;
                DataCache.orderCache.put(DataCache.ORDER_KEY, new ArrayList<>());
                CountDownLatch ct = new CountDownLatch(1);
                DataCache.latchMap.put(DataCache.ORDER_KEY, ct);
                socketTask.getClientSocket().reqOpenOrders();
                ct.await(CommonConstants.ORDER_SEARCH_TIMEOUT, TimeUnit.MILLISECONDS);
                result.put("orders", DataCache.orderCache.get(DataCache.ORDER_KEY));
            } else {
                return new Result(StatusCode.IN_PROGRESS,"Request in progress, please wait");
            }
        } catch (Exception e) {
            log.error("reqOpenOrders",e);
        } finally {
            if (flag) {
                DataCache.orderCache.remove(DataCache.ORDER_KEY);
                DataCache.lockMap.get(DataCache.ORDER_KEY).unlock();
            }
        }
        return result;
    }

    public Result reqAllOpenOrders() {
        Result result = new Result();
        boolean flag = false;
        try {
            if (DataCache.lockMap.get(DataCache.ORDER_KEY).tryLock()) {
                flag = true;
                DataCache.orderCache.put(DataCache.ORDER_KEY, new ArrayList<>());
                CountDownLatch ct = new CountDownLatch(1);
                DataCache.latchMap.put(DataCache.ORDER_KEY, ct);
                socketTask.getClientSocket().reqAllOpenOrders();
                ct.await(CommonConstants.ORDER_SEARCH_TIMEOUT, TimeUnit.MILLISECONDS);
                result.put("orders", DataCache.orderCache.get(DataCache.ORDER_KEY));
            } else {
                return new Result(StatusCode.IN_PROGRESS,"Request in progress, please wait");
            }
        } catch (Exception e) {
            log.error("reqAllOpenOrders",e);
        } finally {
            if (flag) {
                DataCache.orderCache.remove(DataCache.ORDER_KEY);
                DataCache.lockMap.get(DataCache.ORDER_KEY).unlock();
            }
        }
        return result;
    }

    public Result reqCompletedOrders() {
        Result result = new Result();
        boolean flag = false;
        try {
            if (DataCache.lockMap.get(DataCache.ORDER_KEY).tryLock()) {
                flag = true;
                DataCache.orderCache.put(DataCache.ORDER_KEY, new ArrayList<>());
                CountDownLatch ct = new CountDownLatch(1);
                DataCache.latchMap.put(DataCache.ORDER_KEY, ct);
                socketTask.getClientSocket().reqCompletedOrders(false);
                ct.await(CommonConstants.ORDER_SEARCH_TIMEOUT, TimeUnit.MILLISECONDS);
                result.put("orders", DataCache.orderCache.get(DataCache.ORDER_KEY));
            } else {
                return new Result(StatusCode.IN_PROGRESS,"Request in progress, please wait");
            }
        } catch (Exception e) {
            log.error("reqCompletedOrders",e);
        } finally {
            if (flag) {
                DataCache.orderCache.remove(DataCache.ORDER_KEY);
                DataCache.lockMap.get(DataCache.ORDER_KEY).unlock();
            }
        }
        return result;
    }

    public Result cancelOrder(PlaceOrderReq req) {
        AssertUtil.validateEmpty(req.getUniqueId(), "uniqueId");

        int tid = req.getUniqueId();
        TickerOrderVO tickerVO = new TickerOrderVO(tid, null);
        tickerVO.setCountDown(new CountDownLatch(1));
        DataCache.tickerOrderCache.put(tid, tickerVO);
        socketTask.getClientSocket().cancelOrder(tid);
        try {
            tickerVO.getCountDown().await(CommonConstants.SEARCH_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("placeOrder timeout");
        }
        DataCache.tickerOrderCache.remove(tid);
        if (tickerVO.getErrorCode() == 0) {
            Result result = new Result();
            return result;
        } else if (tickerVO.getErrorCode() != 0) {
            return Result.fail(tickerVO.getErrorCode(), tickerVO.getErrorMsg());
        }
        return Result.ok();
    }

    public Result orderStatus(PlaceOrderReq req) {
        AssertUtil.validateEmpty(req.getUniqueId(), "uniqueId");
        OrderDetail orderDetail = DataCache.orderMap.get(req.getUniqueId());
        Result result = new Result();
        if (orderDetail != null) {
            result.put("order", orderDetail);
        }
        return result;
    }

    public Result reqOrderId() {
        Result result = new Result();
        result.put("nextOrderId", DataCache.nextOrderId);
        return result;
    }

    public Result reqContractDetails(ContractVO req) {
        AssertUtil.validateAllEmpty("symbol and conid", req.getSymbol(), req.getConid());

        int tid = SocketTask.tickerId.incrementAndGet();
        TickerVO tickerVO = new TickerVO(tid, req.getSymbol());
        tickerVO.setCountDown(new CountDownLatch(1));
        List<ContractDetailsVO> details = new ArrayList<>();
        tickerVO.setContractDetails(details);
        DataCache.tickerCache.put(tid, tickerVO);

        socketTask.getClientSocket().reqContractDetails(tid, req.toContract());
        try {
            tickerVO.getCountDown().await(CommonConstants.SEARCH_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("reqContractDetails timeout");
        }
        DataCache.tickerCache.remove(tid);
        if (tickerVO.getErrorCode() == 0 && details.size() > 0) {
            Result result = new Result();
            result.put("contractDetails", tickerVO.getContractDetails());
            return result;
        } else if (tickerVO.getErrorCode() != 0) {
            return Result.fail(tickerVO.getErrorCode(), tickerVO.getErrorMsg());
        }
        return Result.fail(StatusCode.TIME_OUT, "timeout");
    }
}

