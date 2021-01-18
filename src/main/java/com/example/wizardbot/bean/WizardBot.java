package com.example.wizardbot.bean;

import com.example.wizardbot.contants.Global;
import com.example.wizardbot.service.BotService;
import com.example.wizardbot.utils.Tool;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.BotConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.text.ParseException;

/**
 * @Auther: auther
 * @Date: 2021/1/14 15:32
 * @Description:后期优化多线程
 */
@Component
public class WizardBot implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(WizardBot.class);

    //临时消息
    private String messageTemp;

    //临时群号
    private Long groupId;

    @Autowired
    private Global global;

    @Autowired
    private BotService botService;

    @Override
    public void run(ApplicationArguments args) {

        if (global.getStartStatus() == 1) {
            //初始化启动
            init();
            //启动监听
            startListen();
        }

    }

    public void init() {
        Long account = Long.parseLong(global.getAccount());
        String password = global.getPassword();
        // 使用自定义配置
        global.setWizardBot(BotFactory.INSTANCE.newBot(account, password, new BotConfiguration() {{
            fileBasedDeviceInfo(); // 使用 device.json 存储设备信息
            setProtocol(MiraiProtocol.ANDROID_WATCH); // 切换协议
        }}));

        global.getWizardBot().login();
    }

    public void startListen() {
        // 创建监听
        System.out.println("======监听开启======");
        global.getWizardBot().getEventChannel().subscribeAlways(GroupMessageEvent.class, event -> {

            MessageChain message = event.getMessage();
            messageTemp = "";
            for (int i = 0; i < message.size(); i++) {
                if (("" + message.get(i)).indexOf("[mirai:") == -1) {
                    messageTemp += ("" + message.get(i)).replace("\r", " ").trim();
                }
            }
//            System.out.println(messageTemp);
            if (messageTemp.equals("开启每日简讯推送")) {
                groupId = event.getSubject().getId();

                if (botService.addGroup(groupId)) {
                    event.getSubject().sendMessage("每日简讯推送开启成功");
                } else {
                    event.getSubject().sendMessage("已经开启了,不要重复开启!");
                }
            } else if (messageTemp.equals("石原里美你好")) {
                event.getSubject().sendMessage(event.getSenderName() + "你好呀");
            } else if (messageTemp.equals("新闻")) {
                event.getSubject().sendMessage(botService.news());
            } else if (messageTemp.equals("吃饭推荐")) {
                event.getSubject().sendMessage(global.getFoods()[Tool.get_random(0, global.getFoods().length)]);
            } else if (messageTemp.indexOf("生存天数") != -1) {
                //例子:生存天数19951111
                if (messageTemp.length() == 12) {
                    try {
                        event.getSubject().sendMessage(botService.getSurvivalDays(messageTemp));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } else if (messageTemp.indexOf("天气") != -1) {
                if (messageTemp.length() >= 4 && messageTemp.length() < 10) {
                    String[] weatherContent = messageTemp.split("天气");
                    try {
                        event.getSubject().sendMessage(botService.weather("" + weatherContent[0]));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}