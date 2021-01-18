package com.example.wizardbot.service;

import com.alibaba.fastjson.JSON;
import com.example.wizardbot.contants.Global;
import com.example.wizardbot.utils.Tool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Auther: auther
 * @Date: 2021/1/17 11:23
 * @Description:
 */
@Service
public class BotService {
    private static final Logger logger = LoggerFactory.getLogger(BotService.class);

    @Autowired
    Global global;

    @Autowired
    private RedisUtils redisUtils;

    //天气缓存,<城市,时间戳>,13位时间戳
    Map<String, Long> botWeatherDateMap = new HashMap<>();
    //天气缓存,<城市,内容>
    Map<String, String> botWeatherContentMap = new HashMap<>();
    //天气缓存时间,5分钟,1000*60*5
    int botWeatherCache = 1000 * 60 * 5;

    public String[] getGroupNewsList() {
        //群号缓存
        String groupStr = (String) redisUtils.get("groupNewsList");

        if (groupStr == null || groupStr == "" || groupStr.equals("")) {
            return null;
        }

        //群号数组
        String[] groupNewsList = groupStr.split(",");

        return groupNewsList;
    }

    public boolean addGroup(Long groupId) {
        String groupStr = (String) redisUtils.get("groupNewsList");
        if (groupStr == null || groupStr == "" || groupStr.equals("")) {
            redisUtils.set("groupNewsList", "" + groupId);
            logger.info("addGroup,添加成功");
        } else {
            String[] groupNewsList = groupStr.split(",");
            for (int i = 0; i < groupNewsList.length; i++) {
                //已经存在则直就退出
                if (groupId == (Long.parseLong(groupNewsList[i]))) {
                    logger.info("addGroup,已经存在的群号");
                    return false;
                }
                //不存在则加入缓存
                if (i == (groupNewsList.length - 1)) {
                    groupStr += "," + groupId;
                    redisUtils.set("groupNewsList", groupStr);
                    logger.info("addGroup,添加成功");
                }
            }
        }
        return true;
    }

    public boolean delGroup(Long groupId) {
        String groupStr = (String) redisUtils.get("groupNewsList");
        if (groupStr == null || groupStr == "" || groupStr.equals("")) {
            return true;
        } else {
            String[] groupNewsList = groupStr.split(",");
            String groupStrResult = "";
            for (int i = 0; i < groupNewsList.length; i++) {
                if (groupId == (Long.parseLong(groupNewsList[i]))) {
                    continue;
                }
                if (groupStrResult == "") {
                    groupStrResult += "" + groupId;
                } else {
                    groupStrResult += "," + groupId;
                }
            }
            redisUtils.set("groupNewsList", groupStr);
        }
        return true;
    }

    public String sendMessage(String groupId, String content) {
        if (groupId != null && groupId != "" && content != null && content != "") {
            global.getWizardBot().getGroup(Long.parseLong(groupId)).sendMessage(content);
            logger.info("sendMessage,success");
            return "success";
        } else {
            logger.info("sendMessage,fail,群号和内容不能为空");
            return "fail";
        }
    }

    public String autoSendNews() {
        if (redisUtils.get("autoDate") != null) {
            if (redisUtils.get("autoDate").equals(Tool.getCurrDate())) {
                logger.info("autoSendNews,bot0000004,已经发过了");
                return "bot0000004,已经发过了";
            }
        }

        logger.info("autoSendNews,开始新闻推送");

        String[] groupNewsList = getGroupNewsList();
        if (groupNewsList != null) {
            String news = news();
            if (news.indexOf("bot000000") == -1) {
                for (int i = 0; i < groupNewsList.length; i++) {
                    global.getWizardBot().getGroup(Long.parseLong(groupNewsList[i])).sendMessage(news);
                }
                //上传今天日期,证明今天已经启动
                redisUtils.set("autoDate", Tool.getCurrDate());
                logger.info("autoSendNews,success");
                return "success";
            } else {
                logger.info("autoSendNews," + news);
                return news;
            }
        }
        logger.info("autoSendNews,bot0000005,没有绑定群");
        return "bot0000005,没有绑定群";
    }

    public String weather(String city) throws Exception {
        if (botWeatherDateMap.get(city) != null) {
            if (botWeatherDateMap.get(city) + botWeatherCache > System.currentTimeMillis()) {
                logger.info("weather,获取缓存");
                return botWeatherContentMap.get(city);
            }
        }

        logger.info("weather,重新获取");

        //返回结果
        String result = "bot0000001,不存在的地区";

        String url = "https://bird.ioliu.cn/weather?city=" + city;
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = okHttpClient.newCall(request).execute();

        //获取返回的json
        String r;
        if (response.isSuccessful()) {
            r = response.body().string();
        } else {
            logger.info("weather,网络异常");
            return "bot0000002,网络异常";
        }

        Map<String, Object> map = JSON.parseObject(r, Map.class);

        String status = "" + map.get("status");
        if (status.equals("ok")) {
            //模板
            //桓台.今天2021-1-15,多云,温度1-10,东北风1-2级;明天2021-1-16,多云,温度1-10,东北风1-2级;后天2021-1-17,多云,温度1-10,东北风1-2级.天气更新时间2021-01-15 13:17.
            String resultStart;
            String resultEnd;
            String result1 = "今天";
            String result2 = "明天";
            String result3 = "后天";

            //获取地址
            Map basicMap = (Map) map.get("basic");
            String addr = "" + basicMap.get("city");
            //获取更新日期
            Map updateBasicMap = (Map) basicMap.get("update");
            String updateDate = "" + updateBasicMap.get("loc");
            resultStart = addr + ".\n";
            resultEnd = "天气更新时间" + updateDate + ".";

            //获取7天数据
            List weatherData = (List) map.get("daily_forecast");

            Map weatherDataTemp;//当天数据,包括当天日期
            Map cond;//获取天气
            Map tmp;//获取最高温度和最低温度
            Map wind;//获取风向和风力
            for (int i = 0; i < 3; i++) {
                weatherDataTemp = (Map) weatherData.get(i);
                cond = (Map) weatherDataTemp.get("cond");
                tmp = (Map) weatherDataTemp.get("tmp");
                wind = (Map) weatherDataTemp.get("wind");
                if (i == 0) {
                    result1 += weatherDataTemp.get("date") + "," + cond.get("txt_d") + ",温度" + tmp.get("min") + "℃-" + tmp.get("max") + "℃," + wind.get("dir") + wind.get("sc") + "级;\n";
                    continue;
                }
                if (i == 1) {
                    result2 += weatherDataTemp.get("date") + "," + cond.get("txt_d") + ",温度" + tmp.get("min") + "℃-" + tmp.get("max") + "℃," + wind.get("dir") + wind.get("sc") + "级;\n";
                    continue;
                }
                if (i == 2) {
                    result3 += weatherDataTemp.get("date") + "," + cond.get("txt_d") + ",温度" + tmp.get("min") + "℃-" + tmp.get("max") + "℃," + wind.get("dir") + wind.get("sc") + "级;\n";
                    continue;
                }
            }
            result = resultStart + result1 + result2 + result3 + resultEnd;
            botWeatherDateMap.put(city, System.currentTimeMillis());
            botWeatherContentMap.put(city, result);
        }
//        System.out.println(result);
        return result;
    }

    public String news() {
        //获取今天日期
        String currDateStr = Tool.getCurrDate();

        if (redisUtils.get(currDateStr) != null) {
            logger.info("news,获取缓存");
            return (String) redisUtils.get(currDateStr);
        }

        logger.info("news,重新获取");

        String url = "******";//狗头保命
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // 设置代理地址
        SocketAddress sa = new InetSocketAddress("127.0.0.1", global.getSockPort());
        builder.proxy(new Proxy(Proxy.Type.SOCKS, sa));

        OkHttpClient client = builder.build();

        Request request = new Request.Builder().url(url).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();

            String result;//获取html页面信息
            if (response.isSuccessful()) {
                result = response.body().string();
//            System.out.println(result);
                Document document = Jsoup.parse(result);//html的页面信息,String转Document
//            r = document.body().toString();

                //获取所有简讯,按天的,根据简讯的class筛选
//            System.out.println(document.body().getElementsByClass("tgme_widget_message_text"));
                List<Element> elementList = document.body().getElementsByClass("tgme_widget_message_text");
//            System.out.println(elementList.get(0));

                Element currElement = null;//当天简讯
                String dateStr;//简讯日期
                Element elementTemp;
                String resultStr;
                for (int i = elementList.size(); i > 0; i--) {
                    elementTemp = elementList.get(i - 1);
                    dateStr = elementTemp.getElementsByIndexEquals(0).get(0).toString();
                    if (dateStr.indexOf(currDateStr) != -1) {
                        currElement = elementTemp;
                        break;
                    }
                }
                if (currElement != null) {
//                System.out.println("" + currElement.text());
                    resultStr = ("" + currElement).replace("<br><br>", "\n");
//                System.out.println(resultStr);
//                return currElement.wholeText();
//                    botNewsMap.put(currDateStr, Jsoup.parse(resultStr).wholeText());
                    redisUtils.set(currDateStr, Jsoup.parse(resultStr).wholeText());
                    return Jsoup.parse(resultStr).wholeText();
                } else {
                    logger.info("news,今天没有新闻");
                    return "bot0000003,今天没有新闻";
                }
            } else {
                logger.info("news,网络异常");
                return "bot0000002,网络异常";
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("news,网络异常");
            return "bot0000002,网络异常";
        }
    }

    //例,生存天数19951111
    public String getSurvivalDays(String str) throws ParseException {
        String result = "";

        String[] s = str.split("生存天数");
        String birthday = s[1];

        //出生日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date = sdf.parse(birthday);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        //当天日期
        Date currDate = new Date();
        Calendar currCalendar = Calendar.getInstance();

        //判断日期是否正常
        if (currDate.before(date)) {
            return "日期异常";
        }

        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日");
        Long days = (currDate.getTime() - date.getTime()) / (24 * 60 * 60 * 1000) + 1;
        result += "如果说" + sdf2.format(date) + "是第一天,那么今天是你在世界上的第" + days + "天\n";

        //计算年
        int currYear = currCalendar.get(Calendar.YEAR);
        int year = calendar.get(Calendar.YEAR);
        int years = currYear - year + 1;
        result += "你今年虚岁:" + years + "岁\n";

        years = currYear - year;
        if (years <= 0) {
            result += "你今年周岁:" + years + "岁";
        } else {
            int currMonth = currCalendar.get(Calendar.MONTH);
            int currDay = currCalendar.get(Calendar.DAY_OF_MONTH);

            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            if ((currMonth < month) || (currMonth == month && currDay <= day)) {
                years--;
            }
            result += "你今年周岁:" + (years < 0 ? 0 : years) + "岁";
        }
        return result;
    }
}
