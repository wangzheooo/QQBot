package com.example.wizardbot.controller;

import com.example.wizardbot.contants.Global;
import com.example.wizardbot.service.BotService;
import com.example.wizardbot.utils.BotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: auther
 * @Date: 2021/1/14 19:50
 * @Description:基本没用,要什么controller,这可是机器人,直接跟他聊天就行了
 */

@RestController
public class BotController {
    private static final Logger logger = LoggerFactory.getLogger(BotController.class);

    @Autowired
    private Global global;

    @Autowired
    private BotService botService;

    @GetMapping("date")
    public String getDate() {
        return BotUtils.getYesterdayDate();
    }
}
