package com.linlin.seckill.controller;


import com.linlin.seckill.pojo.User;
import com.linlin.seckill.rabbitmq.MQSender;
import com.linlin.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * 啊哈哈哈哈
 * @author zhoubin

 */
@Controller
@RequestMapping("/user")
public class UserController {

   /* @Autowired
    private MQSender mqSender;



    //用户信息，测试
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user){
        return RespBean.success();
    }

    //RabbitMQ测试
    @RequestMapping("/mq")
    @ResponseBody
    public void mq(){
        mqSender.send("hello");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public void mq01(){
        mqSender.send("hello");
    }*/
}
