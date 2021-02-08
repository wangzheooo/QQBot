package com.example.wizardbot.controller;

import com.example.wizardbot.contants.Global;
import com.example.wizardbot.service.BotService;
import com.example.wizardbot.utils.BotUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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

    /**
     * 微博爬虫以后再说,现在不想弄
     * 参考文章:https://zhuanlan.zhihu.com/p/111773590
     * 参考代码:https://github.com/dataabc/weibo-crawler/blob/master/weibo.py
     * 抓取地址:https://m.weibo.cn/u/2812256417
     * */
    @GetMapping("weibo")
    public String getWeiboInfo() {
        //2812256417,测试号
        //个人信息 https://m.weibo.cn/api/container/getIndex?containerid=100505 2812256417
        logger.info("weibo,获取");
        String url = "";
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = null;
        String r;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                r = response.body().string();
                System.out.println(r);
                return r;
            } else {
                return "网络异常";
            }
        } catch (IOException e) {
            return "IO异常";
        } finally {
            response.close();
        }
    }
}
