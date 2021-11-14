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





### 线程在操作系统的5种状态和Java中的6种状态





## 共享模型之管程

临界区线程安全问题:

阻塞式解决方案: synchronized(对象锁) ,Lock

非阻塞式: 原子变量





### synchronized

实际用对象锁保证了临界区内代码的原子性,尝试获取锁才会阻塞,如果一个线程使用synchronized,一个不用,那等于没用,因为不获取锁就不会阻塞

1. 加载公共对象上

2. 加载成员方法上

   ```java
   public class Demo {
   	//在方法上加上synchronized关键字
   	public synchronized void test() {
   	
   	}
   	//等价于
   	public void test() {
   		synchronized(this) {
   		
   		}
   	}
   }
   ```

3. 加载静态方法上

   ```java
   public class Demo {
   	//在静态方法上加上synchronized关键字
   	public synchronized static void test() {
   	
   	}
   	//等价于
   	public void test() {
   		synchronized(Demo.class) {
   		
   		}
   	}
   }
   ```

   



### 变量的线程安全分析

成员变量和静态变量是否线程安全?

若没共享,则安全

若被共享,根据其状态是否能够改变? 只读:安全 ; 读写:则是临界区,需要考虑线程安全





常见线程安全类

- String
- Integer
- StringBuﬀer
- Random
- Vector （List的线程安全实现类）
- Hashtable （Hash的线程安全实现类）
- java.util.concurrent 包下的类



这里说它们是线程安全的是指，多个线程调用它们**同一个实例的某个方法时**，是线程安全的(多个线程同时调用 hashtable.put,不会被线程上下文切换干扰)

- 它们的每个方法是原子的（都被加上了synchronized）
- 但注意它们**多个方法的组合不是原子的**，所以可能会出现线程安全问题





外星方法



## Java对象头

32位虚拟机:

普通对象:

Object Header(64 bit): Mark Word (32 bit)   Klass Word(32 bit)





数组对象:

96 bit , 因为最后多出 array length(32 bit)



Mark Word结构



## Minitor

监视器 或 管程



synchronized 字节码层面是 首先拿到引用地址,复制一份存储到一个临时变量(slot 1) , 将 lock对象的 MarkWord 置为 Monitor指针

操作完后 aload_1 拿到lock引用 , monitorexit 将lock对象的MarkWord重置,唤醒EntryList



如果在同步代码块出现异常, 也是会释放锁的(拿到引用地址,重置,唤醒,抛出异常)



## 轻量级锁

创建锁记录(Lock Record) 对象 , 每个线程的栈帧都会包含一个锁记录的结构,内部可以存储锁定对象的Mark Word

![image-20211114174217419](images/JUC/image-20211114174217419.png)

![image-20211114174617695](images/JUC/image-20211114174617695.png)

![image-20211114174657339](images/JUC/image-20211114174657339.png)



轻量级锁在没有竞争时(也就是当前只有自己一个线程),每次重入都需要执行CAS操作,JDK6引入了偏向锁来做优化 : 只有第一次使用CAS将线程ID设置到对象的Mark Word头, 之后发现这个线程ID是自己的就表示没有竞争,不用重新CAS.以后只要不发生竞争,这个对象就归该线程所有



调用一个对象的hashcode会撤销掉该对象的偏向锁,但其他锁没影响,因为有存储锁信息的地方(线程栈帧的锁记录里,Monitor对象里)



### 批量重偏向

当撤销超过20次后（超过阈值），JVM会觉得是不是偏向错了，这时会在给对象加锁时，重新偏向至加锁线程



### 批量撤销

当撤销偏向锁的阈值超过40以后，就会将**整个类的对象都改为不可偏向的**

### 锁膨胀

尝试加轻量级锁过程中,CAS操作无法成功,需要进行锁膨胀,将轻量级锁变为重量级锁

![image-20211114175849415](images/JUC/image-20211114175849415.png)

![image-20211114180015604](images/JUC/image-20211114180015604.png)



Monitor获取失败到陷入阻塞中间有自旋优化,如果成功则避免阻塞

自旋会占用cpu,得多核才有优势





## wait sleep

sleep(long n) 和 wait(long n)的状态都是 TIMED_WAITING,不带参数的wait进入的是 WAITING状态
