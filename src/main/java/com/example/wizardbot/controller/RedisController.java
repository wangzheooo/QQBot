package com.example.wizardbot.controller;

import com.example.wizardbot.service.RedisService;
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
    private RedisService redisService;

    @RequestMapping("setAndGet")
    public String test(String k, String v) {
        redisService.set(k, v);
        return (String) redisService.get(k);
    }
}
