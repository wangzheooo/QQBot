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
            "火锅",
            "酸菜鱼",
            "烤串",
            "披萨",
            "烤鸭",
            "汉堡",
            "炸鸡",
            "寿司",
            "煎饼果子",
            "生煎包" };
}
