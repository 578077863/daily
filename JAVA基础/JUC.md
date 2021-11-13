# JUC

线程1先将对象的mark复制到自身的锁记录,再将对象的mark word设置成线程的锁记录地址

lamada表达式在接口上加注解@FunctionalInterface,该接口的抽象方法只能有一个

slf4j:

log.debug("{}",task.get())//{}表示占位符,值为后面的get



**两阶段终止模式**



## 查看进程线程的方法

ps -fe 查看所有进程

> ps -fe | grep java

ps -fT -p \<PID\> 查看某个进程(PID)的所有线程

kill 杀死进程

top 按大写H切换是否显示线程

top -H -p \<PID\> 查看某个进程(PID)的所有线程





jps 命令查看所有Java进程

jstack \<PID\> 查看某个Java进程(PID)的所有线程状态

jconsole 查看某个Java进程中线程的运行情况(图形界面)



## 线程上下文切换(Thread Context Switch)

1. 线程的cpu时间片用完
2. 垃圾回收
3. 有更高优先级的线程需要运行
4. 线程自己调用了 sleep , yield , wait , join , park , synchronized , lock等方法



Context Switch发生时,需要由操作系统保存当前线程的状态,并恢复另一个线程的状态,Java中对应的概念就是程序计数器,其作用是记录了下一条JVM指令的执行地址

* 状态包括程序计数器、虚拟机栈中每个栈帧的信息，如局部变量、操作数栈、返回地址等
* Context Switch 频繁发生会影响性能



