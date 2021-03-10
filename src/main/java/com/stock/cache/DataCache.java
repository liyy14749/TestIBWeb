package com.stock.cache;

import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import com.stock.vo.ContractVO;
import com.stock.vo.MktData;
import com.stock.vo.TickerOrderVO;
import com.stock.vo.TickerVO;
import io.swagger.models.auth.In;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DataCache {
    public static Map<Integer, MktData> cache = new ConcurrentHashMap<>();
    public static Map<Integer, TickerVO> tickerCache = new ConcurrentHashMap<>();
    public static Map<Integer, TickerOrderVO> tickerOrderCache = new ConcurrentHashMap<>();
    public static Map<Integer, OrderState> orderCache = new ConcurrentHashMap<>();
    public static int nextOrderId = -1;
    public static AtomicBoolean reqOpenOrders = new AtomicBoolean(false);
    public static boolean SERVER_OK = false;

    public static List<ContractVO> initContract = new ArrayList<>();
    static {
        initContract.add(new ContractVO("EUR","CASH","USD","IDEALPRO"));
        initContract.add(new ContractVO("GBP","CASH","USD","IDEALPRO"));
        initContract.add(new ContractVO("USD","CASH","JPY","IDEALPRO"));
    }
}
