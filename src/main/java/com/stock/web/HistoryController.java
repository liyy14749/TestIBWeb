package com.stock.web;


import com.stock.core.annotation.LogPoint;
import com.stock.core.common.Result;
import com.stock.core.common.StatusCode;
import com.stock.service.HistoryServiceImpl;
import com.stock.vo.ContractVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xjp
 * @since 2020-07-12
 */
@RestController
@RequestMapping("/data")
public class HistoryController {

    @Autowired
    public HistoryServiceImpl historyServiceImpl;

    @ApiOperation(value = "绑定邮箱")
    @PostMapping("/reqHistoricalData")
    @LogPoint
    public Result reqHistoricalData(@RequestBody ContractVO vo){
        return historyServiceImpl.reqHistoricalData(vo);
    }
}