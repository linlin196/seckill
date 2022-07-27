package com.linlin.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linlin.seckill.pojo.Order;
import com.linlin.seckill.pojo.User;
import com.linlin.seckill.vo.GoodsVo;
import com.linlin.seckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * 啊哈哈哈哈
 * @author zhoubin

 */
public interface IOrderService extends IService<Order> {

    //秒杀
    Order seckill(User user, GoodsVo goods);


    //订单详情
    OrderDetailVo detail(Long orderId);

    //获取秒杀地址
    String createPath(User user, Long goodsId);

    //校验秒杀地址
    boolean checkPath(User user, Long goodsId, String path);


    //校验验证码
    boolean checkCaptcha(User user, Long goodsId, String captcha);
}
