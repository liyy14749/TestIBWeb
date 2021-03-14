package com.stock.cache;

import com.stock.vo.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class DataCache {
    public static Map<Integer, MktData> cache = new ConcurrentHashMap<>();
    public static Map<Integer, TickerVO> tickerCache = new ConcurrentHashMap<>();
    public static Map<Integer, TickerOrderVO> tickerOrderCache = new ConcurrentHashMap<>();
    public static int nextOrderId = -1;
    public static Map<String,ReentrantLock> lockMap = new ConcurrentHashMap<>();
    public static Map<String,CountDownLatch> latchMap = new ConcurrentHashMap<>();
    public static String ORDER_KEY = "reqOrders";
    public static Map<String, List<OrderDetail>> orderCache = new ConcurrentHashMap<>();
    public static Map<Integer, OrderDetail> orderMap = new ConcurrentHashMap<>();

    public static boolean SERVER_OK = false;

    static {
        lockMap.put(ORDER_KEY,new ReentrantLock());
    }
}
