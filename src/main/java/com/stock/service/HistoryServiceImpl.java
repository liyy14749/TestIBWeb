package com.stock.service;

import com.ib.client.Contract;
import com.stock.SocketTask;
import com.stock.cache.DataMap;
import com.stock.core.common.Result;
import com.stock.core.common.StatusCode;
import com.stock.core.util.AssertUtil;
import com.stock.vo.ContractVO;
import com.stock.vo.HistoryVO;
import com.stock.vo.TickerVO;
import com.stock.vo.req.HistoricalDataReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
public class HistoryServiceImpl {

    private static Logger log = LoggerFactory.getLogger(HistoryServiceImpl.class);

    public Result reqHistoricalData(HistoricalDataReq req) {
        ContractVO vo = req.getContract();
        AssertUtil.validateEmpty(vo, "contract");
        AssertUtil.validateEmpty(vo.getSymbol(), "symbol");
        AssertUtil.validateEmpty(vo.getSecType(), "secType");
        AssertUtil.validateEmpty(vo.getCurrency(), "currency");
        AssertUtil.validateEmpty(vo.getExchange(), "exchange");

        AssertUtil.validateEmpty(req.getEndDateTime(), "endDateTime");
        AssertUtil.validateEmpty(req.getDurationStr(), "durationStr");
        AssertUtil.validateEmpty(req.getBarSizeSetting(), "barSizeSetting");
        AssertUtil.validateEmpty(req.getWhatToShow(), "whatToShow");

        Contract contract = new Contract();
        contract.symbol(vo.getSymbol());
        contract.secType(vo.getSecType());
        contract.currency(vo.getCurrency());
        contract.exchange(vo.getExchange());
        contract.primaryExch(vo.getPrimaryExch());

        int tid = SocketTask.tickerId.incrementAndGet();
        HistoryVO his = new HistoryVO();
        his.setBars(new ArrayList<>());
        TickerVO tickerVO = new TickerVO(tid,vo.getSymbol());
        tickerVO.setHistory(his);
        tickerVO.setCountDown(new CountDownLatch(1));
        DataMap.tickerCache.put(tid,tickerVO);
        SocketTask.clientSocket.reqHistoricalData(tid, contract,
                req.getEndDateTime(), req.getDurationStr(), req.getBarSizeSetting(), req.getWhatToShow(), 1, 1, false, null);
        try {
            tickerVO.getCountDown().await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("reqHistoricalData timeout");
            return Result.fail(StatusCode.TIME_OUT,"timeout");
        }
        DataMap.tickerCache.remove(tid);
        if(tickerVO.getErrorCode() == 0){
            Result result = new Result();
            result.put("history",tickerVO.getHistory());
            return result;
        }
        return Result.fail(tickerVO.getErrorCode(),tickerVO.getErrorMsg());
    }
}

