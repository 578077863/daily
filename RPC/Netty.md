# Netty



## 逻辑架构

![image-20220111211736050](images/Netty/image-20220111211736050.png)

















































# 收纳箱



![image-20211226144834537](images/Netty/image-20211226144834537.png)

![image-20211226160050426](images/Netty/image-20211226160050426.png)





## 琐碎

### Netty Promise

可以获取结果和异常，如果get到异常没有做处理，那就会交给JVM默认方法处理该异常







## 前置知识

### 什么是 **IO** 和 **NIO**





## Netty组件

### EventLoop

### Channel

### Future与Promise

### Handler与Pipeline

### ByteBuf





## 水平和边缘触发





# 笔记

### Channel状态

![image-20220128175238102](images/Netty/image-20220128175238102.png)





### 事件调度层

![image-20220128175348494](images/Netty/image-20220128175348494.png)



### EventLoop 与 Channel关系

![image-20220128175147061](images/Netty/image-20220128175147061.png)

EventLoopGroup是Netty Reactor线程模型的具体实现方式

 

3种Reactor线程模型

![image-20220120105518577](images/Netty/image-20220120105518577.png)





服务编排层

![image-20220120105728883](images/Netty/image-20220120105728883.png)







### 服务编排层

#### ChannelPipeline

![image-20220122224039223](images/Netty/image-20220122224039223.png)



![image-20220122224132065](images/Netty/image-20220122224132065.png)



![image-20220122224409914](images/Netty/image-20220122224409914.png)





![image-20220122224525491](images/Netty/image-20220122224525491.png)

![image-20220122230015057](images/Netty/image-20220122230015057.png)

![image-20220122230148221](images/Netty/image-20220122230148221.png)













线程安全



### 组件关系梳理

![image-20220122230224001](images/Netty/image-20220122230224001.png)

Boss负责监听网络连接事件，当有新的网络连接事件到达时，就channel注册到Worker EventLoopGroup，然后woker EventLoopGroup会被分配一个EventLoop负责处理该channel读写事件 ，每个EventLoop都是单线程的，由selector进行事件循环。当客户端发起IO事件读写时，服务端EventLoop会进行事件的读取，然后通过PipeLine触发各种监听器进行数据的加工处理，客户端的数据会被传递到ChannelHandler的第一个ChannelInboundHandler中





### 单线程模型

![image-20220128192827429](images/Netty/image-20220128192827429.png)



![image-20220128193032122](images/Netty/image-20220128193032122.png)



### 多线程模型

![image-20220122223504315](images/Netty/image-20220122223504315.png)





### 主从多线程模型

![image-20220122223606860](images/Netty/image-20220122223606860.png)



### 功能模块

![image-20220122232113468](images/Netty/image-20220122232113468.png)

![image-20220122232205668](images/Netty/image-20220122232205668.png)

![image-20220122232305509](images/Netty/image-20220122232305509.png)

![image-20220122232402823](images/Netty/image-20220122232402823.png)





### EventLoop是什么

![image-20220128193114773](images/Netty/image-20220128193114773.png)

![image-20220128193145558](images/Netty/image-20220128193145558.png)

![image-20220128193209919](images/Netty/image-20220128193209919.png)



### 事件处理机制

![image-20220128193428334](images/Netty/image-20220128193428334.png)

![image-20220128193459020](images/Netty/image-20220128193459020.png)

**EventLoop和channel pipeline也是线程安全的**





![image-20220128193618988](images/Netty/image-20220128193618988.png)





![image-20220128193757580](images/Netty/image-20220128193757580.png)







### EventLoop最佳实践

![image-20220128194132203](images/Netty/image-20220128194132203.png)



### ChannelPipeline

![image-20220128194407178](images/Netty/image-20220128194407178.png)

![image-20220128194501866](images/Netty/image-20220128194501866.png)

![image-20220128194633630](images/Netty/image-20220128194633630.png)



### 异常传播机制

若InboundHandler出现异常并没有拦截,则最后由tail节点的handler来处理异常

所以推荐在tail前一个节点添加一个Handler来处理异常,也就是channel pipeline的末端

![image-20220128201430406](images/Netty/image-20220128201430406.png)

![image-20220128201445183](images/Netty/image-20220128201445183.png)



### 为什么有拆包/粘包

![image-20220128202714139](images/Netty/image-20220128202714139.png)

![image-20220128213211611](images/Netty/image-20220128213211611.png)

![image-20220128213317276](images/Netty/image-20220128213317276.png)









消息定长

特定分隔符

消息长度 + 消息内容



### 通信协议设计

![image-20220128213623529](images/Netty/image-20220128213623529.png)

![image-20220128213733386](images/Netty/image-20220128213733386.png)

![image-20220128213805029](images/Netty/image-20220128213805029.png)

![image-20220128213837437](images/Netty/image-20220128213837437.png)

![image-20220128213847771](images/Netty/image-20220128213847771.png)



![image-20220128213855626](images/Netty/image-20220128213855626.png)

![image-20220128213908136](images/Netty/image-20220128213908136.png)

![image-20220128213923856](images/Netty/image-20220128213923856.png)



![image-20220128213933507](images/Netty/image-20220128213933507.png)



![image-20220128213941669](images/Netty/image-20220128213941669.png)





### Netty如何实现自定义通信协议

![image-20220128214057967](images/Netty/image-20220128214057967.png)

![image-20220128214217083](images/Netty/image-20220128214217083.png)

![image-20220128214239868](images/Netty/image-20220128214239868.png)

![image-20220128214625956](images/Netty/image-20220128214625956.png)

![image-20220128215328109](images/Netty/image-20220128215328109.png)

![image-20220128215434890](images/Netty/image-20220128215434890.png)

![image-20220128215456172](images/Netty/image-20220128215456172.png)



![image-20220128215511248](images/Netty/image-20220128215511248.png)







### Pipeline 事件传播回顾

![image-20220129231400846](images/Netty/image-20220129231400846.png)
