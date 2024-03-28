package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;

@Mapper
public interface OrderMapper {
    public void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO getByOrderId(Long id);

    @Update("update orders set pay_status=#{payStatus},status=#{status} where number=#{number}")
    void setPaymenStatus(Orders byNumber);

    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    @Select("select count(0) from orders where status=#{status}")
    Integer getBystatis(Integer status);

    @Update("update orders set status=#{status} where id=#{id}")
    void setStatusConfirm(OrdersConfirmDTO ordersConfirmDTO);

    @Update("update orders set status=6 ,pay_status=2,rejection_reason=#{rejectionReason} where id=#{id}")
    void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO);

    @Update("update orders set status=6 ,pay_status=2,cancel_reason=#{cancelReason} where id=#{id}")
    void cancelOrder(OrdersCancelDTO cancelDTO);

    @Update("update orders set status=4 where id=#{id}")
    void delivery(Long id);

    @Update("update orders set status=5 where id=#{id}")
    void complete(Long id);

    @Select("select * from orders where status=#{status} and order_time <#{now}")
    List<Orders> getStatusAndOrderTime(Integer status, LocalDateTime now);

    Double sumByMap(Map map);
}
