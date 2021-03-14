package com.stock.web;


import com.alibaba.fastjson.JSON;
import com.ib.client.ContractDetails;
import com.stock.core.annotation.LogPoint;
import com.stock.core.common.Result;
import com.stock.core.common.StatusCode;
import com.stock.service.AccountServiceImpl;
import com.stock.service.HistoryServiceImpl;
import com.stock.service.OrderServiceImpl;
import com.stock.vo.ContractVO;
import com.stock.vo.req.AccountSummaryReq;
import com.stock.vo.req.HistoricalDataReq;
import com.stock.vo.req.PlaceOrderReq;
import com.stock.vo.req.PnlReq;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author xjp
 * @since 2020-07-12
 */
@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    private HistoryServiceImpl historyServiceImpl;

    @Autowired
    private OrderServiceImpl orderServiceImpl;

    @Autowired
    private AccountServiceImpl accountServiceImpl;

    @ApiOperation(value = "请求历史数据")
    @PostMapping("/reqHistoricalData")
    @LogPoint
    public Result reqHistoricalData(@RequestBody HistoricalDataReq req) {
        return historyServiceImpl.reqHistoricalData(req);
    }

    @ApiOperation(value = "下订单")
    @PostMapping("/placeOrder")
    @LogPoint
    public Result placeOrder(@RequestBody PlaceOrderReq req) {
        return orderServiceImpl.placeOrder(req);
    }

    @ApiOperation(value = "取消订单")
    @PostMapping("/cancelOrder")
    @LogPoint
    public Result cancelOrder(@RequestBody PlaceOrderReq req) {
        return orderServiceImpl.cancelOrder(req);
    }

    @ApiOperation(value = "查询下个可用订单id")
    @PostMapping("/reqOrderId")
    @LogPoint
    public Result reqOrderId() {
        return orderServiceImpl.reqOrderId();
    }

    @ApiOperation(value = "订单状态")
    @PostMapping("/orderStatus")
    @LogPoint
    public Result orderStatus(@RequestBody PlaceOrderReq req) {
        return orderServiceImpl.orderStatus(req);
    }

    @ApiOperation(value = "合约详情")
    @PostMapping("/reqContractDetails")
    @LogPoint
    public Result reqContractDetails(@RequestBody ContractVO req) {
        return orderServiceImpl.reqContractDetails(req);
    }

    @ApiOperation(value = "账号概况")
    @PostMapping("/reqAccountSummary")
    @LogPoint
    public Result reqAccountSummary(@RequestBody AccountSummaryReq req) {
        return accountServiceImpl.reqAccountSummary(req);
    }

    @ApiOperation(value = "reqPnl")
    @PostMapping("/reqPnl")
    @LogPoint
    public Result reqPnl(@RequestBody PnlReq req) {
        return accountServiceImpl.reqPnl(req);
    }

    @ApiOperation(value = "reqPositionsMulti")
    @PostMapping("/reqPositionsMulti")
    @LogPoint
    public Result reqPositionsMulti(@RequestBody PnlReq req) {
        return accountServiceImpl.reqPositionsMulti(req);
    }

    @ApiOperation(value = "reqOpenOrders")
    @PostMapping("/reqOpenOrders")
    @LogPoint
    public Result reqOpenOrders() {
        return orderServiceImpl.reqOpenOrders();
    }

    @ApiOperation(value = "reqAllOpenOrders")
    @PostMapping("/reqAllOpenOrders")
    @LogPoint
    public Result reqAllOpenOrders() {
        return orderServiceImpl.reqAllOpenOrders();
    }

    @ApiOperation(value = "reqCompletedOrders")
    @PostMapping("/reqCompletedOrders")
    @LogPoint
    public Result reqCompletedOrders() {
        return orderServiceImpl.reqCompletedOrders();
    }
}