package com.stock.cache;

import com.stock.core.util.BeanUtil;
import com.stock.utils.KeyUtil;
import com.stock.vo.MktData;
import com.stock.vo.OrderDetail;
import com.stock.vo.TickerOrderVO;
import com.stock.vo.TickerVO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class DataCache {
    public static Map<Integer, TickerVO> tickerCache = new ConcurrentHashMap<>();
    public static Map<Integer, TickerOrderVO> tickerOrderCache = new ConcurrentHashMap<>();
    public static int nextOrderId = -1;
    public static Map<String,ReentrantLock> lockMap = new ConcurrentHashMap<>();
    public static Map<String,CountDownLatch> latchMap = new ConcurrentHashMap<>();
    public static String ORDER_KEY = "reqOrders";
    public static String ACCOUNT_KEY = "reqAccountSummary";
    public static String POSITION_KEY = "reqPositions";
    public static Map<String, Map<Integer,OrderDetail>> orderCache = new ConcurrentHashMap<>();
    public static String ORDER_MAP_KEY = "order_map";
    public static String PERM_ID_MAP_KEY = "perm_id_map";
//    public static Map<Integer, OrderInfo> orderMap = new ConcurrentHashMap<>();
//    public static Map<Integer, Integer> permIdMap = new ConcurrentHashMap<>();

    public static boolean SERVER_OK = false;

    static {
        lockMap.put(ORDER_KEY,new ReentrantLock());
        lockMap.put(ACCOUNT_KEY,new ReentrantLock());
        KeyUtil keyUtil = BeanUtil.getBean(KeyUtil.class);
        ORDER_MAP_KEY = keyUtil.getKeyWithPrefix(ORDER_MAP_KEY);
        PERM_ID_MAP_KEY = keyUtil.getKeyWithPrefix(PERM_ID_MAP_KEY);
    }
}
