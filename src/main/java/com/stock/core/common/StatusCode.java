package com.stock.core.common;

/**
 * @description:
 * @author: YJH
 * @time: 2020/4/5 11:52
 */
public class StatusCode {
    /**
     * 请求成功
     */
    public static final int SUCCESS = 0;
    /**
     * 服务器错误
     */
    public static final int ERROR = 10004;
    /**
     * 参数错误
     */
    public static final int PARAM_ERROR = 10001;
    /**
     * 超时
     */
    public static final int TIME_OUT = 10002;
    /**
     * 请求过快，请稍等
     */
    public static final int IN_PROGRESS = 10003;
}
