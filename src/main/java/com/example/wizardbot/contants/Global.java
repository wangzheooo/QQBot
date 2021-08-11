package com.example.wizardbot.contants;

import lombok.Data;
import net.mamoe.mirai.Bot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Auther: auther
 * @Date: 2021/1/17 11:26
 * @Description:
 */
@Data
@Component
public class Global {

    @Value("${wizardBot.startStatus}")
    private Integer startStatus;

    @Value("${qq.account}")
    private String account;

    @Value("${qq.password}")
    private String password;

    @Value("${sockPort}")
    private Integer sockPort;

    @Value("${baidu.ak}")
    private String ak;

    @Value("${device.directoryWindows}")
    private String directoryWindows;

    @Value("${device.directoryLinux}")
    private String directoryLinux;

    private String directory;

    //机器人对象
    private Bot wizardBot;

    //线程池
    ExecutorService executor = Executors.newCachedThreadPool();

    private String menu = "" +
            "1.发送(.菜单)\n" +
            "2.发送(.开启每日简讯)\n" +
            "3.发送(.关闭每日简讯)\n" +
            "4.发送(.你好)\n" +
            "5.发送(.新闻)\n" +
            "6.发送(.简餐)\n" +
            "7.发送(.大餐)\n" +
            "8.发送(.简餐桓台文体中心)\n" +
            "9.发送(.大餐桓台文体中心)\n" +
            "10.发送(.年龄19951212)\n" +
            "11.发送(淄博天气)\n" +
            "12.发送,(.二维码123)\n" +
            "13.发送,(.nba)\n" +
            "注:\n" +
            "1.餐饮地理信息用城市名+地名/标志物,避免搜到重名的地方,例:桓台文体中心,山东辛泉村,黄岛泊里镇政府\n";

    public String getDirectory() {
        String system = System.getProperty("os.name");
        if (system.toLowerCase().startsWith("win")) {
            return getDirectoryWindows();
        } else {
            return getDirectoryLinux();
        }
    }
}
