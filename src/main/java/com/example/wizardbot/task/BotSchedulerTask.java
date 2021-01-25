package com.example.wizardbot.task;

import com.example.wizardbot.contants.Global;
import com.example.wizardbot.service.BotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Auther: auther
 * @Date: 2021/1/16 13:00
 * @Description:6个定时器的目的是,第一次爬取外网数据失败,另外三次再获取,,,当第一次获取成功,其余不执行,增大自动发送新闻成功率
 */
@Component
public class BotSchedulerTask {
    private static final Logger logger = LoggerFactory.getLogger(BotSchedulerTask.class);

    @Autowired
    private Global global;

    @Autowired
    private BotService botService;

//    @Scheduled(cron = "*/10 * * * * ?")
//    private void newsTaskTest() {
////        logger.info(botService.sendMessage("487317072", "123"));
//        logger.info(botService.autoSendNews());
//    }

    @Scheduled(cron = "0 0 8 * * ?")
    private void newsTask1() {
        global.getExecutor().execute(() -> logger.info(botService.autoSendNews()));
    }

    @Scheduled(cron = "0 1 8 * * ?")
    private void newsTask2() {
        global.getExecutor().execute(() -> logger.info(botService.autoSendNews()));
    }

    @Scheduled(cron = "0 15 8 * * ?")
    private void newsTask3() {
        global.getExecutor().execute(() -> logger.info(botService.autoSendNews()));
    }

    @Scheduled(cron = "0 16 8 * * ?")
    private void newsTask4() {
        global.getExecutor().execute(() -> logger.info(botService.autoSendNews()));
    }

    @Scheduled(cron = "0 30 8 * * ?")
    private void newsTask5() {
        global.getExecutor().execute(() -> logger.info(botService.autoSendNews()));
    }

    @Scheduled(cron = "0 31 8 * * ?")
    private void newsTask6() {
        global.getExecutor().execute(() -> logger.info(botService.autoSendNews()));
    }
}
