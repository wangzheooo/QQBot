package com.example.wizardbot.controller;

import com.example.wizardbot.contants.Global;
import com.example.wizardbot.service.BotService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    //暂时搁置,大众点评反爬有点厉害
//    @GetMapping("/getKuaiCan")
    public void getKuaiCan(String city) {
        //小吃快餐
        //ch10餐饮,g112小吃快餐,x5y25价格筛选,p2第二页
        String url = "http://www.dianping.com/" + city + "/ch10/g112x5y20";
        System.out.println(url);
        System.out.println("=======================================");
        try {
            Document document = Jsoup.connect(url).get();
            System.out.println(document.body());
            System.out.println("=================================================");
            //先查看有几页
            Element element = document.body().getElementsByClass("page").get(0);
            System.out.println(element);
            /*for (int i = 0; i < elementList.size(); i++) {
                System.out.println(elementList.get(i));
                System.out.println("=================================================");
            }*/

            /*List<Element> elementList = document.body().getElementsByClass("txt");
            Element elementTemp = elementList.get(0);
            Element tit = elementTemp.getElementsByClass("txt").get(0).child(0);
            System.out.println(Jsoup.parse("" + tit).wholeText().trim());*/
            /*for (int i = 0; i < elementList.size(); i++) {
                elementTemp = elementList.get(i);
                System.out.println(elementTemp);
                System.out.println("=================================================");
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
