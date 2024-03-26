package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    void updateNumberById(ShoppingCart shoppingCart);

    void insert(ShoppingCart shoppingCart);


    @Delete("delete from shopping_cart where user_id=#{currentId}")
    void clean(Long currentId);

    @Delete("delete from shopping_cart where id=#{id}")
    void deleteById(Long id);

    void insertBatch(List<ShoppingCart> shoppingCartList);
}
