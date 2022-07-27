package com.linlin.seckill.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linlin.seckill.pojo.User;
import com.linlin.seckill.service.IUserService;
import com.linlin.seckill.utils.CookieUtil;
import com.linlin.seckill.vo.RespBean;
import com.linlin.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Component
public class AccessLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;
    @Autowired
    private RedisTemplate redisTemplate;


    //handler，就是注解加上去真正要处理的方法
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            User user = getUser(request, response);
            UserContext.setUser(user);//当前线程存一下user，相当于盒子
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);//拿到handler方法上的注解
            if (accessLimit == null) {
                return true;
            }
            int second = accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();


            //拿到注解的值后，计数器算法实现限流
            String key = request.getRequestURI();
            if (needLogin) {
                if (user == null) {
                    render(response, RespBeanEnum.SESSION_ERROR);
                    return false;
                }
                key += ":" + user.getId();
            }
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Integer count = (Integer) valueOperations.get(key);
            if (count == null) {
                valueOperations.set(key, 1, second, TimeUnit.SECONDS);
            } else if (count < maxCount) {
                valueOperations.increment(key);
            } else {
                render(response, RespBeanEnum.ACCESS_LIMIT_REAHCED);
                return false;
            }
        }
        return true;
    }


    //构建返回对象
    private void render(HttpServletResponse response, RespBeanEnum respBeanEnum) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter printWriter = response.getWriter();
        RespBean bean = RespBean.error(respBeanEnum);
        printWriter.write(new ObjectMapper().writeValueAsString(bean));
        printWriter.flush();
        printWriter.close();
    }

    //获取当前登录用户
    private User getUser(HttpServletRequest request, HttpServletResponse response) {
        String userTicket = CookieUtil.getCookieValue(request, "userTicket");
        if (StringUtils.isEmpty(userTicket)) {
            return null;
        }
        return userService.getUserByCookie(userTicket, request, response);
    }
}
