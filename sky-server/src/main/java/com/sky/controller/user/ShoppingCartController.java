package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "c端购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车，商品信息：{}", shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();

    }

    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public  Result<List<ShoppingCart>> list(){
        log.info("查询用户购物车id:", BaseContext.getCurrentId());
        List<ShoppingCart> list= shoppingCartService.getByUserId(BaseContext.getCurrentId());
        return Result.success(list);
    }

    @PostMapping("/sub")
    @ApiOperation("购物车数量减一")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("减少购物车，商品信息：{}", shoppingCartDTO);
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();

    }

    @DeleteMapping("/clean")
    @ApiOperation("购物车数量减一")
    public Result clean() {
        log.info("清空购物车");
        shoppingCartService.clean();
        return Result.success();

    }
}
