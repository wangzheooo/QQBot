package com.example.wizardbot.handle;

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
        event.accept();
        logger.info("被拉入群" + event.getGroupId());
        return ListeningStatus.LISTENING;
    }

    @EventHandler()
    public ListeningStatus onBotJoinGroup(BotJoinGroupEvent event) {
        event.getGroup().sendMessage(new PlainText(global.getMenu()));
        logger.info("首次进群功能介绍" + event.getGroup().getId());
        return ListeningStatus.LISTENING;
    }

    @EventHandler()
    public ListeningStatus onFriendMessage(FriendMessageEvent event) {
        event.getFriend().sendMessage(new PlainText("只支持群聊"));
        logger.info("收到好友消息" + event.getFriend().getId());
        return ListeningStatus.LISTENING;
    }

    @NotNull
    @EventHandler
    public ListeningStatus getGroupMessage(@NotNull GroupMessageEvent event) throws Exception {
        MessageChain message = event.getMessage();
        String messageTemp = "";

        //因为只要文字信息,所以接收的群信息里,除了文字都过滤掉了
        for (int i = 0; i < message.size(); i++) {
            if (("" + message.get(i)).indexOf("[mirai:") == -1) {
                messageTemp += ("" + message.get(i)).replace("\r", " ").trim();
            }
        }
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
        } else if (messageTemp.equals(".功能介绍")) {
            event.getSubject().sendMessage(new PlainText(global.getMenu()));
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
        } else if (messageTemp.startsWith(".快餐推荐")) {
            String finalMessageTemp = messageTemp;
            String[] str = finalMessageTemp.split(".快餐推荐");
            if (str.length > 0) {
                global.getExecutor().execute(() -> botService.getKuaiCan(event.getSubject().getId(),event.getSender().getId(), str[1]));
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
