## 1. 为什么需要并发
通过并发编程的形式可以充分发挥多核CPU的性能，减少cpu空闲时间，提高程序响应速度

## 2. 并发编程有哪些缺点
### 2.1 上下文切换开销

什么情况会发生线程上下文切换：
 1. 线程的cpu时间片用完
 2. 垃圾回收
 3. 有更高优先级的线程需要运行
 4. 线程自己调用了 sleep , yield , wait , join , park , synchronized , lock等方法

Context Switch发生时,需要由操作系统保存当前线程的状态,并恢复另一个线程的状态,Java中对应的概念就是程序计数器,其作用是记录了下一条JVM指令的执行地址
* 状态包括程序计数器、虚拟机栈中每个栈帧的信息，如局部变量、操作数栈、返回地址等
* Context Switch 频繁发生会影响性能


减少上下文切换的思路：
	1. 无锁并发编程，可以参照concurrentHashMap锁分段的思想，不同的线程处理不同段的数据，这样在多线程竞争的条件下，可以减少上下文切换的时间。
	2.CAS算法，利用Atomic下使用CAS算法来更新数据，使用了乐观锁，可以有效的减少一部分不必要的锁竞争带来的上下文切换
	3.使用最少线程：避免创建不需要的线程，比如任务很少，但是创建了很多的线程，这样会造成大量的线程都处于等待状态
	4.协程：在单线程里实现多任务的调度，并在单线程里维持多个任务间的切换
### 2.2 线程安全
什么是线程安全
	多个线程同一时刻对同一份资源做写操作时（读操作不会涉及到线程安全），如果结果和我们预期的一样，则是线程安全
	eg：抢火车票
### 2.3 死锁

由死锁联想到 redis分布式锁
### 2.4 增加资源消耗
线程在运行的时候需要从计算机里面得到一些资源。除了CPU，线程还需要一些内存来维持它本地的堆栈。它也需要占用操作系统中一些资源来管理线程
### 2.5 设计更加复杂
[[MySQL#隔离级别]]
MySQL隔离级别就是为了解决线程并发带来的问题所设计的

## 3. 基础知识
### 3.1 同步与异步
同步和异步通常用来形容一次方法调用。同步方法调用一开始，调用者必须等待被调用的方法结束后，调用者后面的代码才能执行，比如，主线程中突然另外一个线程加入，t.join(),主线程被挂起，只有t线程完成后才能继续前行。而异步调用，指的是，调用者不用管被调用方法是否完成，都会继续执行后面的代码，当被调用的方法完成后会通知调用者

### 3.2 并发与并行
并发和并行是十分容易混淆的概念。并发指的是多个任务交替进行，而并行则是指真正意义上的“同时进行”。实际上，如果系统内只有一个CPU，而使用多线程时，那么真实系统环境下不能并行，只能通过切换时间片的方式交替进行，而成为并发执行任务。真正的并行也只能出现在拥有多个CPU的系统中。

### 3.3 阻塞和非阻塞
阻塞和非阻塞通常用来形容多线程间的相互影响，比如一个线程占有了临界区资源，那么其他线程需要这个资源就必须进行等待该资源的释放，会导致等待的线程挂起，这种情况就是阻塞，而非阻塞就恰好相反，它强调没有一个线程可以阻塞其他线程，所有的线程都会尝试地往前运行

### 3.4 临界区
临界区用来表示一种公共资源或者说是共享数据，可以被多个线程使用。但是每个线程使用时，一旦临界区资源被一个线程占有，那么其他线程必须等待。

### 3.5 进程 & 线程


1. 联系：
  线程是进程中的一部分，一个进程可以有多个线程，但线程只能存在于一个进程中。

2.区别：

 根本区别：进程是操作系统资源调度的基本单位，线程是任务的调度执行的基本单位

 开销方面：进程都有自己的独立数据空间，程序之间的切换开销大；线程也有自己的运行栈和程序计数器，线程间的切换开销较小，更加轻量。

 比如说，一个web服务器，在接受一个新的请求的时候，可以大动干戈的fork一个子进程去处理这个请求，也可以只在进程内部创建一个新的线程来处理。线程更加轻便一点。线程可以有很多，但他们并不会改变进程对内存（heap）等资源的管理，线程之间会共享这些资源。

 共享空间：进程拥有各自独立的地址空间、资源，所以共享复杂，需要用IPC（Inter-Process Communication，进程间通信），但是同步简单。而线程共享所属进程的资源，因此共享简单，但是同步复杂，需要用加锁等措施。

  
进程和线程的主要差别在于**它们是不同的操作系统资源管理方式**。进程有独立的地址空间，一个进程崩溃后，在保护模式下不会对其它进程产生影响，**而线程只是一个进程中的不同执行路径**。线程有自己的堆栈和局部变量，但**线程之间没有单独的地址空间**，一个线程死掉就等于整个进程死掉，所以多进程的程序要比多线程的程序健壮，但在进程切换时，耗费资源较大，效率要差一些。但对于一些要求同时进行并且又要共享某些变量的并发操作，只能用线程，不能用进程。

联想：[[#4 java线程]]

[[操作系统#进程和线程]]


## 4. java线程
Java的线程是映射到操作系统的原生线程之上的，如果要阻塞或唤醒一条线程，都需要操作系统来帮忙完成，这就需要从用户态转换到核心态中，因此状态转换需要耗费很多的处理器时间。所以synchronized是Java语言中的一个重量级操作。在JDK1.6中，虚拟机进行了一些优化，譬如在通知操作系统阻塞线程之前加入一段自旋等待过程，避免频繁地切入到核心态中

### 4.1 线程状态及状态转变方式
- 线程状态
	1.  NEW –> RUNNABLE
	    
	    -   当调用了t.start()方法时，由 NEW –> RUNNABLE
	        
	2.  RUNNABLE <–> WAITING
	    
	    -   当调用了t 线程用 synchronized(obj) 获取了对象锁后
	        
	        -   调用 obj.wait() 方法时，t 线程从 RUNNABLE –> WAITING
	            
	        -   调用 obj.notify() ， obj.notifyAll() ， t.interrupt() 时
	            
	            -   竞争锁成功，t 线程从 WAITING –> RUNNABLE
	                
	            -   竞争锁失败，t 线程从 WAITING –> BLOCKED
	                
	3.  RUNNABLE <–> WAITING
	    
	    -   当前线程
	        
	        调用 t.join() 方法时，当前线程从 RUNNABLE –> WAITING
	        
	        -   注意是**当前线程**在t 线程对象的监视器上等待
	            
	    -   t 线程**运行结束**，或调用了**当前线程**的 interrupt() 时，当前线程从 WAITING –> RUNNABLE
	        
	4.  RUNNABLE <–> WAITING
	    
	    -   当前线程调用 LockSupport.park() 方法会让当前线程从 RUNNABLE –> WAITING
	        
	    -   调用 LockSupport.unpark(目标线程) 或调用了线程 的 interrupt() ，会让目标线程从 WAITING –> RUNNABLE
	        
	5.  RUNNABLE <–> TIMED_WAITING
	    
	    t 线程用 synchronized(obj) 获取了对象锁后
	    
	    -   调用 obj.wait(**long n**) 方法时，t 线程从 RUNNABLE –> TIMED_WAITING
	        
	    -   t 线程等待时间超过了 n 毫秒，或调用 obj.notify() ， obj.notifyAll() ， t.interrupt() 时
	        
	        -   竞争锁成功，t 线程从 TIMED_WAITING –> RUNNABLE
	            
	        -   竞争锁失败，t 线程从 TIMED_WAITING –> BLOCKED
	            
	6.  RUNNABLE <–> TIMED_WAITING
	    
	    -   当前线程调用 t.join
	        
	        (long n) 方法时，当前线程从 RUNNABLE –> TIMED_WAITING
	        
	        -   注意是当前线程在t 线程对象的监视器上等待
	            
	    -   当前线程等待时间超过了 n 毫秒，或t 线程运行结束，或调用了当前线程的 interrupt() 时，当前线程从 TIMED_WAITING –> RUNNABLE
	        
	7.  RUNNABLE <–> TIMED_WAITING
	    
	    -   当前线程调用 Thread.sleep(long n) ，当前线程从 RUNNABLE –> TIMED_WAITING
	        
	    -   当前线程等待时间超过了 n 毫秒，当前线程从 TIMED_WAITING –> RUNNABLE
	        
	8.  RUNNABLE <–> TIMED_WAITING
	    
	    -   当前线程调用 LockSupport.parkNanos(long nanos) 或 LockSupport.parkUntil(long millis) 时，当前线 程从 RUNNABLE –> TIMED_WAITING
	        
	    -   调用 LockSupport.unpark(目标线程) 或调用了线程 的 interrupt() ，或是等待超时，会让目标线程从 TIMED_WAITING–> RUNNABLE
	        
	9.  RUNNABLE <–> BLOCKED
	    
	    -   t 线程用 synchronized(obj) 获取了对象锁时如果**竞争失败**，从 RUNNABLE –> BLOCKED
	        
	    -   持 obj 锁线程的同步代码块执行完毕，会唤醒该对象上所有 BLOCKED 的线程重新竞争，如果其中 t 线程竞争 成功，从 BLOCKED –> RUNNABLE ，其它**失败**的线程仍然 BLOCKED
	        
	10.  RUNNABLE <–> TERMINATED
		* 当前线**程所有代码运行完毕**，进入 TERMINATED
![[Pasted image 20220211120101.png]]

操作系统中的 Ready 和 Running 状态 合起来对应 java Runnable状态

为什么要合起来

因为 主流JVM的底层系统调度全交给操作系统,所以jvm启动线程后进入runnable,这时候线程处于操作系统调度中,可以处于运行中（内核态）或者阻塞（挂起到用户态），所以jvm并不知道它启动以后的线程是处于操作系统阻塞还是操作系统运行中，因此笼统得称之为runnable。
现在的时分多任务操作系统架构 通常都是用 时间片轮转的方式进行抢占式调度，通常 Java的线程状态是服务于监控的,由于线程的上下文切换速度对于人来说非常快,那么区分ready 与 running 就没什么意义

### 4.2 线程使用方式
1. 继承Thread类
 通过继承Thread类重写 run方法
2. 实现Runnable接口
 通过实现Runnable接口中的run方法，然后将实现该接口的类的实例作为参数传给Thread的实例，由Thread的实例调用start方法	
3. 实现Callable接口
 实现Callable接口，返回值通过FutureTask进行封装

 
**Java 线程调用 start->start0 这个本地方法，实际上会调用到 JVM_StartThread 方法，而 JVM_StartThread方法中会创建与平台相关的本地线程，该线程执行 Java 线程的 run 方法。**
**创建线程只有一种方式就是构造Thread类,而实现线程的执行单元有两种方式**

实现接口和继承Thread比较
实现接口会更好一些，因为:
-   从共享资源来看，Runnable接口更适合（因为都是同一个Runnable实例）
-   从继承角度看，Runnable更适合，因为接口可以多继承，类只能单继承，这样不利于扩展


### 4.3 基础线程机制
#### Executor
通常是管理多个互不干扰，不需要进行同步操作的任务的执行，无需程序员显示地管理线程地生命周期
主要的Executor：


手写Executor：


#### Daemon
守护线程是程序运行时在后台提供服务的线程，不属于程序中不可或缺的部分。

当所有非守护线程结束时，程序也就终止，同时会杀死所有守护线程。

main() 属于非守护线程。

在线程启动之前使用 setDaemon() 方法可以将一个线程设置为守护线程。


#### sleep
Thread.sleep(millisec) 方法会休眠当前正在执行的线程，millisec 单位为毫秒。

sleep() 可能会抛出 InterruptedException，因为异常不能跨线程传播回 main() 中，因此必须在本地进行处理。线程中抛出的其它异常也同样需要在本地进行处理。

#### yield
对静态方法 Thread.yield() 的调用声明了当前线程已经完成了生命周期中最重要的部分，可以切换给其它线程来执行。该方法只是对线程调度器的一个建议，而且也只是建议具有相同优先级的其它线程可以运行。


### 4.5 synchronized及锁升级
jdk1.6的优化是：
1. 锁升级机制
2. 锁消除。一些框架采用保守策略，将程序基于[线程安全](https://so.csdn.net/so/search?q=%E7%BA%BF%E7%A8%8B%E5%AE%89%E5%85%A8&spm=1001.2101.3001.7020)实现，锁消除是一种编译器优化，通过逃逸分析消除部分无必要的同步代码。
3. 锁粗化。在编译期间将相邻的同步代码块合并成一个大的同步代码块，减少反复申请、释放造成的开销。（即使每次都可以获得锁，那么频繁的操作底层同步队列也将造成不必要的消耗）
4. 自适应自旋锁。synchronizedCAS占用owner失败后，会进行自旋尝试，这个时间不是固定的，而是**前一次在同一个锁上的自旋时间以及锁的拥有者的状态来决定的**

同时，用户也可以具有一些优化意识，如：
锁分离。最常见的就是读写分离。
减少不必要的同步代码、减少同步代码大小，减少锁的粒度（例如jdk1.8concurrentHashMap基于synchronized实现分段加锁，将粒度压缩都了每一个桶）、尽量让同步代码短小精悍，减少锁的持有时间。


锁的状态取决于对象头的mark word低两位。

锁升级是单向的（也不一定，和JVM的实现有关）：

创建锁记录(Lock Record) 对象 , 每个线程的栈帧都会包含一个锁记录的结构,内部可以存储锁定对象的Mark Word

Synchronized经过编译，会在同步块的前后分别形成monitorenter和monitorexit这个两个字节码指令。在执行monitorenter指令时，首先要尝试获取对象锁。如果这个对象没被锁定，或者当前线程已经拥有了那个对象锁，把锁的计算器加1，相应的，在执行monitorexit指令时会将锁计算器就减1，当计算器为0时，锁就被释放了。如果获取对象锁失败，那当前线程就要阻塞，直到对象锁被另一个线程释放为止。

#### 特点:

1.  原子性
    
    > 线程操作同步块代码时，是**原子**的（即使OS层面存在线程切换，但是java层面我们将线程访问共享变量的整套同步代码的操作看作是原子的
    
2.  可见性
    
    > 同步块具有**可见性**，线程**写共享同步块内的共享变量**，会使得其他线程保存该共享变量的对应缓存行**失效**，读共享变量则会重新从主存中去读取。
    
3.  有序性
    
    > synchronized块内的代码不会被重排序到synchronized块外。（synchronized同步块可以看作单线程，遵循as-if-serial，会进行重排序优化）

#### 锁升级过程中mark word存储的变化：

当对象状态为**偏向锁**时，mark word存储的是**偏向的线程ID**，当状态为**轻量级锁**的时候，存储的是**指向线程栈中 lock record(锁记录对象) 的指针**，当状态为**重量级锁**的时候，**指向堆中monitor对象的指针**。

线程在进入同步块之前，JVM会在当前线程的栈帧中创建一个**锁记录 lock record**（不同的锁类型对lock record具有不同的处理，偏向锁中lock record是存在的,但存储对象的mark word 为 null，所以偏向锁调用锁对象的hashcode会撤销偏向锁状态，因为lock record没有创建所以mark word和偏向锁有关的信息就丢失了，而轻量级锁和重量级锁中保存了lock record的地址），这个结构用于保存对象头mark word初始结构的复制，称为**displaced mark word**

其中displaced mark word用于保存对象mark word未锁定状态下的结构（用于替换——**因为mark word的结构依据锁的状态不同动态变化着，因此必须有一个结构用于保存mark word的原始状态**，这个结构就是保存在线程栈帧中的displaced mark word）。

#### Monitor
[[JVM#Monitor]]
#### 偏向锁

偏向锁——一段同步代码总是被一个线程所访问（不存在另外一个线程），那么该线程会自动获取锁，降低获取锁的代价。（单线程环境下都是偏向锁） 偏向锁在一个线程第一次访问的时候将该线程的id记录下来，下次判断如果还是该线程就不会加锁了。如果有另一个线程也来访问它，说明有可能出现线程并发。此时偏向锁就会升级为轻量级锁。

偏向锁的目的——在某个线程获得锁之后，消除这个线程重入（CAS）的开销，看起来让这个线程得到了偏向。 偏向锁只需要在设置thread ID时进行一次CAS操作，后续发生重入时仅仅进行简单的thread id检查，**并且向线程栈帧中添加一个空的lock record表示重入**，不需要CAS指令。（偏向锁一旦被某个线程获得，除非出现竞争导致撤销，否则线程不会主动释放锁即thread id只能被设定一次）

如果在运行过程中，遇到了其他线程抢占锁，则持有偏向锁的线程会被挂起（走到安全点后stop the world），JVM会消除它身上的偏向锁，将锁恢复到标准的轻量级锁。

偏向锁是对单线程场景下的优化，例如消除第三方框架同步代码带来的性能损失

##### 批量重偏向
当撤销超过20次后（超过阈值），JVM会觉得是不是偏向错了，这时会在给对象加锁时，重新偏向至加锁线程

##### 批量撤销
当撤销偏向锁的阈值超过40以后，就会将**整个类的对象都改为不可偏向的**

#### 轻量级锁

使用场景：如果一个对象虽然有多线程要加锁，但加锁的时间是错开的（也就是没有竞争），那么可以 使用轻量级锁来优化

线程试图占用轻量级锁时，必须使用CAS指令，这是相对于偏向锁提升的开销。轻量级锁在对象头的mark word体现，就是一个指向lock record的指针（偏向锁则是thread id）。 线程monitorenter时，栈帧中创建一个锁记录结构，将锁记录的Object reference 指向锁对象，然后将对象的mark word的值存入到锁记录中，**然后使用CAS试图修改对象mark word的值为lock record地址值和状态**，成功则代表成功获取锁，失败则要么存在重入，或者存在竞争并通知JVM执行锁升级

轻量级锁适用于线程交替执行同步块的情况，如果存在同一时间访问同一锁即冲突访问的情况，就会导致轻量级锁膨胀为重量级锁。在线程总是能交替执行的场景（并发量小、同步代码执行快速），可以防止monitor对象的创建。

**轻量级锁是为了在线程交替执行同步块时提高性能，而偏向锁则是在只有一个线程执行同步块时进一步提高性能**

如果CAS替换成功,对象头中存储了锁记录地址和状态00,表示由线程给对象加锁

如果CAS失败,**如果发现是其他进程来竞争,首先会进行自旋锁,自旋一定次数后还是失败就进入锁膨胀过程,** 如果发现锁的持有者是自己,则再添加一条Lock Record作为重入的计数,退出synchronized时,如果有取值的null的锁记录,则重置锁记录,表示重入数减1,若锁记录的值不为null,则使用cas将Mark Work的值恢复给对象头(就是替换掉对象头的值),替换成功,解锁,替换失败,说明轻量级锁已经进行了锁膨胀或已经升级到重量级锁,进入重量级锁的解锁流程

调用一个对象的hashcode会撤销掉该对象的偏向锁,但其他锁没影响,因为有存储锁信息的地方(线程栈帧的锁记录里,Monitor对象里)

轻量级锁在没有竞争时(也就是当前只有自己一个线程),每次重入都需要执行CAS操作,JDK6引入了偏向锁来做优化 : 只有第一次使用CAS将线程ID设置到对象的Mark Word头, 之后发现这个线程ID是自己的就表示没有竞争,不用重新CAS.以后只要不发生竞争,这个对象就归该线程所有

##### 流程图片

![image-20211114174217419](file://D:\java-daily\JAVA%E5%9F%BA%E7%A1%80\images\JUC\image-20211114174217419.png?lastModify=1644675866)

![image-20211114174617695](file://D:\java-daily\JAVA%E5%9F%BA%E7%A1%80\images\JUC\image-20211114174617695.png?lastModify=1644675866)

![image-20211114174657339](file://D:\java-daily\JAVA%E5%9F%BA%E7%A1%80\images\JUC\image-20211114174657339.png?lastModify=1644675866)




#### 重量级锁


一个线程进入synchronized后便进行**一次CAS**（CAS（owner,null,cur)试图让自己称为owner），没错，这里强调的就是一次。如果第一次CAS失败则说明抢占失败，通常会进行**自适应自旋（重试）**，如果仍然失败则进入entryList同步队列，并且调用park()阻塞当前线程，底层对应系统调用**将当前线程对象映射到的操作系统线程挂起，并让出CPU**，这一步通常代价比较大，因为涉及系统调用和线程切换。如果成功将owner修改为自己，则开始执行同步代码，并且将count加一。执行完毕将count减一，复位owner，并且唤起entryList阻塞的线程（实现上通常唤醒队头线程，不过如果没抢到还会进入entryList队尾，通常流动性很大，不会出现饥饿）。  
而如果owner线程调用wait，则进入waitSet并阻塞（同样对应park调用），同时让出CPU。只有其他线程调用notify它才会被唤醒，而且唤醒后进入entryList，当owner被复位后，同entryList其他线程进行竞争，当称为owner将从原执行位置继续向下执行。

注意：synchronized阻塞指的通常是synchronized抢占锁失败的行为，即不管互斥锁还是自旋锁指的都是**失败后的处理策略**。

[从jvm源码看synchronized - unbelievableme - 博客园 (cnblogs.com)](https://www.cnblogs.com/kundeg/p/8422557.html)
如果显示调用了hashCode()、notify、wait方法则会导致对象直接升级为重量级锁

一个线程尝试获取对象锁，会先令_owner指向该线程，同时_count自增1，这时候有其他线程尝试获取锁时，会先存入_EntryList集合，并进入阻塞。当前线程执行完成后，令_count自减1，若\_coubt为 0,则\_owner=null

如果线程被调用了wait()等方式，释放锁时，也会令_owner=null，\_count减1，同时将该线程放入_WaitSet集合，等待唤醒。


**自旋优化:重量级锁竞争的时候,使用自旋来优化**

锁膨胀流程:

为Object对象申请Monitor锁,让Object指向重量级锁地址,然后竞争的线程进入Monitor的EntryList,自身状态变成blocked

**当持有对象的线程退出同步块解锁时,使用CAS将Mark Word的值恢复给对象头,失败.进入重量级锁解锁流程,按照Monitor地址找到Monitor对象,设置Owner 为 nul,**
唤醒EntryList中的Blocked线程.

重量级锁之所以重是因为底层依赖OS的mutex互斥量实现，依赖堆中的monitor对象（Hotspot对应objectMonitor实现）。 当然了，如果单线程下，或者不存在“竞争明显”的情况下，没有线程会被挂起，也不会出现进程切换，但是仍然需要为使用的锁对象创建绑定的monitor并且频繁CAS设置owner。用户态与内核态的切换主要是由于park()底层涉及系统调用导致的，如果CPU上下文切换的时间接近同步代码的执行时间，那么就显得效率很低下。

##### 锁膨胀

尝试加轻量级锁过程中,CAS操作无法成功,需要进行锁膨胀,将轻量级锁变为重量级锁

![image-20211114175849415](file://D:\java-daily\JAVA%E5%9F%BA%E7%A1%80\images\JUC\image-20211114175849415.png?lastModify=1644677687)

![image-20211114180015604](file://D:\java-daily\JAVA%E5%9F%BA%E7%A1%80\images\JUC\image-20211114180015604.png?lastModify=1644677687)
Monitor获取失败到陷入阻塞中间有自旋优化,如果成功则避免阻塞

自旋会占用cpu,得多核才有优势


**自旋优化:重量级锁竞争的时候,使用自旋来优化**

锁膨胀流程:

为Object对象申请Monitor锁,让Object指向重量级锁地址,然后竞争的线程进入Monitor的EntryList,自身状态变成blocked

当持有对象的线程退出同步块解锁时,使用CAS将Mark Word的值恢复给对象头,失败.进入重量级锁解锁流程,按照Monitor地址找到Monitor对象,设置Owner 为 nul,

唤醒EntryList中的Blocked线程.

重量级锁之所以重是因为底层依赖OS的mutex互斥量实现，依赖堆中的monitor对象（Hotspot对应objectMonitor实现）。 当然了，如果单线程下，或者不存在“竞争明显”的情况下，没有线程会被挂起，也不会出现进程切换，但是仍然需要为使用的锁对象创建绑定的monitor并且频繁CAS设置owner。**用户态与内核态的切换主要是由于park()底层涉及系统调用导致的，如果CPU上下文切换的时间接近同步代码的执行时间，那么就显得效率很低下。**

Monitor并不是随着对象的创建而创建的，而是通过synchronized告诉JVM，需要为某个java对象关联一个monitor对象。每个线程都存在两个objectMonitor对象列表，分别为free和used。当线程需要ObjectMonitor对象时，首先从线程自身的free表中申请，若存在则使用，若不存在则从global list中分配一批monitor到free中。

##### 重量级锁为什么重
synchronized 底层是利用 monitor 对象，CAS 和 mutex 互斥锁(量)来实现的，内部会有等待队列(cxq 和 EntryList)和条件等待队列(waitSet)来存放相应阻塞的线程。

未竞争到锁的线程存储到等待队列中，获得锁的线程调用 wait 后便存放在条件等待队列中，解锁和 notify 都会唤醒相应队列中的等待线程来争抢锁。

然后由于阻塞和唤醒依赖于底层的操作系统实现，(这里说下JVM线程是1对1关系)系统调用存在用户态与内核态之间的切换，所以有较高的开销，因此称之为重量级锁。

所以又引入了自适应自旋机制，来提高锁的性能。


#### 为什么wait/notify需要被同步块包裹

**从实现的角度**：  
wait和notify依赖对象绑定的锁，只有获取锁的线程才能执行该方法（需要借助monitor关联的waitSet），否则将会抛出IllegalMonitorStateException异常（没有获取monitor）。

当一个线程调用一个对象/monitor的notify（）方法时，调度器会从所有处于该对象/monitor等待队列的线程中取出任意一个线程，将其添加到同步队列中（entry list）。然后在同步队列中的多个线程就会竞争对象的锁，得到锁的线程就可以继续执行。如果等待队列中没有线程，notify（）就不会产生作用（相当于对空队列唤醒）。

> 调用wait（）,唤醒的一般是等待队列首线程，如果notifyAll就是依次唤醒队列所有线程。而entryList一般也是从首节点开始唤醒，而竞争主要是**entryList之外线程与entryList刚醒来线程之间的竞争**。

notifyAll()比notify()更加常用， 因为notify()方法只会唤起一个线程（_你也不知道等待队列首节点对应哪个线程，因此对用户来说似乎是随机唤醒的_）， 且无法指定唤醒哪一个线程，所以只有在多个执行相同任务的线程在并发运行时， 我们不关心哪一个线程被唤醒时，才会使用notify()

**从设计的角度**：  
因为wait和notify存在竞争关系，wait和notify的调用顺序必须被严格限定。  
而且wait通常伴随着条件语句**if(A)wait()**，而notify则对应**A；notify（）**。同时，为了防止**虚假唤醒**一般将条件语句换成循环**while(A)wait()**  
wait和notify用于线程通信，肯定是线程A调用if(A)wait()和线程B调用A；notify（）。  
如果A；notify()和if(A)wait()可以被执行，将会出现**死等**的问题——A ;if(A) ; notify() wait() 最终的结果是wait()没有notify对它进行唤醒，线程一直阻塞在等待队列中。（死锁、死等导致任务无法被处理、相应内存一直被占用、造成**内存泄露**和**浪费线程资源**）



#### sleep与wait
sleep来自Thread类，而wait来自Object类。  
sleep是线程的行为，而每个Object对象都可以关联一个monitor对象，因此wait/notify被设计属于Object。二者都声明了中断异常（throws InterruptedException），由于java天生就是多线程的，因此任何地方（实例方法、主方法）都可以调用Thread.sleep(xxx)，而默认调用方就是主线程。

当然了，这些都是java层面的描述。二者的阻塞JVM底层都依赖**park（）函数**，这会导致线程放弃CPU被挂起。只不过wait搭配wait set使用，因此增加了释放锁的逻辑。而调用sleep时，JVM不关心当前线程是否持有锁，因此调用sleep并不会释放锁。  
调用sleep或wait后，java线程处于wait等待状态。


#### yield与join
yield调用使得当前获得CPU的线程让出CPU资源，以便其他线程有机会抢占（有可能当前线程会再次抢占）。sleep(0)和yield()可以达到相同的效果。

> 实现上，底层通常会使当前线程放弃CPU资源，同时加入同等优先级队列的末尾。对于和调用线程相同或者更高优先级的线程来说，yield方法给予他们一次运行的机会

而join底层依靠wait/notify实现，使用场景：父线程需要等待子线程的结果即需要等待子线程运行结束。join方法本身也是一个同步方法，而**子线程对象**本身也是一个Object对象，具有相应的monitor。
`son.join;`
主线程中调用son.join（）底层相当于调用son.wait()，这里把son线程对象看作一个Object对象、一个对象锁。父线程调用完son.wait()后进入monitor关联的waitSet中。  
以下的伪代码中，son有两层含义：子线程对象和monitor

```java
    public static void main(String[] args) {
        synchronized (son){
            while(son.isAlive()){
                son.wait(0);
            }
        }
    }

```
当son线程执行完毕，会唤醒父线程，同时isAlive()调用结果为false，父线程（主线程）退出等待状态。（notify的调用位于JVM源码中，join的java源码中只能找到wait的调用）


#### interrupt
jdk主要提供了三个中断相关方法，这里的中断指的是对java线程阻塞打断，如果一个线程正在正常执行，那么不会做出任何反映。

【1】interrupt。在一个线程中调用另一个线程的interrupt()方法，即会向那个线程发出信号——线程**中断状态**已被设置（set为true）。至于那个线程何去何从，由具体的代码实现决定  
【2】isIntercepted。用来判断当前线程的中断状态(true or false)  
【3】interrupted。是个Thread的静态方法，用来**恢复中断初始状态**（检查中断标志，返回一个布尔值并清除中断状态，第二次调用时，中断状态已经被清除，返回false）——检查当前线程的中断标志并且清除(重置为非中断false)

> interrupted底层调用了isIntercepted()方法，同时清除了标志位，实质上是返回currentThread()。isInterrupted()并且重置中断标志，这也是它作为一个静态方法存在的原因。

底层，当一个线程被调用interrupt()方法时，JVM拿到这个线程对象（C++），然后插入**内存屏障**以保证该线程的中断状态的可见性，**修改线程对象的中断状态为true**。之后对该线程调用**unpark函数**将线程唤醒。  
【1】如果这个线程阻塞在wait、yield、sleep等可中断的方法，线程被唤醒后将检查自身中断标记，如果为true则会抛出interruptException。  
【2】如果线程仅仅是阻塞在synchronized对应的entryList，那么被唤醒后会再次产生获取锁，失败则进行进入阻塞状态，**不会响应中断**  
【3】Lock.lock()方法和synchronized差不多，被唤醒后也会调用unsafe封装的park()继续阻塞。而lockInterruptibly被唤醒后则检查中断标记，并抛出异常。

一般情况下，**抛出异常时，会清空/重置thread的interrupt标记**。

总结：线程中断的底层实现中，实际上是将线程唤醒，但是线程如何响应则取决于此时的调用函数

##### interrupt总结
每个线程会有一个**中断标志位**，这个中断标志位是由JVM源码层面去维护的，java层面看不见这个标志位，当某个线程去调用这个interrupt方法的时候，本质上是对某个线程的标志位进行了一个置位操作，然后去唤醒一下这个线程。如果这个线程没有进入wait或者timed_wait的状态（这里的状态指的是java线程层面的），那么其实这个interrupt的调用是没有任何效果的。

这个interrupt调用一般和两种行为进行搭配：【1】我们自己去轮询这个中断状态，然后做出相应的相应。【2】配合声明抛出中断异常的调用去使用，例如sleep、wait、以及JUC下lock的各种实现类支持的“可中断锁”去使用。  
对于后者，一般都是将自己通过park()调用阻塞起来，而当我们调用interrupt之后，会额外对该线程执行一个unpark()调用。（park和unpark就是以线程为单位的调用），一个线程被unpark()唤醒之后一般也会做出不同的表现，如果是sleep()、wait()这类的调用，线程会检查一下中断标志，如果true则抛出异常并清除中断标志，而阻塞在synchronized同步队列的线程则是“认为自己被虚假唤醒”，于是继续调用park()进行阻塞状态。

另外，由于所有线程如果拿到某一个线程对象的引用，都可以去调用interrupt方法，因此必须保证线程中断标志位的可见性，且修改这个标志也需要是同步的。其中**原子性**通过java层面的synchronize去保证，**可见性**则由内存屏障去保证。

> JMM向我们保证：线程A对线程B调用interrupt() 线程B检测到中断事件发生 。说白了，JMM向我们保证，当一个线程对另一个线程调用interrupt，被中断对该操作一定是可见的。不会因为重排序而发生可见性问题。

#### park/unpark
LockSupport是Java6(JSR166-JUC)引入的一个类，用来**创建锁和其他同步工具类的基本线程阻塞原语**。底层是对unsafe类对应的park/unpark方法的封装，java实现阻塞与唤醒功能底层绝大多数都依赖了park函数（park底层调用了哪些系统调用和具体平台、操作系统有关）。  
park/unpark更加贴近操作系统层面的阻塞与唤醒线程，不需要获取monitor，以线程为单位进行操作。  
每个java线程底层都绑定了一个Parker对象，主要有三个字段：counter、condition和mutex  
counter用于记录“许可”。  
**当调用park时，这个变量置为了0；当调用unpark时，这个变量置为1**。（二进制置位）  
（unpark提供的许可是一次性的，不能叠加，两个函数的调用使得count在0和1直接切换，底层依赖互斥量mutex 系统调用。当许可为0时，线程被挂起，直到再次获得许可）

park和unpark的灵活之处在于，**unpark函数可以先于park调用**。比如线程B调用unpark函数，给线程A发了一个“许可”，那么当线程A调用park时，它发现已经有“许可”了，那么它会马上再继续运行。但是park()是**不可重入**的，如果一个线程连续2次调用LockSupport.park()，那么该线程一定会一直阻塞下去（调用的时候，如果资源为1则 不会阻塞线程，如果资源为0则会阻塞进程）

相对于wait/notify，park与unpark的调用顺序不是固定的，而且是以线程为单位的，每个线程需要关联一个Parker对象。而wait需要关联到一个monitor对象的waitSet。park/unpark可以看作wait/notify实现的基础。

> wait、notify、synchronized等底层依赖JVM源码级别的park/unpark实现，而java封装了park/unpark，其中用户可以直接使用lockSupport提供的park和unpark函数，而AQS框架实现阻塞与唤醒底层依赖了unsafe提供的park/unpark（也属于native方法，底层是c++提供的）



## 5.理解Lock接口
Lock是JUC包下提供的接口，定义了一次锁类型应该具有的行为。  
Lock接口的意义就是把锁这个东西抽象为了一个对象拿到台面上来了，而不是像synchronized那样将锁这个东西透明化了。  
提供Lock接口，使得一提到锁对象不再只是C++的objectMonitor对象，而也可以是Lock对应的reentrantLock、reentrantReadWriteLock等对象了。  
Lock接口提供了与synchronized相似的行为，同时提供了一些额外的特性：  
【1】非阻塞获取锁tryLock  
【2】可响应中断的上锁方式lockInterruptibly  
【3】超时获取锁，在指定的时间内没有获取锁将返回一个布尔值。

另一方面，将Lock从底层抽象出来，也可以使得用户更好的监控锁的行为，如当前的owner是谁？锁是否被获取等。而且可以接着扩展用户子接口，来使得锁可以扩展出更多的行为，使得上锁操作更加灵活可控


### synchronized与Lock（reentrantLock）对比
Lock毕竟是一个接口，讨论还是需要具体到某一个实现类上的。以最常用的reentrantLock为例。  
上面已经提到过Lock接口本身提供的synchronized不具备的特性：支持超时、非阻塞、响应中断、更好的扩展性  
synchronized和reentrantLock都是可重入的，实际上AQS大多数锁都是可重入的，这可以在一定程度上避免死锁的发生  
synchronized默认就是非公平的，而Lock只是定义了行为，实现类可以基于非公平和公平进行实现，这也反映基于Lock实现锁更加具有扩展性。

synchronized实现线程通信时搭配wait以及monitor的waitSet,reentrantLock搭配condition对象和await方法。一个synchronized对应一个monitor，因此多个线程调用wait()后将会等待在同一个waitSet。而基于高层实现的reentrantLock可以创建多个condition对象，每个condition对应一个等待队列，因此不同的线程根据不同的等待条件，可以等待在不同的队列，可以使得线程唤醒更加精确。

实际上synchronized的优点也不少：  
【1】synchronized使得用户不需要关心上锁、解锁的逻辑，甚至不需要关心锁对象的存在，而我如果想使用reentrantLock，那么我必须显示创建一个对象，并且显示的lock和unlock。而且必须写在**try/finally**中，因为synchronized隐式帮我们释放锁，即使出现了**异常**，而reentrantLock使用的过程中出现异常，并且没有处理锁对象的释放，那么可能出现死锁。  
【2】以concurrentHashMap 1.8为例，万物皆为monitor，因此可以把数组元素本身看作一个锁，而不需要向concurrentHashMap 1.7那样显示创建锁对象，并且锁的粒度更小，并发度更大。

更深一层，synchronized和reentrantLock实现了相同的特性：可见性、原子性、有序性。  
其中reentrantLock实现这些特性极大依赖于底层的AQS框架（AQS框架使得reentrantLock更加关注于如何实现可重入锁的逻辑而不是同步、阻塞等工作）  
reentrantLock实现可见性和原子性，基于读写volatile变量和CAS指令，同时通过CAS修改锁变量保证原子性。

> 实际上，抛开一些细节，reentrantLock可以看作对synchronized基于java代码的再次实现，一些实现逻辑十分相似，底层都离不开CAS加锁以及直接或间接地插入内存屏障。但是reentrantLock仅仅是Lock/AQS的冰山一角而已。synchronized中的可重入锁是透明的，它只是实现管程synchronized的一个组件，而reentrantLock则是被单独提取，提供给用户，出来以进行复用和扩展。

### 理解AQS框架
AQS abstract queued synchronized 抽象队列同步器，是用于实现锁以及其他同步组件的基础框架，内部实现了线程管理、同步状态管理与队列管理这些“无关性”代码。简化了**组件开发者**的实现工作——不用去关系线程排队、节点封装等底层细节，只需要去使用或重写框架指定的方法即可。  
AQS的设计基于**模板方法设计模式**。

队列同步器面向组件开发者，而组件则面向使用者/一般的程序员。同步器与组件分别为开发者和使用者屏蔽了不必要了解的细节。

如果你想要定义一个自定义组件，仅需要：定义一个实现AQS的静态内部类，组合一个该类型的字段SYN，实现LOCK接口，并且全部委托给这个成员SYN实现即可。唯一需要做的就是：重写AQS中提供的钩子方法（如tryRelease、tryAcquire），同时使用AQS框架已经实现好的方法去实现对应功能（如setExclusiveOwnerThread、getState等）


## 5. 模式


# 6. 琐碎

