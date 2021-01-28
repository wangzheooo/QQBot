package com.example.wizardbot.service;

import com.alibaba.fastjson.JSON;
import com.example.wizardbot.contants.Global;
import com.example.wizardbot.utils.BotUtils;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
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

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private RedisService redisService;

    private static final Base64.Decoder decoder = Base64.getDecoder();

    //天气缓存,<城市,时间戳>,13位时间戳
    Map<String, Long> botWeatherDateMap = new HashMap<>();
    //天气缓存,<城市,内容>
    Map<String, String> botWeatherContentMap = new HashMap<>();
    //天气缓存时间,5分钟,1000*60*5
    int botWeatherCache = 1000 * 60 * 5;

    public String[] getGroupNewsList() {
        //群号缓存
        String groupStr = (String) redisService.get("groupNewsList");
        if (groupStr == null || groupStr == "" || groupStr.equals("")) {
            return null;
        }
        //群号数组
        String[] groupNewsList = groupStr.split(",");
        return groupNewsList;
    }

    public boolean addGroup(Long groupId) {
        String groupStr = (String) redisService.get("groupNewsList");
        if (groupStr == null || groupStr == "" || groupStr.equals("")) {
            redisService.set("groupNewsList", "" + groupId);
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
                    redisService.set("groupNewsList", groupStr);
                    logger.info("addGroup,添加成功");
                }
            }
        }
        return true;
    }

    public boolean delGroup(Long groupId) {
        String groupStr = (String) redisService.get("groupNewsList");
        if (groupStr == null || groupStr == "" || groupStr.equals("")) {
            logger.info("delGroup,删除失败,群号不存在");
            return false;
        } else {
            String[] groupNewsList = groupStr.split(",");
            String groupStrResult = "";
            boolean flag = false;
            for (int i = 0; i < groupNewsList.length; i++) {
                if (groupId == (Long.parseLong(groupNewsList[i]))) {
                    flag = true;
                    continue;
                }
                if (groupStrResult == "") {
                    groupStrResult += "" + groupNewsList[i];
                } else {
                    groupStrResult += "," + groupNewsList[i];
                }
            }
            if (flag) {
                redisService.set("groupNewsList", groupStrResult);
                logger.info("delGroup,删除成功");
                return true;
            } else {
                logger.info("delGroup,删除失败,群号不存在");
                return false;
            }
        }
    }

    /**
     * 发送群消息
     *
     * @return map status-状态;msg-执行信息;result-返回值
     */
    public Map sendGroupMessage(String groupId, String content) {
        Map resultMap = new HashMap();
        if (groupId != null && groupId != "" && content != null && content != "") {
            MessageReceipt messageReceipt = global.getWizardBot().getGroup(Long.parseLong(groupId)).sendMessage(new PlainText(content));
            if (messageReceipt.isToGroup()) {
                logger.info("sendMessage,success");
                resultMap.put("status", "success");
                resultMap.put("msg", "sendMessage,success");
                resultMap.put("result", "success");
                return resultMap;
            }
            logger.info("sendMessage,没有发到群");
            resultMap.put("status", "fail");
            resultMap.put("msg", "sendMessage,没有发到群");
            return resultMap;
        } else {
            logger.info("sendMessage,群号或内容不能为空");
            resultMap.put("status", "fail");
            resultMap.put("msg", "sendMessage,群号或内容不能为空");
            return resultMap;
        }
    }

    /**
     * 发送群图片
     *
     * @return map status-状态;msg-执行信息;result-返回值
     */
    public Map sendGroupImage(String groupId, String img) {
        Map resultMap = new HashMap();
        try {
            ExternalResource image = ExternalResource.create(new ByteArrayInputStream(decoder.decode(img)));
            MessageReceipt messageReceipt = global.getWizardBot().getGroup(Long.parseLong(groupId)).sendMessage(global.getWizardBot().getGroup(Long.parseLong(groupId)).uploadImage(image));
            if (messageReceipt.isToGroup()) {
                logger.info("sendGroupImage,success");
                resultMap.put("status", "success");
                resultMap.put("msg", "sendGroupImage,success");
                resultMap.put("result", "success");
                return resultMap;
            }
            logger.info("sendGroupImage,没有发到群");
            resultMap.put("status", "fail");
            resultMap.put("msg", "sendGroupImage,没有发到群");
            return resultMap;
        } catch (Exception e) {
            logger.info("sendGroupImage,发送群图片失败");
            resultMap.put("status", "fail");
            resultMap.put("msg", "sendGroupImage,发送群图片失败");
            return resultMap;
        }
    }

    /**
     * 自动发送新闻图片
     *
     * @return map status-状态;msg-执行信息;result-返回值
     */
    public Map autoSendNews() {
        Map resultMap = new HashMap();
        if (redisService.get("autoDate") != null) {
            if (redisService.get("autoDate").equals(BotUtils.getCurrDate())) {
                logger.info("autoSendNews,已经发过了");
                resultMap.put("status", "fail");
                resultMap.put("msg", "autoSendNews,已经发过了");
                return resultMap;
            }
        }
        logger.info("autoSendNews,开始新闻推送");
        String[] groupNewsList = getGroupNewsList();
        if (groupNewsList != null) {
            Map newsMap = getCurrNewsBase64();
            if (newsMap.get("status").equals("success")) {
                for (int i = 0; i < groupNewsList.length; i++) {
                    sendGroupImage(groupNewsList[i], (String) newsMap.get("result"));
                    if (groupNewsList.length > 1) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //上传今天日期,证明今天已经启动
                redisService.set("autoDate", BotUtils.getCurrDate());

                logger.info("autoSendNews,success");
                resultMap.put("status", "success");
                resultMap.put("msg", "autoSendNews,success");
                resultMap.put("result", "success");
                return resultMap;
            } else {
                logger.info("autoSendNews," + newsMap.get("msg"));
                resultMap.put("status", "fail");
                resultMap.put("msg", "autoSendNews," + newsMap.get("msg"));
                return resultMap;
            }
        }
        logger.info("autoSendNews,没有绑定群");
        resultMap.put("status", "fail");
        resultMap.put("msg", "autoSendNews,没有绑定群");
        return resultMap;
    }

    /**
     * 获取三天天气
     *
     * @return map status-状态;msg-执行信息;result-返回值
     */
    public Map weather(String city) {
        Map resultMap = new HashMap();
        if (botWeatherDateMap.get(city) != null) {
            if (botWeatherDateMap.get(city) + botWeatherCache > System.currentTimeMillis()) {
                logger.info("weather,获取缓存");
                resultMap.put("status", "success");
                resultMap.put("msg", "weather,success");
                resultMap.put("result", botWeatherContentMap.get(city));
                return resultMap;
            }
        }
        logger.info("weather,重新获取");
        String url = "https://bird.ioliu.cn/weather?city=" + city;
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response;
        //获取返回的json
        String r;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                r = response.body().string();
            } else {
                logger.info("weather,网络异常");
                resultMap.put("status", "fail");
                resultMap.put("msg", "weather,网络异常");
                return resultMap;
            }
        } catch (IOException e) {
            logger.info("weather,IO异常");
            resultMap.put("status", "fail");
            resultMap.put("msg", "weather,IO异常");
            return resultMap;
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
            String result = resultStart + result1 + result2 + result3 + resultEnd;
            botWeatherDateMap.put(city, System.currentTimeMillis());
            botWeatherContentMap.put(city, result);

            logger.info("weather,success");
            resultMap.put("status", "success");
            resultMap.put("msg", "weather,success");
            resultMap.put("result", result);
            return resultMap;
        } else {
            logger.info("weather,不存在的地区");
            resultMap.put("status", "fail");
            resultMap.put("msg", "weather,不存在的地区");
            return resultMap;
        }
    }

    /**
     * 获取当天新闻简讯,图片
     *
     * @return map status-状态;msg-执行信息;result-返回值(base64)
     */
    public Map getCurrNewsBase64() {
        Map resultMap = new HashMap();
        Map map = getCurrNewsString();
        if ((map.get("status")).equals("success")) {
            String newsString = (String) map.get("result");
            Map newsBase64Map = BotUtils.stringToBase64(newsString, new Font("楷体", Font.PLAIN, 24), 18, 40, 500);
            if ((newsBase64Map.get("status")).equals("success")) {
                logger.info("getCurrNewsBase64,success");
                resultMap.put("status", "success");
                resultMap.put("msg", "getCurrNewsBase64,success");
                resultMap.put("result", newsBase64Map.get("result"));
                return resultMap;
            }
        }
        logger.info("getCurrNewsBase64," + map.get("msg"));
        resultMap.put("status", "fail");
        resultMap.put("msg", "getCurrNewsBase64," + map.get("msg"));
        return resultMap;

    }

    /**
     * 获取当天新闻简讯,文字
     *
     * @return map status-状态;msg-执行信息;result-返回值
     */
    public Map getCurrNewsString() {
        Map resultMap = new HashMap();
        String currDateStr = BotUtils.getCurrDate();
        if (redisService.get(currDateStr) != null) {
            logger.info("getCurrNewsString,获取缓存success");
            resultMap.put("status", "success");
            resultMap.put("msg", "getCurrNewsString,success");
            resultMap.put("result", redisService.get(currDateStr));
            return resultMap;
        }
        logger.info("news,重新获取");
        String url = "https://t.me/s/pojieapk";
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // 设置代理地址
        SocketAddress sa = new InetSocketAddress("127.0.0.1", global.getSockPort());
        builder.proxy(new Proxy(Proxy.Type.SOCKS, sa));

        OkHttpClient client = builder.build();
        Request request = new Request.Builder().url(url).build();
        Response response;
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
                    redisService.set(currDateStr, Jsoup.parse(resultStr).wholeText());

                    logger.info("getCurrNewsString,获取成功success");
                    resultMap.put("status", "success");
                    resultMap.put("msg", "getCurrNewsString,success");
                    resultMap.put("result", Jsoup.parse(resultStr).wholeText());
                    return resultMap;
                } else {
                    logger.info("getCurrNewsString,今天没有新闻");
                    resultMap.put("status", "fail");
                    resultMap.put("msg", "getCurrNewsString,今天没有新闻");
                    return resultMap;
                }
            } else {
                logger.info("getCurrNewsString,网络异常");
                resultMap.put("status", "fail");
                resultMap.put("msg", "getCurrNewsString,网络异常");
                return resultMap;
            }
        } catch (IOException e) {
            logger.info("getCurrNewsString,网络异常");
            resultMap.put("status", "fail");
            resultMap.put("msg", "getCurrNewsString,网络异常");
            return resultMap;
        }
    }

    /**
     * 生存日期功能,获取出生日期后,计算年龄和生存天数
     *
     * @param str 例,生存天数19951111
     * @return map status-状态;msg-执行信息;result-返回值
     */
    public Map getSurvivalDays(String str) {
        return BotUtils.getSurvivalDays(str);
    }
}
