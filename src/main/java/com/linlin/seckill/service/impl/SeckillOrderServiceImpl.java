package com.linlin.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linlin.seckill.mapper.SeckillOrderMapper;
import com.linlin.seckill.pojo.SeckillOrder;
import com.linlin.seckill.pojo.User;
import com.linlin.seckill.service.ISeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * 啊哈哈哈哈
 * @author zhoubin

 */
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements ISeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Resource
    private RedisTemplate redisTemplate;




    //获取秒杀结果，orderId 成功 ；-1 秒杀失败 ；0 排队中
    @Override
    public Long getResult(User user, Long goodsId) {

        SeckillOrder seckillOrder= seckillOrderMapper.selectOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        if (null != seckillOrder) {
            return seckillOrder.getOrderId();
        } else if (redisTemplate.hasKey("isStockEmpty:" + goodsId)) {
            return -1L;
        } else {
            return 0L;
        }
    }
}
