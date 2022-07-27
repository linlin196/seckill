package com.linlin.seckill.controller;


import com.linlin.seckill.pojo.User;
import com.linlin.seckill.service.IGoodsService;
import com.linlin.seckill.service.IUserService;
import com.linlin.seckill.vo.DetailVo;
import com.linlin.seckill.vo.GoodsVo;
import com.linlin.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;


@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IUserService userService;


    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;



    //跳转到商品列表页
/*    @RequestMapping("/toList")
    public String toList(HttpServletRequest request, HttpServletResponse response, Model model, @CookieValue("userTicket") String ticket){
        *//*if (StringUtils.isEmpty(ticket)) {
            return "login";
        }

        //User user=(User)session.getAttribute(ticket);

        User user=userService.getUserByCookie(ticket,request,response);
        if(null==user){
            return "login";
        }*//*
        model.addAttribute("user",user);
        return "goodsList";
    }*/
    //windows优化前：QPS：699 （线程数1000，循环5 ）
    //       缓存QPS:1095



    //linux优化前：QPS:16.6（线程数1000，循环5 ）

    @RequestMapping(value="/toList",produces = "text/html;charset=utf-8",method = RequestMethod.GET)
    @ResponseBody
    public String toList( Model model, User user,HttpServletRequest request, HttpServletResponse response){
        //redis获取页面，如果不为空，直接返回页面
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String)valueOperations.get("goodsList");
        if (!StringUtils.isEmpty(html)){
            return html;
        }

        model.addAttribute("user",user);
        model.addAttribute("goodsList",goodsService.findGoodsVo());
        //return "goodsList";

        //如果为空，要手动渲染，存入redis并返回
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html=thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }
        return html;

    }


    //跳转商品详情页面
    @RequestMapping(value="/toDetail2/{goodsId}",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail2(Model model,User user,@PathVariable Long goodsId,HttpServletRequest request, HttpServletResponse response){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //Redis中获取页面，如果不为空，直接返回页面
        String html = (String) valueOperations.get("goodsDetail:" + goodsId);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        model.addAttribute("user",user);

        GoodsVo goodsVo =goodsService.findGoodsVobyGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();

        //秒杀状态
        int seckillStatus = 0;
        //秒杀倒计时
        int remainSeconds = 0;

        if (nowDate.before(startDate)) {
            //秒杀还未开始0
            remainSeconds = (int) ((startDate.getTime() - nowDate.getTime()) / 1000);
        } else if (nowDate.after(endDate)) {
            //秒杀已经结束
            seckillStatus = 2;
            remainSeconds = -1;

        } else {
            //秒杀进行中
            seckillStatus = 1;
            remainSeconds = 0;

        }
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("seckillStatus", seckillStatus);
        model.addAttribute("goods",goodsVo);
        //return "goodsDetail";
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(),
                model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsDetail:" + goodsId, html, 60, TimeUnit.SECONDS);
        }
        return html;
    }



    //跳转商品详情页面,页面静态化
    @RequestMapping(value = "/detail/{goodsId}", method = RequestMethod.GET)
    @ResponseBody
    public RespBean toDetail(Model model, User user, @PathVariable Long goodsId){


        GoodsVo goodsVo =goodsService.findGoodsVobyGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();

        //秒杀状态
        int seckillStatus = 0;
        //秒杀倒计时
        int remainSeconds = 0;

        if (nowDate.before(startDate)) {
            //秒杀还未开始0
            remainSeconds = (int) ((startDate.getTime() - nowDate.getTime()) / 1000);
        } else if (nowDate.after(endDate)) {
            //秒杀已经结束
            seckillStatus = 2;
            remainSeconds = -1;

        } else {
            //秒杀进行中
            seckillStatus = 1;
            remainSeconds = 0;

        }

        //return "goodsDetail";
        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVo);
        detailVo.setSecKillStatus(seckillStatus);
        detailVo.setRemainSeconds(remainSeconds);
        return RespBean.success(detailVo);
    }
}
