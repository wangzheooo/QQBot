package com.example.wizardbot.contants;

import lombok.Data;
import net.mamoe.mirai.Bot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    private String menu="1.发送,石原里美功能介绍\n2.发送,开启每日简讯推送\n3.发送,关闭每日简讯推送\n4.发送,石原里美你好\n5.发送,新闻\n6.发送,吃饭推荐\n7.发送,生存天数19951212\n8.发送,淄博天气\n";

    private String[] foods = {"馄饨",
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
            "生煎包" };
}
