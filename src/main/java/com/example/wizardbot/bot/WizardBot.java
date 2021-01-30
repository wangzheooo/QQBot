package com.example.wizardbot.bot;

import com.example.wizardbot.contants.Global;
import com.example.wizardbot.handle.EventHandle;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.utils.BotConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @Auther: auther
 * @Date: 2021/1/14 15:32
 * @Description:
 */
@Component
public class WizardBot implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(WizardBot.class);

    @Autowired
    private Global global;

    @Autowired
    private EventHandle eventHandle;

    @Override
    public void run(ApplicationArguments args) {
        if (global.getStartStatus() == 1) {
            //初始化启动
            startupBot();
            //注册监听
            startupListen();
        }
    }

    public void startupBot() {
        // 使用自定义配置
        global.setWizardBot(BotFactory.INSTANCE.newBot(Long.parseLong(global.getAccount()), global.getPassword(), new BotConfiguration() {{
            setWorkingDir(new File("C:/wizardbot"));
            fileBasedDeviceInfo(); // 使用 device.json 存储设备信息
            setProtocol(MiraiProtocol.ANDROID_WATCH); // 切换协议
            redirectNetworkLogToFile();
            redirectNetworkLogToDirectory();
        }}));
        global.getWizardBot().login();//登录
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");//设置https协议，解决SSL peer shut down incorrectly的异常
    }

    // 注册监听
    public void startupListen() {
        logger.info("======注册监听======");
        GlobalEventChannel.INSTANCE.registerListenerHost(eventHandle);
//        GlobalEventChannel.INSTANCE.parentJob(SupervisorJob)
    }

}