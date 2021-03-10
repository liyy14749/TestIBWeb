package com.stock.service;

import com.stock.SocketTask;
import com.stock.cache.DataCache;
import com.stock.constants.CommonConstants;
import com.stock.core.common.Result;
import com.stock.core.common.StatusCode;
import com.stock.core.util.AssertUtil;
import com.stock.vo.TickerVO;
import com.stock.vo.req.AccountSummaryReq;
import com.stock.vo.req.PnlReq;
import com.stock.vo.rsp.AccountSummaryRsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
public class AccountServiceImpl {
    @Autowired
    SocketTask socketTask;

    private static Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    public Result reqAccountSummary(AccountSummaryReq req) {
        AssertUtil.validateEmpty(req.getGroup(), "group");
        AssertUtil.validateEmpty(req.getTags(), "tags");

        int tid = SocketTask.tickerId.incrementAndGet();
        TickerVO tickerVO = new TickerVO(tid);
        tickerVO.setAccountSummary(new AccountSummaryRsp());
        tickerVO.setCountDown(new CountDownLatch(1));
        DataCache.tickerCache.put(tid,tickerVO);
        socketTask.getClientSocket().reqAccountSummary(tid, req.getGroup(), req.getTags());
        try {
            tickerVO.getCountDown().await(CommonConstants.SEARCH_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("reqAccountSummary timeout");
        }
        DataCache.tickerCache.remove(tid);
        if(tickerVO.getErrorCode() == 0 && tickerVO.getAccountSummary().getSummarys().size()>0){
            Result result = new Result();
            result.put("accountSummary",tickerVO.getAccountSummary());
            return result;
        } else if(tickerVO.getErrorCode() != 0 ){
            return Result.fail(tickerVO.getErrorCode(),tickerVO.getErrorMsg());
        }
        return Result.fail(StatusCode.TIME_OUT,"timeout");
    }

    public Result reqPnl(PnlReq req) {
        AssertUtil.validateEmpty(req.getAccount(), "account");

        int tid = SocketTask.tickerId.incrementAndGet();
        TickerVO tickerVO = new TickerVO(tid);
        tickerVO.setCountDown(new CountDownLatch(1));
        DataCache.tickerCache.put(tid,tickerVO);
        socketTask.getClientSocket().reqPnL(tid, req.getAccount(), req.getModelCode());
        try {
            tickerVO.getCountDown().await(CommonConstants.SEARCH_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("reqAccountSummary timeout");
        }
        DataCache.tickerCache.remove(tid);
        if(tickerVO.getErrorCode() == 0 && tickerVO.getPnl()!=null){
            Result result = new Result();
            result.put("pnl",tickerVO.getPnl());
            return result;
        } else if(tickerVO.getErrorCode() != 0){
            return Result.fail(tickerVO.getErrorCode(),tickerVO.getErrorMsg());
        }
        return Result.fail(StatusCode.TIME_OUT,"timeout");
    }

    public Result reqPositionsMulti(PnlReq req) {
        AssertUtil.validateEmpty(req.getAccount(), "account");

        int tid = SocketTask.tickerId.incrementAndGet();
        TickerVO tickerVO = new TickerVO(tid);
        tickerVO.setPositions(new ArrayList<>());
        tickerVO.setCountDown(new CountDownLatch(1));
        DataCache.tickerCache.put(tid,tickerVO);
        socketTask.getClientSocket().reqPositionsMulti(tid, req.getAccount(), req.getModelCode());
        try {
            tickerVO.getCountDown().await(CommonConstants.SEARCH_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("reqPositionsMulti timeout");
        }
        DataCache.tickerCache.remove(tid);
        if(tickerVO.getErrorCode() == 0 && tickerVO.isFinish()){
            Result result = new Result();
            result.put("positions",tickerVO.getPositions());
            return result;
        } else if(tickerVO.getErrorCode() != 0){
            return Result.fail(tickerVO.getErrorCode(),tickerVO.getErrorMsg());
        }
        return Result.fail(StatusCode.TIME_OUT,"timeout");
    }
}

