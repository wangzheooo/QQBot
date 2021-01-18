package com.example.wizardbot.controller;

import com.example.wizardbot.contants.Global;
import com.example.wizardbot.service.BotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: auther
 * @Date: 2021/1/14 19:50
 * @Description:
 */

@RestController
public class BotController {
    private static final Logger logger = LoggerFactory.getLogger(BotController.class);

    @Autowired
    private Global global;

    @Autowired
    private BotService botService;

    @GetMapping("/sendMessage")
    public String sendMessage(String groupId, String content) {
        return botService.sendMessage(groupId, content);
    }

}
