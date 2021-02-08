package com.example.wizardbot.utils;

import com.alibaba.fastjson.JSON;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

/**
 * @Auther: auther
 * @Date: 2021/1/17 11:59
 * @Description:
 */
public class BotUtils {
    private static final Logger logger = LoggerFactory.getLogger(BotUtils.class);

    /**
     * 获取今天日期,新闻和天气用
     *
     * @return date 例:1月09日,1月10日
     */
    public static String getCurrDate() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        if (day < 10) {
            return "" + month + "月" + "0" + day + "日";
        }
        return "" + month + "月" + day + "日";
    }

    /**
     * 获取今天日期,新闻每日热点简报用
     *
     * @return date 例:1月9日,1月10日
     */
    public static String getCurrDate1() {
        Calendar calendar = Calendar.getInstance();
        return "" + (calendar.get(Calendar.MONTH) + 1) + "月" + (calendar.get(Calendar.DATE)) + "日";
    }

    /**
     * 获取昨天日期,新闻用
     *
     * @return date 例:1月09日,1月10日
     */
    public static String getYesterdayDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(calendar.DATE, -1);

        int day = calendar.get(Calendar.DATE);
        if (day < 10) {
            return "" + (calendar.get(Calendar.MONTH) + 1) + "月" + "0" + day + "日";
        }
        return "" + (calendar.get(Calendar.MONTH) + 1) + "月" + day + "日";
    }

    /**
     * 获取随机值
     *
     * @param start 起始
     * @param end   截止
     * @return random 随机数
     */
    public static int getRandom(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }

    /**
     * 生存日期功能,获取出生日期后,计算年龄和生存天数
     *
     * @param str 例,生存天数19951111
     * @return map status-状态;msg-执行信息;result-返回值
     */
    public static Map getSurvivalDays(String str) {
        Map resultMap = new HashMap();
        String result = "";

        String[] s = str.split(".年龄");
        String birthday = s[1];

        //出生日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date;
        try {
            date = sdf.parse(birthday);
        } catch (ParseException e) {
            logger.info("getSurvivalDays,日期格式异常");
            resultMap.put("status", "fail");
            resultMap.put("msg", "日期异常");
            return resultMap;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        //当天日期
        Date currDate = new Date();
        Calendar currCalendar = Calendar.getInstance();

        //判断日期是否正常
        if (currDate.before(date)) {
            logger.info("getSurvivalDays,日期大小异常");
            resultMap.put("status", "fail");
            resultMap.put("msg", "日期大小异常");
            return resultMap;
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

        logger.info("getSurvivalDays,success");
        resultMap.put("status", "success");
        resultMap.put("msg", "success");
        resultMap.put("result", result);
        return resultMap;
    }

    /**
     * 文字转图片
     *
     * @param news        文字
     * @param font        字体
     * @param lineTextNum 每行字数
     * @param lineHeight  行高
     * @param width       图片宽
     * @return map status-状态;msg-执行信息;result-返回值(base64)
     */
    public static Map stringToBase64(String news, Font font, int lineTextNum, int lineHeight, Integer width) {
        Map resultMap = new HashMap();

        String[] newsG = news.split("\n");
        int height = 0;
        for (int i = 0; i < newsG.length; i++) {
            if (newsG[i].equals("\n") || newsG[i].trim().equals("")) {
                height++;
                continue;
            }
            if (newsG[i].length() > lineTextNum) {
                height += newsG[i].length() / lineTextNum + 1;
                continue;
            } else {
                height++;
            }
        }
        height = height * lineHeight + lineHeight;

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

        for (int i = 0; i < newsG.length; i++) {
            strTemp = newsG[i];
            if (strTemp.equals("\n") || strTemp.trim().equals("")) {
                if (isNull == 0) {
                    g.drawString("", startX, startY);
                    startY += lineHeight;
                    isNull = 1;
                }
                continue;
            }
            isNull = 0;
            if (strTemp.length() > lineTextNum) {
                for (int j = 0; j <= strTemp.length() / lineTextNum; j++) {
                    if (j == strTemp.length() / lineTextNum) {
                        g.drawString(strTemp.substring(j * lineTextNum), startX, startY);
                        startY += lineHeight;
                    } else {
                        g.drawString(strTemp.substring(j * lineTextNum, (j * lineTextNum) + lineTextNum), startX, startY);
                        startY += lineHeight;
                    }
                }
                continue;
            } else {
                g.drawString(strTemp, startX, startY);
                startY += lineHeight;
            }
        }
        g.drawString("", startX, startY);
        //加水印
        String waterMarkContent = "我是水印我是水印";
        g.drawImage(image, 0, 0, width, height, null);
        g.setColor(new Color(169, 169, 169)); //设置水印颜色
        g.setFont(new Font("华文琥珀", Font.ITALIC, 34));              //设置字体
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.4f));//设置水印文字透明度
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

        //输出流
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", stream);
            byte[] bytes = stream.toByteArray();
            BASE64Encoder encoder = new BASE64Encoder();
            String png_base64 = encoder.encodeBuffer(bytes).trim();
            png_base64 = png_base64.replaceAll("\n", "").replaceAll("\r", "");

            logger.info("stringToBase64,success");
            resultMap.put("status", "success");
            resultMap.put("msg", "success");
            resultMap.put("result", png_base64);
            return resultMap;
        } catch (IOException e) {
            logger.info("stringToBase64,IO异常");
            resultMap.put("status", "fail");
            resultMap.put("msg", "IO异常");
            return resultMap;
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 快餐推荐,用百度地图的api
     *
     * @param ak           百度地图密钥
     * @param city         城市,例,北京/淄博/桓台
     * @param priceSection 价格区间,例,5,25
     * @return map status-状态;msg-执行信息;result-返回值
     */
    public static Map getKuaiCan(String ak, String city, String priceSection) {
        Map resultMap = new HashMap();

        if (city == null || city.trim().equals("")) {
            logger.info("getKuaiCan,城市不能为空");
            resultMap.put("status", "fail");
            resultMap.put("msg", "城市不能为空");
            return resultMap;
        }

        if (priceSection == null || priceSection.trim().equals("")) {
            logger.info("getKuaiCan,价格区间不能为空");
            resultMap.put("status", "fail");
            resultMap.put("msg", "价格区间不能为空");
            return resultMap;
        }

        //百度地图,地点检索url
        String url = "http://api.map.baidu.com/place/v2/search?";
        int pageSize = 20;

        Map map = new HashMap();
        map.put("query", "美食");//关键字
//        map.put("tag", "快餐");
        map.put("region", city);//检索行政区划区域
        map.put("output", "json");
        map.put("ak", ak);
        map.put("scope", "2");//检索结果详细程度,1简单,2详细
        map.put("filter", "industry_type:cater|price_section:" + priceSection);//检索过滤条件,cater（餐饮）
        map.put("page_size", "" + pageSize);

        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            url += pairs.getKey() + "=" + pairs.getValue() + "&";
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                Map<String, Object> mapJson = JSON.parseObject(response.body().string(), Map.class);
                if (mapJson.get("result_type").equals("poi_type")) {
                    List<Map> mapList = (List<Map>) mapJson.get("results");
                    if (mapList.size() > 0) {
                        String area1 = (String) mapList.get(0).get("area");
                        String city1 = (String) mapList.get(0).get("city");
                        if ((area1.indexOf(city) != -1) || (city1.indexOf(city) != -1)) {
                            int total = (int) mapJson.get("total");
                            int no = BotUtils.getRandom(1, total);
                            int page = (no / pageSize) + 1;
                            int pageNo = (no - (page - 1) * pageSize) == 0 ? pageSize : (no - (page - 1) * pageSize);

                            url += "page_num=" + (page - 1);
                            request = new Request.Builder().url(url).build();
                            response = okHttpClient.newCall(request).execute();
                            if (response.isSuccessful()) {
                                mapJson = JSON.parseObject(response.body().string(), Map.class);
                                mapList = (List<Map>) mapJson.get("results");

                                Map shopMap = mapList.get(pageNo - 1);
                                Map shopDetailInfoMap = (Map) shopMap.get("detail_info");
                                String shopName = (String) shopMap.get("name");
                                String shopAddress = (String) shopMap.get("address");
                                String shopAveragePrice = (String) shopDetailInfoMap.get("price");
                                String shopOverallRating = (String) shopDetailInfoMap.get("overall_rating");

                                String result = "店铺:" + shopName + "\n" + "地址:" + shopAddress + "\n" + "人均:" + shopAveragePrice + "\n" + "评价:" + (shopOverallRating == null ? "无评价" : shopOverallRating);
                                logger.info("getKuaiCan,success");
                                resultMap.put("status", "success");
                                resultMap.put("msg", "success");
                                resultMap.put("result", result);
                                return resultMap;
                            }
                        }
                    }
                }
                logger.info("getKuaiCan,城市错误," + city);
                resultMap.put("status", "fail");
                resultMap.put("msg", "城市错误");
            }
            logger.info("getKuaiCan,接口异常");
            resultMap.put("status", "fail");
            resultMap.put("msg", "接口异常");
        } catch (Exception e) {
            logger.info("getKuaiCan,网络异常");
            resultMap.put("status", "fail");
            resultMap.put("msg", "网络异常");
        } finally {
            response.close();
            return resultMap;
        }
    }

    /**
     * 今日NBA赛事
     *
     * @return map status-状态;msg-执行信息;result-返回值
     */
    public static Map getNBAInfo() {
        Map resultMap = new HashMap();
        String currDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String resultStr;
        String url = "https://nba.hupu.com/games/" + currDate;
        try {
            Document document = Jsoup.connect(url).get();
            //gamecenter_content_l
            Elements elementsByTag = document.getElementsByClass("list_box");
            if (elementsByTag != null && elementsByTag.size() > 0) {
                resultStr = currDate + "赛事\n";
                resultStr += "------\n";
                for (Element tag : elementsByTag) {
                    String[] str1 = tag.getElementsByClass("team_vs_a_1").text().split(" ");
                    String[] str2 = tag.getElementsByClass("team_vs_a_2").text().split(" ");
                    if (str1.length == 2 && str2.length == 2) {
                        resultStr += str1[1] + " " + str1[0] + " - " + str2[0] + " " + str2[1] + "\n";
                    } else {
                        resultStr += str1[0] + " - " + str2[0] + "\n";
                    }
                }
                logger.info("getNBAInfo,success");
                resultMap.put("status", "success");
                resultMap.put("msg", "success");
                resultMap.put("result", resultStr);
                return resultMap;
            } else {
                logger.info("getNBAInfo,今天没有比赛");
                resultMap.put("status", "fail");
                resultMap.put("msg", "今天没有比赛");
                return resultMap;
            }
        } catch (IOException e) {
            logger.info("getNBAInfo,IO异常");
            resultMap.put("status", "fail");
            resultMap.put("msg", "IO异常");
            return resultMap;
        }
    }
}
