package com.example.wizardbot.handle;

import com.example.wizardbot.contants.Global;
import com.example.wizardbot.service.BotService;
import com.example.wizardbot.utils.Tool;
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
        String fromNick=event.getMessage();
        if(fromNick.equals("1233211234567")){
            event.accept();
            logger.info("添加好友"+event.getFromId());
        }else {
            logger.info("添加好友失败,暗号没对上");
        }
        return ListeningStatus.LISTENING;
    }

    @EventHandler()
    public ListeningStatus onBotInvitedJoinGroupRequest(BotInvitedJoinGroupRequestEvent event) {
        event.accept();
        logger.info("被拉入群"+event.getGroupId());
        return ListeningStatus.LISTENING;
    }

    @EventHandler()
    public ListeningStatus onBotJoinGroup(BotJoinGroupEvent event) {
        event.getGroup().sendMessage(new PlainText(global.getMenu()));
        logger.info("首次进群功能介绍"+event.getGroup().getId());
        return ListeningStatus.LISTENING;
    }

    @EventHandler()
    public ListeningStatus onFriendMessage(FriendMessageEvent event) {
        event.getFriend().sendMessage(new PlainText("只支持群聊"));
        logger.info("收到好友消息"+event.getFriend().getId());
        return ListeningStatus.LISTENING;
    }

    @NotNull
    @EventHandler
    public void getGroupMessage(@NotNull GroupMessageEvent event) throws Exception {
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

        if (messageTemp.equals("开启每日简讯推送")) {
            Long groupId = event.getSubject().getId();
            if (botService.addGroup(groupId)) {
                event.getSubject().sendMessage(new PlainText("每日简讯推送开启成功!"));
            } else {
                event.getSubject().sendMessage(new PlainText("已经开启了,不要重复开启!"));
            }
        } else if (messageTemp.equals("关闭每日简讯推送")) {
            Long groupId = event.getSubject().getId();
            if (botService.delGroup(groupId)) {
                event.getSubject().sendMessage(new PlainText("每日简讯推送已关闭"));
            } else {
                event.getSubject().sendMessage(new PlainText("不存在的群号"));
            }
        } else if (messageTemp.equals("石原里美你好")) {
            event.getSubject().sendMessage(new PlainText(event.getSenderName() + "你好呀"));
        } else if (messageTemp.equals("新闻")) {
            String news = botService.getNews();
            if (news.indexOf("bot000000") == -1) {
                botService.sendGroupImage("" + event.getGroup().getId(), news);
            } else {
                event.getSubject().sendMessage(new PlainText(news));
            }
        } else if (messageTemp.equals("吃饭推荐")) {
            event.getSubject().sendMessage(new PlainText(global.getFoods()[Tool.get_random(0, global.getFoods().length)]));
        } else if (messageTemp.equals("石原里美功能介绍")) {
            event.getSubject().sendMessage(new PlainText(global.getMenu()));
        } else if (messageTemp.indexOf("生存天数") != -1) {
            //例子:生存天数19951111
            if (messageTemp.length() == 12) {
                event.getSubject().sendMessage(new PlainText(botService.getSurvivalDays(messageTemp)));
            }
        } else if (messageTemp.indexOf("天气") != -1) {
            if (messageTemp.length() >= 4 && messageTemp.length() < 10) {
                String[] weatherContent = messageTemp.split("天气");
                try {
                    event.getSubject().sendMessage(new PlainText(botService.weather("" + weatherContent[0])));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @NotNull
    @EventHandler
    public void getBotOffline(@NotNull BotOfflineEvent event) throws Exception {
        logger.error("我掉了");
    }

    @NotNull
    @EventHandler
    public void getBotOffline(@NotNull BotReloginEvent event) throws Exception {
        logger.error("我重新登录");
    }

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        // 处理事件处理时抛出的异常
        logger.error("异常" + context + "\n原因:" + exception.toString());
        throw new RuntimeException("在事件处理中发生异常", exception);
    }

}