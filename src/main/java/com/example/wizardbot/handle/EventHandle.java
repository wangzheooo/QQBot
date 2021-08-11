package com.example.wizardbot.handle;

import com.alibaba.fastjson.JSONObject;
import com.example.wizardbot.contants.Global;
import com.example.wizardbot.service.BotService;
import com.example.wizardbot.utils.BotUtils;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

/**
 * @Auther: auther
 * @Date: 2021/1/23 11:23
 * @Description:
 */
@Component
public class EventHandle extends SimpleListenerHost {
    private static final Logger logger = LoggerFactory.getLogger(EventHandle.class);

    @Autowired
    private Global global;

    @Autowired
    private BotService botService;

    @EventHandler()
    public ListeningStatus onNewFriendRequest(NewFriendRequestEvent event) {
        String fromNick = event.getMessage();
        if (fromNick.equals("1233211234567")) {
            event.accept();
            logger.info("添加好友" + event.getFromId());
        } else {
            logger.info("添加好友失败,暗号没对上");
        }
        return ListeningStatus.LISTENING;
    }

    @EventHandler()
    public ListeningStatus onBotInvitedJoinGroupRequest(BotInvitedJoinGroupRequestEvent event) {
        if (botService.getLastGroupNum() > 0) {
            event.accept();
            logger.info("被拉入群" + event.getGroupId());
            botService.subGroupNum();
        }
        return ListeningStatus.LISTENING;
    }

    @EventHandler()
    public ListeningStatus onBotJoinGroup(BotJoinGroupEvent event) {
        event.getGroup().sendMessage(new PlainText(global.getMenu()));
        logger.info("首次进群功能介绍 " + event.getGroup().getId());
        return ListeningStatus.LISTENING;
    }

    @EventHandler()
    public ListeningStatus onFriendMessage(FriendMessageEvent event) {
        logger.info("收到好友消息" + event.getFriend().getId());
        event.getFriend().sendMessage(new PlainText("只支持群聊"));
        return ListeningStatus.LISTENING;
    }

    @NotNull
    @EventHandler
    public ListeningStatus getGroupMessage(@NotNull GroupMessageEvent event) {
        MessageChain message = event.getMessage();
//        System.out.println(message);
        String messageTemp = BotUtils.filterMessage(message);
//        System.out.println(messageTemp);
//        System.out.println(event.getMessage().contentToString());
        if (messageTemp.equals(".开启每日简讯")) {
            Long groupId = event.getSubject().getId();
            global.getExecutor().execute(() -> {
                if (botService.addGroup(groupId)) {
                    botService.sendGroupMessage(groupId, "已开启推送每日简讯");
                } else {
                    botService.sendGroupMessage(groupId, "已经开启了,不要重复开启");
                }
            });
            return ListeningStatus.LISTENING;
        } else if (messageTemp.equals(".关闭每日简讯")) {
            Long groupId = event.getSubject().getId();
            global.getExecutor().execute(() -> {
                if (botService.delGroup(groupId)) {
                    botService.sendGroupMessage(groupId, "已关闭每日简讯推送");
                } else {
                    botService.sendGroupMessage(groupId, "不存在的群号");
                }
            });
            return ListeningStatus.LISTENING;
        } else if (messageTemp.equals(".你好")) {
            event.getSubject().sendMessage(new PlainText(event.getSenderName() + "你好呀"));
            return ListeningStatus.LISTENING;
        } else if (messageTemp.equals(".新闻")) {
            Long groupId = event.getSubject().getId();
            global.getExecutor().execute(() -> {
                Map newsMap = botService.getCurrNewsBase64();
                if (newsMap.get("status").equals("success")) {
                    botService.sendGroupImage(groupId, (String) newsMap.get("result"));
                } else {
                    botService.sendGroupMessage(groupId, (String) newsMap.get("msg"));
                }
            });
            return ListeningStatus.LISTENING;
        } else if (messageTemp.equals(".菜单")) {
            event.getSubject().sendMessage(new PlainText(global.getMenu()));
            return ListeningStatus.LISTENING;
        } else if (messageTemp.equals(".nba")) {
            Long groupId = event.getSubject().getId();
            global.getExecutor().execute(() -> botService.getNBAInfo(groupId));
            return ListeningStatus.LISTENING;
        } else if (messageTemp.equals(".简餐")) {
            global.getExecutor().execute(() -> botService.getCater(event.getSubject().getId(), event.getSender().getId(), "1"));
            return ListeningStatus.LISTENING;
        } else if (messageTemp.equals(".大餐")) {
            global.getExecutor().execute(() -> botService.getCater(event.getSubject().getId(), event.getSender().getId(), "2"));
            return ListeningStatus.LISTENING;
        } else if (messageTemp.startsWith(".年龄")) {
            Long groupId = event.getSubject().getId();
            String finalMessageTemp = messageTemp;
            String demoStr = ".年龄20200130";
            if (messageTemp.length() == demoStr.length()) {
                global.getExecutor().execute(() -> {
                    Map map = botService.getSurvivalDays(finalMessageTemp);
                    if (map.get("status").equals("success")) {
                        botService.sendGroupMessage(groupId, (String) map.get("result"));
                    } else {
                        botService.sendGroupMessage(groupId, (String) map.get("msg"));
                    }

                });
            }
            return ListeningStatus.LISTENING;
        } else if (messageTemp.startsWith(".二维码")) {
            Long groupId = event.getSubject().getId();
            String finalMessageTemp = messageTemp;
            String[] qrStr = finalMessageTemp.split(".二维码");
            if (qrStr.length > 0) {
                global.getExecutor().execute(() -> botService.generateQRCodeImage(groupId, qrStr[1]));
            }
            return ListeningStatus.LISTENING;
        } else if (messageTemp.startsWith(".简餐")) {
//            event.getSubject().sendMessage(new PlainText("敬请期待"));
            //无论是简餐还是大餐,地点最好越详细越好,例: .简餐张店区火炬公园 .简餐济南市奥体中心 .简餐济南市高新区软件园
            String finalMessageTemp = messageTemp;
            String[] str = finalMessageTemp.split(".简餐");
            if (str.length > 0) {
                global.getExecutor().execute(() -> botService.getCater(event.getSubject().getId(), event.getSender().getId(), str[1], "1"));
            }
            return ListeningStatus.LISTENING;
        } else if (messageTemp.startsWith(".大餐")) {
            String finalMessageTemp = messageTemp;
            String[] str = finalMessageTemp.split(".大餐");
            if (str.length > 0) {
                global.getExecutor().execute(() -> botService.getCater(event.getSubject().getId(), event.getSender().getId(), str[1], "2"));
            }
            return ListeningStatus.LISTENING;
        } else if (messageTemp.indexOf("天气") != -1) {
            Long groupId = event.getSubject().getId();
            String finalMessageTemp = messageTemp;
            if (finalMessageTemp.length() >= 4 && finalMessageTemp.length() < 10) {
                global.getExecutor().execute(() -> {
                    String[] weatherContent = finalMessageTemp.split("天气");
                    Map map = botService.weather("" + weatherContent[0]);
                    if (map.get("status").equals("success")) {
                        botService.sendGroupMessage(groupId, (String) map.get("result"));
                    } else {
                        botService.sendGroupMessage(groupId, (String) map.get("msg"));
                    }
                });
            }
            return ListeningStatus.LISTENING;
        } else if (event.getMessage().contentToString().indexOf("位置分享") != -1 || event.getMessage().contentToString().indexOf("sharedmap") != -1) {
            Long groupId = event.getSubject().getId();
            String jsonApp = event.getMessage().contentToString();

            String lng = "";
            String lat = "";
            String address = "";

            if (BotUtils.isJson(jsonApp)) {
                JSONObject jsonObject = JSONObject.parseObject(jsonApp);

                jsonObject = (JSONObject) ((JSONObject) jsonObject.get("meta")).get("Location.Search");

                lng = (String) jsonObject.get("lng");
                lat = (String) jsonObject.get("lat");
                address = (String) jsonObject.get("address");

                if (botService.addUserInfo(event.getSender().getId(), lat, lng, address)) {
                    botService.sendGroupMessage(groupId, "已经记录你的位置,\n经度:" + lng + ";\n维度:" + lat + ";\n地址名:" + address);
                } else {
                    botService.sendGroupMessage(groupId, "记录位置失败,请联系管理员(0x0000001)");
                }

            } else if (BotUtils.isXML(jsonApp)) {
                for (String s : jsonApp.split("&amp;")) {
                    if (s.indexOf("lat=") != -1) {
//                        System.out.println(s.split("lat=")[1]);
                        lat = s.split("lat=")[1];
                    }
                    if (s.indexOf("lon=") != -1) {
//                        System.out.println(s.split("lon=")[1]);
                        lng = s.split("lon=")[1];
                    }
                    if (s.indexOf("loc=") != -1) {
//                        System.out.println(s.split("loc=")[1]);
                        address = s.split("loc=")[1];
                    }
                }
                if ((!lat.equals("")) && (!lng.equals("")) && (!address.equals(""))) {
                    if (botService.addUserInfo(event.getSender().getId(), lat, lng, address)) {
                        botService.sendGroupMessage(groupId, "已经记录你的位置,\n经度:" + lng + ";\n维度:" + lat + ";\n地址名:" + address);
                    } else {
                        botService.sendGroupMessage(groupId, "记录位置失败,请联系管理员(0x0000001)");
                    }
                }
            }

            return ListeningStatus.LISTENING;
        }
        return ListeningStatus.LISTENING;
    }

    @NotNull
    @EventHandler
    public void getBotOffline(@NotNull BotOfflineEvent event) throws Exception {
        logger.error("掉了");
    }

    @NotNull
    @EventHandler
    public void getBotOffline(@NotNull BotReloginEvent event) throws Exception {
        logger.error("重新登录");
    }

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        // 处理事件处理时抛出的异常
        logger.error("异常" + context + "\n原因:" + exception.toString());
        throw new RuntimeException("在事件处理中发生异常", exception);
    }

}
