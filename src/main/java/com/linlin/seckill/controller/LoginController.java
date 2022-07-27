package com.linlin.seckill.controller;


import com.linlin.seckill.service.IUserService;
import com.linlin.seckill.vo.LoginVo;
import com.linlin.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {


    @Autowired
    private IUserService userService;

    @RequestMapping(value = "/toLogin")
    public String toLogin() {
        return "login";
    }



    @RequestMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request, HttpServletResponse response){
        return userService.doLogin(loginVo,request,response);
        //log.info("{}",loginVo);
        //return null;
    }
}
