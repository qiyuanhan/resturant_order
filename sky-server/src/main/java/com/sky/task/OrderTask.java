package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeoutOrder(){
        log.info("处理超时订单：{}",LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getStatusAndOrderTime(Orders.PENDING_PAYMENT,LocalDateTime.now().plusMinutes(-15));
        if(ordersList!=null && ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("订单提交超时，已取消");
                orders.setStatus(Orders.CANCELLED);
                orderMapper.update(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ? ")
    public void processDeliveryOrder(){
        log.info("处理还在派送中的订单:{}",LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS,LocalDateTime.now().plusHours(-1));
        if(ordersList!=null && ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
