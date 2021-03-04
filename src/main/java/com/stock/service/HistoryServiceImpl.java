package com.stock.service;

import com.ib.client.Contract;
import com.stock.SocketTask;
import com.stock.cache.DataMap;
import com.stock.core.common.Result;
import com.stock.core.util.AssertUtil;
import com.stock.vo.ContractVO;
import com.stock.vo.HistoryVO;
import com.stock.vo.TickerVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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

    public Result reqHistoricalData(ContractVO vo) {
        AssertUtil.validateEmpty(vo.getSymbol(), "symbol");
        AssertUtil.validateEmpty(vo.getSecType(), "secType");
        AssertUtil.validateEmpty(vo.getCurrency(), "currency");
        AssertUtil.validateEmpty(vo.getExchange(), "exchange");

        Contract contract = new Contract();
        contract.symbol(vo.getSymbol());
        contract.secType(vo.getSecType());
        contract.currency(vo.getCurrency());
        contract.exchange(vo.getExchange());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -6);
        SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        String formatted = form.format(cal.getTime());
        int tid = ++SocketTask.tickerId;
        HistoryVO his = new HistoryVO();
        his.setBars(new ArrayList<>());
        TickerVO tickerVO = new TickerVO(tid,vo.getSymbol(),his);
        DataMap.tickerCache.put(tid,tickerVO);
        SocketTask.clientSocket.reqHistoricalData(tid, contract, formatted, "1 M", "1 day", "MIDPOINT", 1, 1, false, null);
        int count=0;
        long t1 = System.currentTimeMillis();
        while(count<10 && !tickerVO.isResult()){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
            count++;
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t2-t1);
        if(tickerVO.isResult()){
            Result result = new Result();
            result.put("history",tickerVO.getHistory());
            return result;
        }
        return Result.fail(405,"timeout");
    }
}

