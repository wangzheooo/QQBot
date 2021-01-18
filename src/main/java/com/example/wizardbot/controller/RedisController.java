package com.example.wizardbot.controller;

import com.example.wizardbot.service.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: auther
 * @Date: 2021/1/16 22:33
 * @Description:
 */
@RestController
public class RedisController {

    @Autowired
    private RedisUtils redisUtils;

    @RequestMapping("setAndGet")
    public String test(String k, String v) {
        redisUtils.set(k, v);
        return (String) redisUtils.get(k);
    }
}
