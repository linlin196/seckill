package com.linlin.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linlin.seckill.exception.GlobalException;
import com.linlin.seckill.mapper.UserMapper;
import com.linlin.seckill.pojo.User;
import com.linlin.seckill.service.IUserService;
import com.linlin.seckill.utils.CookieUtil;
import com.linlin.seckill.utils.MD5Util;
import com.linlin.seckill.utils.UUIDUtil;
import com.linlin.seckill.utils.ValidatorUtil;
import com.linlin.seckill.vo.LoginVo;
import com.linlin.seckill.vo.RespBean;
import com.linlin.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * 啊哈哈哈哈
 * @author zhoubin

 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

/*        //参数校验
        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)) {
           return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }

        if (!ValidatorUtil.isMobile(mobile)) {
            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
        }*/
        //根据手机号获取用户
        User user = userMapper.selectById(mobile);
        if(null==user){
            //return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        if(!MD5Util.formPassToDBPass(password, user.getSlat()).equals(user.getPasword())){
            //return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        //生成cookie
        String ticket = UUIDUtil.uuid();
        //将用户信息存入redis中
        redisTemplate.opsForValue().set("user:"+ticket,user);
        //request.getSession().setAttribute(ticket,user);

        CookieUtil.setCookie(request,response,"userTicket",ticket);

        return RespBean.success(ticket);
    }

    @Override
    public User getUserByCookie(String userTicket,HttpServletRequest request, HttpServletResponse response) {
        if(StringUtils.isEmpty(userTicket)){
            return null;
        }

        User user=(User)redisTemplate.opsForValue().get("user:"+userTicket);
        if(user!=null){
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }
        return user;
    }

    //更新密码,保证数据库和redis 数据一样
    @Override
    public RespBean updatePassword(String userTicket, String password,HttpServletRequest request, HttpServletResponse response) {
        User user = getUserByCookie(userTicket, request, response);
        if (user == null) {
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPasword(MD5Util.inputPassToDbPass(password, user.getSlat()));
        int result = userMapper.updateById(user);
        if (1 == result) {
            //删除Redis
            redisTemplate.delete("user:" + userTicket);
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }
}
