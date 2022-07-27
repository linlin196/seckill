package com.linlin.seckill.config;

import com.linlin.seckill.pojo.User;

public class UserContext {
    private static ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    public static void setUser(User user) {
        userThreadLocal.set(user);
    }

    public static User getUser() {
        return userThreadLocal.get();
    }
}
