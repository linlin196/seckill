package com.linlin.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linlin.seckill.pojo.SeckillOrder;
import com.linlin.seckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * 啊哈哈哈哈
 * @author zhoubin

 */
public interface ISeckillOrderService extends IService<SeckillOrder> {


    //获取秒杀结果，orderId 成功 ；-1 秒杀失败 ；0 排队中
    Long getResult(User user, Long goodsId);
}
