package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
@Api(tags = "管理端订单相关接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation("订单查询")
    public Result<PageResult> ordersSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单查询:{}",ordersPageQueryDTO);
        PageResult pageResult=orderService.pageQuery(ordersPageQueryDTO,0);

        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    @ApiOperation("查询订单情况")
    public Result<OrderStatisticsVO> orderStatistics(){
        log.info("查询订单情况");
        OrderStatisticsVO orderStatisticsVO= orderService.orderStatistics();
        return Result.success(orderStatisticsVO);
    }

    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> orderDetailQuery(@PathVariable Long id){
        log.info("查询订单详情");
        OrderVO orderVO=orderService.orderDetailQuery(id);
        return Result.success(orderVO);
    }

    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result orderReceive(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("接单");
        orderService.setStatus(ordersConfirmDTO);
        return Result.success();
    }


    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejectionOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("拒单");
        orderService.rejectionOrder(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancelOrder(@RequestBody OrdersCancelDTO cancelDTO){
        log.info("取消订单");
        orderService.cancel(cancelDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id){
        log.info("派送订单");
        orderService.delivery(id);
        return Result.success();
    }
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id){
        log.info("完成订单");
        orderService.complete(id);
        return Result.success();
    }
}
