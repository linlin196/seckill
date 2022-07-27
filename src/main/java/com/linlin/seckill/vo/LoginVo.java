package com.linlin.seckill.vo;


import com.linlin.seckill.validator.IsMobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class LoginVo {
    //注解表示参数不为null
    @NotNull
    @IsMobile(required = true)
    private String mobile;

    @NotNull
    @Length(min=32)
    private String password;
}
