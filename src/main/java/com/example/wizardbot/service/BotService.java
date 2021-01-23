package com.example.wizardbot.service;

import com.alibaba.fastjson.JSON;
import com.example.wizardbot.contants.Global;
import com.example.wizardbot.utils.Tool;
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
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

/**
 * @Auther: auther
 * @Date: 2021/1/17 11:23
 * @Description:
 */

/**
 * bot0000001:不存在的地区
 * bot0000002:网络异常
 * bot0000003:今天没有新闻
 * bot0000004:已经发过了
 * bot0000005:没有绑定群
 * bot0000006:IO异常
 * bot0000007:发送群图片失败
 * bot0000008:日期异常
 */
@Service
public class BotService {
    private static final Logger logger = LoggerFactory.getLogger(BotService.class);

    @Autowired
    Global global;

    @Autowired
    private RedisUtils redisUtils;

    private static final Base64.Decoder decoder = Base64.getDecoder();

    //天气缓存,<城市,时间戳>,13位时间戳
    Map<String, Long> botWeatherDateMap = new HashMap<>();
    //天气缓存,<城市,内容>
    Map<String, String> botWeatherContentMap = new HashMap<>();
    //天气缓存时间,5分钟,1000*60*5
    int botWeatherCache = 1000 * 60 * 5;

    //图片属性
    int line = 40;//行高
    int lingTextNum = 18;//每行字数

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

    public String sendGroupMessage(String groupId, String content) {
        if (groupId != null && groupId != "" && content != null && content != "") {
            global.getWizardBot().getGroup(Long.parseLong(groupId)).sendMessage(new PlainText(content));
            logger.info("sendMessage,success");
            return "success";
        } else {
            logger.info("sendMessage,fail,群号和内容不能为空");
            return "fail";
        }
    }

    public String sendGroupImage(String groupId, String img) {
        try {
            ExternalResource image = ExternalResource.create(new ByteArrayInputStream(decoder.decode(img)));
            global.getWizardBot().getGroup(Long.parseLong(groupId)).sendMessage(global.getWizardBot().getGroup(Long.parseLong(groupId)).uploadImage(image));
            return "sendGroupImage,success";
        } catch (Exception e) {
            logger.info("autoSendNews,bot0000007,发送群图片失败");
            return "sendGroupImage,bot0000007,发送群图片失败";
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
            String news = getNews();
            if (news.indexOf("bot000000") == -1) {
                for (int i = 0; i < groupNewsList.length; i++) {
                    sendGroupImage(groupNewsList[i], news);
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

    public String weather(String city) {
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
        Response response = null;
        //获取返回的json
        String r = "";
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                r = response.body().string();
            } else {
                logger.info("weather,网络异常");
                return "bot0000002,网络异常";
            }
        } catch (IOException e) {
            logger.info("weather,bot0000006,IO异常");
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

    public String getNews() {
        String currDateStr = Tool.getCurrDate();

        if (redisUtils.get(currDateStr) != null) {
            logger.info("news,获取缓存");
            return stringToPicture((String) redisUtils.get(currDateStr));
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
                    redisUtils.set(currDateStr, Jsoup.parse(resultStr).wholeText());
                    return stringToPicture(Jsoup.parse(resultStr).wholeText());
                } else {
                    logger.info("news,今天没有新闻");
                    return "bot0000003,今天没有新闻";
                }
            } else {
                logger.info("news,网络异常");
                return "bot0000002,网络异常";
            }
        } catch (IOException e) {
//            e.printStackTrace();
            logger.info("news,网络异常");
            return "bot0000002,网络异常";
        }
    }

    public String stringToPicture(String news) {
        String[] newsG = news.split("\n");
        int height = 0;
        for (int i = 0; i < newsG.length; i++) {
            if (newsG[i].equals("\n") || newsG[i].trim().equals("")) {
                height++;
                continue;
            }
            if (newsG[i].length() > lingTextNum) {
                height += newsG[i].length() / lingTextNum + 1;
                continue;
            } else {
                height++;
            }
        }
        height = height * line + line;
        BufferedImage bufferedImage = createImage(newsG, new Font("楷体", Font.PLAIN, 24), 500, height);
        //输出流
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", stream);
            byte[] bytes = stream.toByteArray();//转换成字节
            BASE64Encoder encoder = new BASE64Encoder();
            String png_base64 = encoder.encodeBuffer(bytes).trim();//转换成base64串
            png_base64 = png_base64.replaceAll("\n", "").replaceAll("\r", "");//删除 \r\n
            logger.info("stringToPicture,success");
            return png_base64;
        } catch (IOException e) {
            logger.info("stringToPicture,bot0000006,IO异常");
            return "bot0000006,IO异常";
        }
    }

    public BufferedImage createImage(String[] str, Font font, Integer width, Integer height) {
        // 创建图片
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_BGR);
        Graphics2D g = image.createGraphics();
        g.setClip(0, 0, width, height);
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);// 先用黑色填充整张图片,也就是背景
        g.setColor(Color.black);// 在换成黑色
        g.setFont(font);// 设置画笔字体

        String strTemp;
        int startX = 30;//起始X
        int startY = 35;//起始Y

        int isNull = 0;

        for (int i = 0; i < str.length; i++) {
            strTemp = str[i];
            if (strTemp.equals("\n") || strTemp.trim().equals("")) {
                if (isNull == 0) {
                    g.drawString("", startX, startY);
                    startY += line;
                    isNull = 1;
                }
                continue;
            }
            isNull = 0;
            if (strTemp.length() > lingTextNum) {
                for (int j = 0; j <= strTemp.length() / lingTextNum; j++) {
                    if (j == strTemp.length() / lingTextNum) {
                        g.drawString(strTemp.substring(j * lingTextNum), startX, startY);
                        startY += line;
                    } else {
                        g.drawString(strTemp.substring(j * lingTextNum, (j * lingTextNum) + lingTextNum), startX, startY);
                        startY += line;
                    }
                }
                continue;
            } else {
                g.drawString(strTemp, startX, startY);
                startY += line;
            }
        }
        g.drawString("", startX, startY);

        //加水印
        String waterMarkContent = "老刘星什么时候结婚";
        g.drawImage(image, 0, 0, width, height, null);
        g.setColor(new Color(169, 169, 169)); //设置水印颜色
        g.setFont(new Font("楷体", Font.ITALIC, 32));              //设置字体
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));//设置水印文字透明度
        g.rotate(Math.toRadians(45));//设置水印旋转

        JLabel label = new JLabel(waterMarkContent);
        FontMetrics metrics = label.getFontMetrics(font);
        int width1 = metrics.stringWidth(label.getText());//文字水印的宽
        int rowsNumber = height / width1;// 图片的高  除以  文字水印的宽    ——> 打印的行数(以文字水印的宽为间隔)
        int columnsNumber = width / width1;//图片的宽 除以 文字水印的宽   ——> 每行打印的列数(以文字水印的宽为间隔)
        //防止图片太小而文字水印太长，所以至少打印一次
        if (rowsNumber < 1) {
            rowsNumber = 1;
        }
        if (columnsNumber < 1) {
            columnsNumber = 1;
        }
        for (int j = 0; j < rowsNumber; j++) {
            for (int i = 0; i < columnsNumber; i++) {
                g.drawString(waterMarkContent, i * width1 + j * width1, -i * width1 + j * width1);//画出水印,并设置水印位置
            }
        }

        //释放资源
        g.dispose();
        logger.info("createImage,success");
        return image;
    }

    //例,生存天数19951111,事件直接调用的方法
    public String getSurvivalDays(String str) {
        String result = "";

        String[] s = str.split("生存天数");
        String birthday = s[1];

        //出生日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date;
        try {
            date = sdf.parse(birthday);
        } catch (ParseException e) {
            logger.info("getSurvivalDays,bot0000007,日期异常");
            return "bot0000007,日期异常";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        //当天日期
        Date currDate = new Date();
        Calendar currCalendar = Calendar.getInstance();

        //判断日期是否正常
        if (currDate.before(date)) {
            logger.info("getSurvivalDays,bot0000007,日期异常");
            return "bot0000007,日期异常";
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
