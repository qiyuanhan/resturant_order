package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()) + UUID.randomUUID());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);

        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();

        for (ShoppingCart shoppingCart1 : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart1, orderDetail);
            orderDetail.setOrderId(orders.getId());

            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        shoppingCartMapper.clean(userId);
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();


        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO,Integer job) {
        if(job==1){
            ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        }
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize(),true);
        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQueryDTO);
//        if(page.getTotal()==0){
//            throw new OrderBusinessException("查询对象不存在");
//        }
        for(OrderVO orderVO:page){
            List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(orderVO.getId());
            orderVO.setOrderDetailList(orderDetailList);
            String detail=new String();
            for(OrderDetail orderDetail:orderDetailList){
                detail=detail+orderDetail.getName()+" "+orderDetail.getDishFlavor()+" "+orderDetail.getNumber()+",";
            }
            detail=detail.substring(0,detail.length()-1);
            orderVO.setOrderDishes(detail);
        }

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public OrderVO OrderDetailQuery(Long id) {
        OrderVO orderVO = orderMapper.getByOrderId(id);
        AddressBook addressBook = addressBookMapper.getById(orderVO.getAddressBookId());
        orderVO.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
        return orderVO;
    }

    @Override
    public void setPayment(String number,Integer i) {
        Orders byNumber = orderMapper.getByNumber(number);
        byNumber.setPayStatus(i);
        if(i==1){
            byNumber.setStatus(2);
        }
        orderMapper.setPaymenStatus(byNumber);
    }

    @Override
    public void cancelOrder(Long id) {
        Orders order = orderMapper.getById(id);
        order.setStatus(6);
        order.setPayStatus(2);
        orderMapper.setPaymenStatus(order);
    }

    @Override
    public void orderAgain(Long id) {
        Long userId =BaseContext.getCurrentId();
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> shoppingCartList=new ArrayList<>();
        orderDetailList.forEach(orderDetail -> {
            ShoppingCart shoppingCart =new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        });
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public OrderStatisticsVO orderStatistics() {
        OrderStatisticsVO orderStatisticsVO =new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(orderMapper.getBystatis(2));
        orderStatisticsVO.setConfirmed(orderMapper.getBystatis(3));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.getBystatis(4));
        return orderStatisticsVO;
    }

    @Override
    public OrderVO orderDetailQuery(Long id) {
        OrderVO orderVO=orderMapper.getByOrderId(id);
        List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(orderVO.getId());
        orderVO.setOrderDetailList(orderDetailList);
        String detail=new String();
        for(OrderDetail orderDetail:orderDetailList){
            detail=detail+orderDetail.getName()+" "+orderDetail.getDishFlavor()+" "+orderDetail.getNumber()+",";
        }
        detail=detail.substring(0,detail.length()-1);
        orderVO.setOrderDishes(detail);
        return orderVO;
    }

    @Override
    public void setStatus(OrdersConfirmDTO ordersConfirmDTO) {
        ordersConfirmDTO.setStatus(3);
        orderMapper.setStatusConfirm(ordersConfirmDTO);
    }

    @Override
    public void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO) {
        orderMapper.rejectionOrder(ordersRejectionDTO);
    }

    @Override
    public void cancel(OrdersCancelDTO cancelDTO) {
        orderMapper.cancelOrder(cancelDTO);
    }

    @Override
    public void delivery(Long id) {
        orderMapper.delivery(id);
    }

    @Override
    public void complete(Long id) {
        orderMapper.complete(id);
    }
}
