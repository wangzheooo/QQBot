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

    //机器人对象
    private Bot wizardBot;

    //线程池
    ExecutorService executor = Executors.newCachedThreadPool();

    private String menu = "" +
            "注:命令前带有小数点\n" +
            "1.发送(.功能介绍)\n" +
            "2.发送(.开启每日简讯)\n" +
            "3.发送(.关闭每日简讯)\n" +
            "4.发送(.你好)\n" +
            "5.发送(.新闻)\n" +
            "6.发送(.吃饭推荐)\n" +
            "7.发送(.年龄19951212)\n" +
            "8.发送(淄博天气)\n" +
            "9.发送,(.二维码123)\n";

    private String[] foods = {
            "馄饨",
            "拉面",
            "热干面",
            "刀削面",
            "油泼面",
            "炸酱面",
            "炒面",
            "重庆小面",
            "米线",
            "酸辣粉",
            "土豆粉",
            "凉皮",
            "麻辣烫",
            "肉夹馍",
            "羊肉汤",
            "炒饭",
            "盖浇饭",
            "烤肉饭",
            "黄焖鸡米饭",
            "驴肉火烧",
            "麻辣香锅",
            "披萨",
            "烤鸭",
            "汉堡",
            "炸鸡",
            "寿司",
            "煎饼果子",
            "生煎包"
    };
}
