package com.example.wizardbot.task;

import com.example.wizardbot.contants.Global;
import com.example.wizardbot.service.BotService;
import com.sun.org.apache.xml.internal.security.Init;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Auther: auther
 * @Date: 2021/1/16 13:00
 * @Description:当第一次获取成功,其余不执行,增大自动发送新闻成功率
 */
@Component
public class BotSchedulerTask {
    private static final Logger logger = LoggerFactory.getLogger(BotSchedulerTask.class);

    @Autowired
    private Global global;

    @Autowired
    private BotService botService;

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//    @Scheduled(cron = "*/1 * * * * ?")
//    private void newsTaskTest1() {
////        logger.info(botService.sendGroupMessage("487317072", "123"));
////        logger.info(botService.autoSendNews());
//        System.out.println(Thread.currentThread().getName() + " >>> task one " + format.format(new Date()));
//    }
//
//    @Scheduled(cron = "*/1 * * * * ?")
//    private void newsTaskTest2() {
////        logger.info(botService.sendGroupMessage("487317072", "123"));
////        logger.info(botService.autoSendNews());
//        System.out.println(Thread.currentThread().getName() + " >>> task two " + format.format(new Date()));
//    }

    @Scheduled(cron = "0 0 8 * * ?")
    private void newsTask1() {
        botService.autoSendNews();
    }

    @Scheduled(cron = "0 1 8 * * ?")
    private void newsTask2() {
        botService.autoSendNews();
    }

    @Scheduled(cron = "0 15 8 * * ?")
    private void newsTask3() {
        botService.autoSendNews();
    }

    @Scheduled(cron = "0 16 8 * * ?")
    private void newsTask4() {
        botService.autoSendNews();
    }

}
