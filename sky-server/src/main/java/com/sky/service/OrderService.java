package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO,Integer job);

    OrderVO OrderDetailQuery(Long id);

    void setPayment(String number , Integer i);

    void cancelOrder(Long id);

    void orderAgain(Long id);

    OrderStatisticsVO orderStatistics();

    OrderVO orderDetailQuery(Long id);

    void setStatus(OrdersConfirmDTO id);

    void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO);

    void cancel(OrdersCancelDTO cancelDTO);

    void delivery(Long id);

    void complete(Long id);

    Object getIdBynumber(String orderNumber);

    void reminder(Long id);
}
