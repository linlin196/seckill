package com.linlin.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.linlin.seckill.config.AccessLimit;
import com.linlin.seckill.exception.GlobalException;
import com.linlin.seckill.pojo.Order;
import com.linlin.seckill.pojo.SeckillMessage;
import com.linlin.seckill.pojo.SeckillOrder;
import com.linlin.seckill.pojo.User;
import com.linlin.seckill.rabbitmq.MQSender;
import com.linlin.seckill.service.IGoodsService;
import com.linlin.seckill.service.IOrderService;
import com.linlin.seckill.service.ISeckillOrderService;
import com.linlin.seckill.utils.JsonUtil;
import com.linlin.seckill.vo.GoodsVo;
import com.linlin.seckill.vo.RespBean;
import com.linlin.seckill.vo.RespBeanEnum;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping("/seckill")
public class SecKillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender mqSender;
    @Autowired
    private RedisScript<Long> redisScript;


    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

    //windows优化前：QPS：425（线程数1000，循环5 ）
    //linux优化前：QPS:（线程数1000，循环5 ）

    //秒杀
    @RequestMapping("/doSeckill2")
    public String doSecKill2(Model model, User user, Long goodsId){
        if(user==null){
            return "login";
        }
        model.addAttribute("user", user);
        GoodsVo goods = goodsService.findGoodsVobyGoodsId(goodsId);
        //判断库存
        if(goods.getStockCount()<1){
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        //判断是否重复抢购
        //SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        SeckillOrder seckillOrder  = (SeckillOrder)redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }

        //可以抢购了
        Order order =orderService.seckill(user,goods);
        model.addAttribute("order", order);
        model.addAttribute("goods", goods);
        return "orderDetail";
    }


    //秒杀
    @RequestMapping(value = "/{path}/doSeckill",method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(@PathVariable String path, User user, Long goodsId){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        boolean check = orderService.checkPath(user, goodsId, path);
        if (!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder  = (SeckillOrder)redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //内存标记，减少Redis的访问，true就是这个商品id空了，就报错
        if (EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减库存
        //Long stock=valueOperations.decrement("seckillGoods:"+goodsId);\
        Long stock = (Long) redisTemplate.execute(redisScript, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
        if(stock<0){
            EmptyStockMap.put(goodsId, true);
            valueOperations.increment("seckillGoods:"+goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //下单
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0);



        /*GoodsVo goods = goodsService.findGoodsVobyGoodsId(goodsId);
        //判断库存
        if(goods.getStockCount()<1){
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        if (seckillOrder != null) {
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        //可以抢购了
        Order order =orderService.seckill(user,goods);

        return RespBean.success(order);*/
    }


    //获取秒杀结果， orderId 成功 ；-1 秒杀失败 ；0 排队中
    @GetMapping(value = "/getResult")
    @ResponseBody
    public RespBean getResult(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }


    @AccessLimit(second = 5, maxCount = 5, needLogin = true)
    @GetMapping("/path")
    @ResponseBody
    public RespBean getPath(User user, Long goodsId,String captcha, HttpServletRequest request) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        /*ValueOperations valueOperations = redisTemplate.opsForValue();
        // 限制访问次数，5秒内访问5次
        String uri = request.getRequestURI();
        captcha = "0";
        Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());
        if (count == null) {
            valueOperations.set(uri + ":" +user.getId(), 1, 5, TimeUnit.SECONDS);
        } else if (count < 5) {
            valueOperations.increment(uri + ":" + user.getId());
        } else {
            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REAHCED);
        }*/

        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check) {
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);

    }


    @GetMapping(value = "/captcha")
    public void verifyCode(User user, Long goodsId, HttpServletResponse response) {
        if (user == null || goodsId < 0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //设置请求头为输出图片的类型
        response.setContentType("image/jpg");
        response.setHeader("Pargam", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        //生成验证码,将结果放在redis里
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
        }
    }



    //初始化，把商品库存数量加载到Redis
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(), false);
        });

    }
}
