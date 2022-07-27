package com.linlin.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linlin.seckill.pojo.User;
import com.linlin.seckill.vo.LoginVo;
import com.linlin.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * 啊哈哈哈哈
 * @author zhoubin

 */
public interface IUserService extends IService<User> {



    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    //根据cookie获取用户
    User getUserByCookie(String userTicket,HttpServletRequest request, HttpServletResponse response);

    //更新密码
    RespBean updatePassword(String userTicket,String passwor,HttpServletRequest request, HttpServletResponse response);
}
