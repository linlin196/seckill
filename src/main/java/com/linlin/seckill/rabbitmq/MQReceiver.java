package com.linlin.seckill.rabbitmq;

import com.linlin.seckill.pojo.SeckillMessage;
import com.linlin.seckill.pojo.SeckillOrder;
import com.linlin.seckill.pojo.User;
import com.linlin.seckill.service.IGoodsService;
import com.linlin.seckill.service.IOrderService;
import com.linlin.seckill.utils.JsonUtil;
import com.linlin.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQReceiver {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IOrderService orderService;

/*    @RabbitListener(queues = "queue")
    public void receive(Object msg){
        log.info("接收消息:"+msg);
    }

    @RabbitListener(queues = "queue_fanout01")
    public void receive01(Object msg){
        log.info("queue01接收消息:"+msg);
    }
    @RabbitListener(queues = "queue_fanout02")
    public void receive02(Object msg){
        log.info("queue02接收消息:"+msg);
    }*/

    @RabbitListener(queues = "seckillQueue")
    public void receive(String message) {
        log.info("接收消息：" + message);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        Long goodsId = seckillMessage.getGoodId();
        User user = seckillMessage.getUser();
        //判断库存
        GoodsVo goodsVo = goodsService.findGoodsVobyGoodsId(goodsId);
        if (goodsVo.getStockCount() < 1) {
            return;
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return;
        }
        //下单操作
        orderService.seckill(user, goodsVo);

    }
}
