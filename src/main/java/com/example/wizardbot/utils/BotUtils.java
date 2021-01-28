package com.example.wizardbot.utils;

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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: auther
 * @Date: 2021/1/17 11:59
 * @Description:
 */
public class BotUtils {
    private static final Logger logger = LoggerFactory.getLogger(BotUtils.class);

    /**
     * 获取今天日期
     *
     * @return date 例:1月9日,1月10日
     */
    public static String getCurrDate() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        return "" + month + "月" + day + "日";
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

        String[] s = str.split("生存天数");
        String birthday = s[1];

        //出生日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date;
        try {
            date = sdf.parse(birthday);
        } catch (ParseException e) {
            logger.info("getSurvivalDays,日期格式异常");
            resultMap.put("status", "fail");
            resultMap.put("msg", "getSurvivalDays,日期异常");
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
            resultMap.put("msg", "getSurvivalDays,日期大小异常");
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
        resultMap.put("msg", "getSurvivalDays,success");
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
        String waterMarkContent = "老刘星什么时候结婚";
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
            resultMap.put("msg", "stringToBase64,success");
            resultMap.put("result", png_base64);
            return resultMap;
        } catch (IOException e) {
            logger.info("stringToBase64,IO异常");
            resultMap.put("status", "fail");
            resultMap.put("msg", "stringToBase64,IO异常");
            return resultMap;
        }
    }

}
