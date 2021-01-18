package com.example.wizardbot.utils;

import java.util.Calendar;

/**
 * @Auther: auther
 * @Date: 2021/1/17 11:59
 * @Description:
 */
public class Tool {

    //获取今天日期,例:1月9日,1月10日
    public static String getCurrDate() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        return "" + month + "月" + day + "日";
    }

    public static int get_random(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }

}
