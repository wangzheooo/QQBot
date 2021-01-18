# QQ群机器人

#### 介绍
QQ群机器人,基于mirai,一开始的目的是为了每天在群里看每日新闻简讯

#### 软件架构
springboot 2.4.1
okhttp
jsoup
redis


#### 参与贡献

感谢mirai提供的开源项目

网址:https://github.com/mamoe/mirai

感谢proxypool提供的开源项目,和搭载的成品网站

git网址:https://github.com/sansui233/proxypool

网站:https://proxypoolsstest.herokuapp.com/

#### 初衷

前言:目前20210118,还能用

初衷是因为看到telegram上有个频道每天推新闻简讯,我想把这个简讯用QQ群机器人转发到我的宿舍群,
仅此而已,其他功能都是鸡肋.

#### 项目介绍

这个项目分两部分,一部分是QQ群机器人,一部分是爬虫

机器人部分用了开源的mirai,文档非常详细,我只用了QQ群监听,还有消息发送两个功能

爬虫用了okhttp,最后选这个okhttp是因为支持sock5代理,毕竟telegram需要代理才行,我在服务器上
运行了clash for windows,加上免费的配置文件,可实现免费代理,获取的网站用jsoup解析

#### 主要功能

每天8:00在群里发每日简讯

可以推荐随机午餐

可以发送天气信息

可以年龄计算