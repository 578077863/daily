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


第1和2种其实都一样,在Thread源码中, if(target != null){target.run;}  如果继承Thread就相当于重写了默认run方法,而传入Runnable实例,就相当于使用构造函数为Thread的runnable成员赋值


**Java 线程调用 start->start0 这个本地方法，实际上会调用到 JVM_StartThread 方法，而 JVM_StartThread方法中会创建与平台相关的本地线程，该线程执行 Java 线程的 run 方法。**
**创建线程只有一种方式就是构造Thread类,而实现线程的执行单元有两种方式**

实现接口和继承Thread比较
推荐使用实现runnable接口的方式，因为实现runnable接口本质上是完成“布置任务”的行为，可以减少“线程细节”与“任务”本身的耦合度——线程是载体，任务是主要关注点，**不是让某类线程绑定某个任务，而是产生一个任务后创建一个线程实例去执行**。

实现接口会更好一些，因为:
-   从共享资源来看，Runnable接口更适合（因为都是同一个Runnable实例）
-   从继承角度看，Runnable更适合，因为接口可以多继承，类只能单继承，这样不利于扩展



【3】以上两种方式很大程度上取决于runnable接口定义的行为，其中runnable接口规定的run方法有两个问题：**没有返回值、不能抛异常**。


jdk5引入了callable接口
>[Java中的Callable的返回值是怎么来的？ - 简书 (jianshu.com)](https://www.jianshu.com/p/8f1cddc08747)

`    V call() throws Exception;`
callable接口规范的call方法允许返回值和异常。
而futureTask类似一个**适配器**，它有一个callable成员，同时（间接）实现了runnable接口，而run方法实现了本质就是call方法的调用。
同时futureTask提供了get方法，是一个阻塞调用，一旦得到结果，方法便返回。

如此，实现callable接口可以算是第三种实现线程的方式：call方法定义任务、thread.start()启动线程，futureTask.get()拿到结果或异常
```java
        FutureTask<Integer> futureTask = new FutureTask<>(() -> {//布置任务
            throw new IndexOutOfBoundsException("233");
        });
        new Thread(futureTask).start(); //线程启动
        System.out.println(futureTask.get());//阻塞，直到返回结果


```
如果正常执行，get（）会得到返回值，如果出现异常，最终get()会抛出executionException。

>run方法中调用了call()，而将返回值和异常对象都保存到成员字段outcome中，在get()中判断outcome的类型，如果是结果就返回否则就抛出异常

总结：callable/call()计算出结果，而通过futureTask/get()向外暴露/公布结果。futureTask表示一个异步运算的结果，对这个异步运算的任务可以等待获取、判断是否完成以及取消任务。

注意：同一个callable()实例对应的任务只会执行一次，不会执行第二遍


>每个futureTask实例都有状态，没当一个futureTask被实例化后，状态为NEW，而执行完毕状态就变为normal。只有当futureTask状态为NEW时run方法才会执行


**我的理解**:本质就是FutureTask继承RunnableFuture接口,而该接口又继承了Runnable和Future两个接口,继承Runnable接口目的是能让Thread调用start开启一个线程,而继承Future目的是Future接口中包含获取返回值结果的方法`get`如果线程没有执行完任务，调用该方法会阻塞当前线程,以及取消执行任务`cancel`,查看任务是否执行完毕`isDone`,以及任务是否取消`isCancelled`。

在实现FutureTask类的run方法中执行了Callable的call方法,将其返回值赋给成员变量result,当使用get方法时,会判断callable任务的状态,如果没有完成，那么阻塞当前线程，等待完成，如果处于已经取消状态直接抛出异常，如果已经执行完毕，将结果返回。
  

FutureTask接口实现的比较复杂，阅读源码理解起来相对困难，但是本质上，FutureTask接口是一个生产者消费者模式，如果生产者没有生产完，那么会阻塞消费者，将消费者放到一个阻塞队列中，生产者生产完后，会唤醒阻塞的消费者去消费结果，大概原理就是这样，下面是一个简易版的实现。
```java

class MyRunnableFuture<T> implements RunnableFuture<T> {

    private Callable<String> callable;

    private Object returnObj;

    private ReentrantLock lock = new ReentrantLock();

    private Condition getCondition = lock.newCondition();

    public MyRunnableFuture(Callable<String> callable) {
        this.callable = callable;
    }

    @SneakyThrows
    @Override
    public void run() {
        this.returnObj = this.callable.call();
        try {
            lock.lock();
            this.getCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isCancelled() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isDone() {
        throw new NotImplementedException();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (returnObj == null) {
            try {
                lock.lock();
                this.getCondition.await();
            } finally {
                lock.unlock();
            }
        }
        return (T) returnObj;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new NotImplementedException();
    }
}
```




#### start与run
调用start（）方法后，java线程进入runnable状态，而jvm会为之创建线程并且调用run()方法。（如果用户调用run()那么就是一个普通的方法调用，而如果调用start()就是委托JVM创建一个线程，而JVM为委托OS创建一个线程，最终将执行run(）方法的任务交给子线程而不是主线程）

#### 为什么java只有runnable状态？
操作系统层面，处于running状态的线程如果放弃CPU则进入runnable状态，而java为我们屏蔽了线程切换（CPU调度）的细节，我们只需要知道创建一个线程，然后调用start()方法该线程进入runnable状态即可。如果将runnable拆分为两个状态，那么就相当于打破了操作系统和虚拟机之间的隔离性，违背了jvm设计的初衷，本末倒置了。
java线程执行的过程中，总是不断的发生CPU调度，但是对于用户是透明的，因为java屏蔽了这一切的细节，对于用户来说，一旦一个线程被start()调用，那么它便是runnable的。


#### 线程池
线程池的核心作用：
【1】通过预先创建线程和复用线程来避免频繁的创建和销毁线程，创建、销毁线程底层涉及系统调用，系统调用会造成CPU上下文切换，我们的目标是让CPU更多去执行用户代码，而减少执行系统代码，提升CPU的利用率（执行用户代码的吞吐量）
【2】时延，一个请求/任务到达如果还需要先申请内存、创建线程那么这将占用一定时间，如果任务到达直接就可以对应一个线程去执行，那么响应时间将会大大缩短，用户体验上升
【3】统一管理，以前线程都是临时工，每次使用都是“一次性”的，而使用线程池可以集中管理一组线程，并且可以统一分析、调优和监控。

一开始，我们通过继承thread重写run方法完成线程的设计。但是这是耦合的，因为thread不但包含线程开启、销毁等与线程调度、生命周期相关的方法，而且通过重写run方法，直接将线程执行逻辑嵌入了一类方法中。而使用runnable方法单独规划任务有一定改善，thread类通过组合runnable类型成员，run方法本质上执行的是外界传入的runnable的run方法。
但是以上两种方式，线程本身和任务本身都由用户直接管理，引入线程后，线程交给线程池对象管理，我们只需要设计任务对象本身即可。

**简述线程池原理**
线程池设计的核心思想：==将任务单元与执行单元相分离，用户负责提交任务，而线程池负责“调度”线程执行任务。==
用户向线程池中提交一个任务，这个任务如果是无返回值的（runnable），那么用户就不用管别的了，如果是由返回值的（callable），那么线程池会给用户返回一个接口（future），用户可以使用这个接口获得异步计算的结果。
线程池收到用户提交的任务后，如果池中线程数量没有达到核心线程数，那么就创建新线程去执行任务，创建新线程后直接去执行任务。线程执行完毕后就会从工作队列中取任务，工作队列中没有任务线程就会阻塞
>先判断是否达到coreSize，因此是否有空闲线程都会创建。“取出并执行”其实就是工作线程去调用任务的run()，因为线程已经“跑起来”了（线程池创建一个线程start()后JVM为其分配OS线程并且调用run()方法），而它的run方法就是一个循环，去扫描工作队列提交的任务。一个核心线程被创建后向去执行一个被提交的任务，然后就去执行扫描队列的循环。非核心线程被创建后也是先执行“入队失败”的任务，然后去扫描工作队列

如果一任务提交到池中，但是线程池的线程数量已经达到核心线程数，这个任务将被提交到工作队列，后续空闲线程会从工作队列取出并执行。一旦队列满了（入队offer失败）则尝试创建非核心线程，如果无法继续创建线程，将对当前任务执行某项拒绝策略。

简单来说：线程池中的线程是“懒创建”的，每个线程创建后总是先执行一个提交的任务，随后去扫描工作队列——工作队列满了，核心线程忙不过来了就创建一些非核心线程，如果无法创建那么必须要对当前的任务一个处理办法，那就是执行拒绝策略


>当队列为空时，核心线程和非核心线程其实都是阻塞在工作队列的，一旦有任务添加，便会唤醒线程（然后抢着执行任务，加锁），其中非核心线程的阻塞是超时阻塞，如果一段时间没有任务到达那么非核心线程将被超时唤醒，CAS减少线程数量，如果成功那么这个线程就被释放了（线程的run方法返回，同时指向线程对象的引用被移出线程池维护线程引用的哈希表），失败则说明“已经有线程被销毁了”，那么继续新一轮阻塞（下一轮循环）


```JAVA
if (isRunning(c) && workQueue.offer(command)) {
    int recheck = ctl.get();
    if (! isRunning(recheck) && remove(command))
        reject(command);
    else if (workerCountOf(recheck) == 0)
        addWorker(null, false);
}
else if (!addWorker(command, false))
    reject(command);

```

源码中，这里还有一个细节：  
如果向队列中放入一个任务，但是队列已经满了，则尝试创建非核心线程去执行当前任务，如果创建失败则说明已经达到最大线程数量，执行拒绝策略。  
**当一个任务成功放入队列之后，会执行一个核实操作（再次检查）**，如果发现此时线程池已经进入非running状态，则尝试移除这个任务，从队列移除成功后则执行拒绝策略。  
如果当前线程池处于running状态或者移除command失败（可能被别的线程拿走了），且此时的工作线程数量为0，这时线程池执行一个保守策略，向线程池中放入一个非核心线程。  
**（以上情况，可能在coreSize=0的线程池遇到，例如newCachedThreadPoolExecutor创建一个核心线程数为0的线程池，每次提交任务都会创建一个非核心线程）**

Worker本身是对thread和task的封装，可以看作高层次的thread

```JAVA

Worker(Runnable firstTask) {
    setState(-1); // inhibit interrupts until runWorker
    this.firstTask = firstTask;
    this.thread = getThreadFactory().newThread(this);
}
```

如果向工作线程队列，成功添加worker实例，则调用worker.thead的start()方法（委托底层分配一个载体去运行），而run方法则是任务的执行逻辑。（执行用户传入的runnable任务或者callable任务）

```JAVA
if (workerAdded) {
    t.start();
    workerStarted = true;
}

    
```


##### 线程池核心参数

【1】corePoolSize：核心线程数，也就是常驻线程池的线程最大数量  
【2】maximumPoolSize：最大线程数，线程池的最大线程数，包含核心线程与非核心线程  
【3】keepAliveTime:非核心线程的最大存活时间  
【4】timeUnit：单位，是一个枚举，可以指定keepAliveTime的单位  
【5】workQueue：工作队列，通常是阻塞队列  
【6】threadFactory：线程工厂，通过工厂模式创建线程，一般使用executors提供的默认工厂，可以指定线程命名规则等  
【7】rejectedExecutionHandler：拒绝策略，当任务无法被处理的处理方案，一般使用ThreadPoolExecutor提供的静态内部类


##### 拒绝策略类型

ThreadPoolExecutor提供了四种拒绝策略，同时还可以实现RejectedExecutionHandler接口自定义拒绝策略。  
【1】丢弃最老的（出队最老的，然后重新提交任务）

```java
                e.getQueue().poll();
                e.execute(r);

    
```

【2】丢弃当前任务（对当前任务什么也不做，相当于丢弃了）

```java
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        }

    
```

【3】直接抛出异常

```java
            throw new RejectedExecutionException("Task " + r.toString() +
                                                 " rejected from " +
                                                 e.toString());

    
```

【4】让提交任务的线程去执行（任务提交相当于直接去执行这个任务了，相当于在main方法中调用线程对象的run方法）

```java
                r.run();

    
```



##### 工作队列

工作队列首先是一个队列，它实现了队列的接口，因此它具有先进先出的行为。  
而工作队列实现了阻塞队列的接口，是阻塞的：当队列空获取元素操作将被阻塞。当队列满，添加元素的操作会被阻塞。如果是单线程，使用阻塞队列不就会死锁了？因此阻塞队列是基于多线程环境下使用的，而且访问队列是需要加锁的。（具体细节还有看具体的实现）

> queue接口搜索jdk5之后的接口，是collection的子接口，与list和set同级，规定了队列先进先出的行为offer/poll/peek，而blockingQueue是queue的子接口，提供了两个阻塞的行为put/take  
> （线程池中，向工作队列添加元素时使用offer，非阻塞，添加失败有返回值）

jdk提供了  
【1】基于数组的阻塞队列arrayBlockingQueue  
【2】基于链表的阻塞队列linkedBlockingQueue  
【3】不存储元素的阻塞队列（一旦put就会阻塞等待另一个线程take，因此称为同步队列）synchronousQueue  
【4】支持优先级排序的队列priorityBlockingQueue（相当于优先队列的阻塞实现）  
【5】使用优先级队列实现的延迟无界阻塞队列delayQueue（每次取都会计算时间值，根据时间值判断是否允许取出，不到时间就阻塞），可以实现定时任务、数据缓存、设置过期值等。

推荐使用构造函数创建线程池对象的一部分原因，就是因为默认创建的线程池对象的工作队列都是无界的，如果短时间内提交过多任务，任务被无限地保存在内存的队列中——**任务不会被拒绝，内存可能会被耗尽**。

##### 写一个阻塞队列

阻塞队列其实就是普通的队列+锁+通信。一旦队列满了，线程就阻塞。一旦队列有空闲，阻塞线程就被唤醒。

```java
    private Object[] array;

    private int tail;
    private int head;
    private int cap;

    private ReentrantLock lock =new ReentrantLock();
    private Condition full = lock.newCondition();
    private Condition empty = lock.newCondition();

    
```

这里使用环形数组作为底层数据结构，并且使用两个指针指向首位，使用两个condition标志“队列空”和“队列满”，使用一个全局锁管理对队列的访问

```java
    public void put(E e) {
     lock.lock();
     try {
         while ((tail+1)%cap==tail){//队列满，阻塞
             try {
                 full.await();
             } catch (InterruptedException e1) {
                 e1.printStackTrace();
             }
         }
         array[tail]=e;
         tail=(tail+1)%cap;
         //队列不空，唤醒因为empty而阻塞的线程
         empty.signal();
     }finally {
         lock.unlock();
     }
    }

    
```

put对队列插入的过程看作一个“事务”，需要保证线程安全。如果队列已经满了就阻塞，否则插入并唤醒“空”条件下阻塞的线程

```java
    public E take() {
        lock.lock();
        try {
            while (head==tail){
                try {
                    empty.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            E res= (E) array[head];
            array[head]=null;
            head=(head+1)%cap;
            //不满了，唤醒full条件的阻塞线程
            full.signal();
            return res;
        }finally {
            lock.unlock();
        }
    }

    
```

take方法一个道理，不再赘述

```java
    public int size() {
        lock.lock();
        try {
            return (tail-head+cap)%cap;
        }finally {
            lock.unlock();
        }
    }

    
```

查询size也是一个事务，当然了使用一个变量实时保存size也是可以的。


##### 线程池的几种类型

说是线程池类型，其实就是工具类executors为我们提供的、预设好的线程池的构造方法。  
【1】fixedThreadPool  
工作线程是固定的，也就是**核心线程数等于最大线程数**，指定多少那么线程池最多就只能创建多少。核心线程数+工作队列size（）就是最大任务数量。阻塞队列是linkedBlockingQueue，是无界的（默认大小是int最大值），适合负载比较大的服务器，为了资源合理利用需要现在线程数量。

> 无界队列导致的问题：如果一个任务执行时间特别长，那么队列积攒的任务将越来越多，导致内存占用飙升最终导致OOM。

【2】singleThreadExecutor  
线程中只有一个线程，相当于fixedThreadPool的单线程版本，适用于串行执行任务的场景。  
【3】scheduledThreadPool  
在给定的延迟后执行任务，基于delayWorkQueue。适用于周期性执行任务的场景。  
该线程池也称为**定时线程池**，实现类实现了scheduledExecutorService接口，其中schedule方法可以延迟执行一个任务，可以执行一个runnable或callable任务。  
scheduleAtFixedRate方法可以指定一个初始延迟和一个周期，可以用于**实现周期任务**。scheduleWithFixedDelay方法，按照延迟执行周期任务（前一个是到点就执行，这个是执行完毕后必须延迟一一段时间才能继续执行）

> scheduledThreadPool底层，任务被提交到延迟队列（delayWorkQueue），延迟队列内部封装的就是一个线程安全的、阻塞的优先队列，其中根据到期时间排序，如果时间相同就根据提交顺序排序。  
> 任务到期就被线程取出去执行，如果执行结束且属于定时任务，又会重新设置到期时间放入队列。工作线程从队头（开始时间最早的元素）里拿元素，如果执行时间不到线程就阻塞，直到执行时间达到，任务才会出队被执行。  
> 一个定时任务包含：任务开始/定时到期时间、任务执行的间隔、入队序列号，定时任务继承了futureTask，使得任务可以被异步执行和获取结果。（定时任务比普通任务多出了更多功能，例如可以被多次执行、延时执行、定时执行等）

【4】CashedThreadPool  
**核心线程数量为0，而且不限制最大线程数量，空闲时间60秒，基于同步队列。**  
这表示每当任务到达，就会提交到同步队列，而创建后的线程总是阻塞在同步队列等待任务到达，一旦超时空闲时间就会回收，因此长时间保存空闲的线程不会占用资源。

但是如果任务执行的很慢，而任务提交的快，将会创建大量的线程，而这些线程都不是空闲的线程，占用资源，极端情况下内存资源和CPU资源将被极大消耗。

适用场景：**并发执行大量短小的任务**。

> CPU密集型线程应该配置少一些线程，因为CPU切换十分频繁，少量的线程可以提升CPU利用率。而IO密集型线程应该配置多一点线程，因为一些线程因为等待IO而放弃CPU资源，那么CPU将不被这些状态下的线程利用，因此需要多分配一些线程。  
> fixedThreadPool适合处理CPU密集型的任务，确保CPU在长期被worker线程使用的情况下，尽可能少的分配线程——适用执行长期的任务。不适合IO密集型场景，因为会堆积大量任务导致内存占用飙升。

线程池中线程数量的确定需要参考CPU数量和应用类型（设CPU数量为N）：  
【1】CPU密集型，少量线程数可以减少线程切换带来的代价，线程数量为N+1  
【2】IO密集型，多一些线程可以防止CPU空闲，设置为2N+1  
估算公式：最佳线程数目=（（线程等待CPU时间+线程获得CPU的时间）/线程获得CPU时间）X CPU数目。（即CPU数目 X （等待CPU时间/占用CPU时间+1））  
线程等待时间占比越高，需要越多线程，CPU持有时间越多，需要越少线程。

##### 线程池异常处理

**线程之间的异常是独立的，主线程无法捕获子线程抛出的（未捕获/未处理）异常**，而这个异常最终会交由JVM处理——打印堆栈信息。而基于call()可以捕获异常，futureTask封装这个异常。

```java
        pool.submit(()->{	//异常被线程池内部捕获，不输出
            throw new IndexOutOfBoundsException();
        });
        pool.execute(()->{	//异常交给JVM处理，打印堆栈信息
            throw new IndexOutOfBoundsException();
        });

    
```

execute中异常堆栈信息将被打印在控制台（因为run()不支持抛异常），而submit则可以捕获异常（call()支持抛异常）,因为submit接收的是callable实例，而execute接收的是runnable实例。futureTask是一个适配器，它是runnable与callable的一个转接口，其中它对runnable中run方法的实现中，使用try/catch包裹了call()调用。

```java
try {
    result = c.call(); 保存执行的结果
    ran = true; 保存执行状态
} catch (Throwable ex) {
    result = null;
    ran = false;
    setException(ex); 捕获子任务中的异常
}

    
```

因此可以使用返回的futureTask实例的get方法来获得异常信息。

> 也就是说，虽然两个线程之间不能传递异常，但是子线程可以捕获call()抛出的异常，并将异常信息保存在futureTask的成员成员中，而主线程可以拿到futureTask的引用，当futureTask调用get（）方法一旦发现子线程产生了异常，那么futureTask就抛出异常（相当于子线程的代理人），以此告知主线程：子线程出现异常

以上强调的实际上是“线程未捕获的异常”，类似在main函数中抛异常，然后主线程未捕获处理的异常，这个时候JVM会处理这个**未处理/捕获异常**，如打印这个异常的堆栈信息。因此封装到futureTask的异常一定是call()方法中未经过捕获的异常，而我们使用execute走的仍然是给JVM处理这个未捕获异常的逻辑即打印堆栈信息（因为run方法中如果出现编译异常一定是需要处理的，如果是运行时异常，直接就抛给JVM了）。

也可以为worker线程设置**未捕获异常处理器**

```java
ExecutorService threadPool = Executors.newFixedThreadPool(1, r -> {
    Thread t = new Thread(r);
    t.setUncaughtExceptionHandler(
            (t1, e) -> System.out.println(t1.getName() + "线程抛出的异常"+e));
    return t;
});

    
```

第二个参数使用了匿名函数，其实是接口threadFactory的实现类，该接口只有一个方法newThread（）  
uncaughtExceptionHandler是thread类的一个成员属性，默认为null，通过实例方法setUncaughtExceptionHandler来设置

> 在Thread中，Java提供了一个setUncaughtExceptionHandler的方法来设置线程的异常处理函数，你可以把异常处理函数传进去，**当发生线程的未捕获异常的时候，由JVM来回调执行**

另一种方式：  
**重写threadPoolExecutor的afterExecutor方法，处理传递的异常引用**

```java
public class ThreadReview extends ThreadPoolExecutor{ 
// 相当于通过继承，扩充了方法，加强了原实现类
    public ThreadReview(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
    //后处理函数，该方法是ThreadPoolExecutor提供的构造方法/模板方法
        if(t ==null&& r instanceof Future<?> ){
            try {
                Object res = ((Future) r).get();
            } catch (Exception e) {
                t=e;
                Thread.currentThread().interrupt();
            }
        }
        if(t!=null) System.out.println(t.getMessage());
    }
}

    
```

> 注意，一旦线程池某个线程出现异常，线程池会释放这个线程，然后再创建一个新的（非核心）线程放入池中（这个线程创建后没有任务执行，直接去扫描同步队列）


##### 细节

###### execute与submit

execute()是上层接口executor提供的方法，而submit()是字接口executorService提供的方法。  
其中execute()接受一个任务实例（runnable）且没有返回值。而submit()方法具有返回值，是用来接收callable实例的，返回一个futureTask对象，用户可以使用该对象获取异步结果。（阻塞调用）

##### 线程池状态

处于running状态的线程池可以正常提供服务（接收任务提交），当调用shutdown()时变为shutdown状态，而调用shutdownNow则变为stop状态。

shutdown状态下的线程池不会接收新的任务（对新任务执行拒绝策略），但是会继续处理剩余的任务。当池中任务为空（线程执行任务完毕且队列为空）则进入tidying状态。  
而stop状态下的线程池（shutdownNow），会中断所有正在执行的任务，丢弃未执行的任务，然后进入tidying状态。  
进入tidying状态后，表示所有任务已经终止，且没有剩余的任务需要执行。调用terminate()后进入terminated状态表示线程池彻底关闭

> 调用shutdown方法后，如果所有线程都被阻塞，会唤醒所有阻塞的线程，线程判断线程池状态为shutdown便主动销毁。否则如果存在正在执行的任务，则等待任务执行完毕后的线程销毁时向阻塞线程发送中断信号。

#### 线程池思考
##### 线程池队列满了
自定义reject策略，如果线程无法执行更多的任务，可以把这个任务信息持久化写入到硬盘中去，后台会专门启动一个线程，等后续线程池的工作负载降低了，就慢慢地从磁盘读取之前持久化的任务，重新提交到线程池里去执行

##### 如果线上机器突然宕机，线程池的阻塞队列中的请求怎么办？
必然会导致线程池中积压的任务都会丢失。

如何解决这个问题呢？

我们可以在提交任务之前，在数据库中插入这个任务的信息，更新任务的状态：未提交、已提交、已完成。提交成功后，更新它的状态是已提交状态。

系统重启后，用一个后台线程去扫描数据库里的未提交和已提交状态的任务，可以把任务的信息读取出来，重新提交到线程池里去，继续进行执行。

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

在 Java 中垃圾回收线程就是特殊的守护线程。
#### sleep
Thread.sleep(millisec) 方法会休眠当前正在执行的线程，millisec 单位为毫秒。

sleep() 可能会抛出 InterruptedException，因为异常不能跨线程传播回 main() 中，因此必须在本地进行处理。线程中抛出的其它异常也同样需要在本地进行处理。

#### yield
对静态方法 Thread.yield() 的调用声明了当前线程已经完成了生命周期中最重要的部分，可以切换给其它线程来执行。该方法只是对线程调度器的一个建议，而且也只是建议具有相同优先级的其它线程可以运行。



###  4.4 java线程通信机制
基于共享内存机制
>[(60条消息) 【多线程】Java线程间是如何通信的呢？慕沐.的博客-CSDN博客_线程间是如何通信的](https://blog.csdn.net/cxh6863/article/details/106779083)

同步：synchronized
关键字synchronized可以修饰方法或者以同步块，它主要确保多个线程在同一个时刻，只能有一个线程处于方法或者同步块中，它保证了线程对变量访问的可见性和排他性。


信号量：volatile
Java支持多个线程同时访问一个对象或者对象的成员变量，由于每个线程可以拥有这个变量的拷贝，所以程序在执行过程中，一个线程看到的变量并不一定是最新的。
关键字volatile可以用来修饰字段（成员变量），就是告知程序任何对该变量的访问均需要从共享内存中获取，而对它的改变必须同步刷新回共享内存，它能保证所有线程对变量访问的可见性。

关于synchronized与volatile ，synchronized主要做的是多线程顺序执行，也就是同一个时间只有一个线程在执行，线程A执行完了再让线程B执行，volatile主要做的是让多线程间共享的变量保证一致，也就是线程A对变量操作了，线程B对变量操作时是知道线程A对变量的操作的，是在线程A操作后的变量上进行操作。

等待通知机制：wait、notify
等待/通知机制使⽤的是使⽤同⼀个对象锁，如果你两个线程使  
⽤的是不同的对象锁，那它们之间是不能⽤等待/通知机制通信的

### 4.5 synchronized及锁升级
>[(58条消息) synchronized加锁流程 从偏向锁到重量级锁_大老李superLi的博客-CSDN博客](https://blog.csdn.net/weixin_43955776/article/details/107078477)

synchronized是java实现线程同步的一个关键字，同步就是步调一致，synchronized修饰代码块或者函数，那么这个区域就可以看作一个同步块，不存在某一时刻多个线程同时执行同步块的代码。

谈到多线程，那就离不开**共享变量**，如果synchronized包裹的同步块中操作的净是些局部变量，那synchronized同步了个寂寞。什么时候需要同步，那肯定是多个线程并发访问同一个共享变量时才需要同步，A线程在同步块修改完这个共享变量时，B再进入这个同步块，它能立即发现共享变量的最新值，而且它修改共享变量时，不存在其他线程来捣乱的情况。


**synchronized包的是什么**
学过操作系统都知道，进程/线程同步有很多方式，例如信号量、互斥量，其中还有一种方式就是管程。**管程**就像一个黑盒子，系统提供给我们使用，他能保证同一时刻只有一个进程/线程可以执行管程包裹的代码，管程为我们隐藏了实现的数据结构等细节，我们只需要关注暴露出的接口。  
java程序运行在JVM之上，而JVM本质上就是对计算机的虚拟，那么java系统是否为我们也提供了管程？synchronized就是java实现的管程。

**JVM层面**
synchronized为用户屏蔽了实现细节，其中**进入synchronized**在JVM底层对应**monitorEnter指令**，而**出synchronized**对应**monitorExit指令**。monitor翻译过来就是管程的意思。调用synchronized方法时，编译源码后，字节码文件的方法表标识字段会出现ACC_synchronized，底层仍然会调用上面的两个指令。  
同时，编译器会在以上指令附近插入**内存屏障**，告诉操作系统和CPU硬件，在执行该指令时禁止某些优化，来保证相应的可见性和有序性特性。

直接看以上两个指令，就感觉底层肯定有一个叫monitor的数据结构管理着同步状态。

```markdown
header中hashcode是类似懒加载的模式，对象被创建时在header中的值是0，第一次被调用的时候才会计算出值，后续每次调用都是这个值，当然这是在没重写hashcode方法的前提下。
另外对象刚被创建的时候，header中不一定会存hashcode，比如使用jvm命令禁用偏向锁，又或者在偏向锁期间发生锁批量撤销，都会导致创建的对象直接分配轻量级锁，而轻量级锁的header只存了lock record地址和锁标志位，hashcode只是间接存储。
```



#### Monitor
>[[JVM#Monitor]]

synchronized包裹的内容可以是字符串、class对象、this（synchronized实例方法包裹的是this，而synchronized类方法包裹的是class对象）等。不管它包裹的什么，那一定是一个对象。

类锁一般说的是synchronized（xxx.class）或static synchronized，那么不管通过哪个实例去操作资源类，都会被同步。因为不管class对象还是类方法都是属于类的，每个JVM实例只存在一个的，大家抢的都是这一个，和从哪里访问没有关系。

其实，如果直到了“锁”的原理，就没必要如此分析。

首先记住：**synchronized关联的是monitor结构，而monitor和Object对象绑定**，因此，不严谨的说，所有object对象都能作为“锁”

每个java对象在内存布局中由三部分组成：**对象头**、**实例数据**和**填充数据/对齐填充**。其中对象头又可以分为两部分：**标记字段 mark word** 和 **类型指针**  
mark word的结构不是固定的，是动态变化的，根据结果不同可以分为无锁状态、偏向锁状态、轻量级锁状态、重量级锁状态。  
如果一个对象处于重量级锁状态，那么mark word将具有一个指向重量级锁的指针。

> 重量级锁的创建是延迟的，而且锁升级的出现，主要原因也是为了避免重量级锁的创建。

总结：假设不存在锁升级，一旦线程初次进入synchronized块，将伴随锁（monitor）的创建，并且线程将试图获取这个锁（实现上一般是CAS将owner字段修改为某个线程id）。（重量级锁的叫法大致在，是引入锁升级之后，这里不做区分）  
注意：锁本质上只是一个变量，上锁、抢锁实际含义是CAS争抢“置位操作”，用户能直接看见的是synchronize包裹着对象，其实底层线程争抢对object关联的monitor进行置位操作


#### 从源码看synchronized

有兴趣的，可以看一看JVM对应的C++源码，这里我只进行一些个人总结。  
synchronized是java对管程的一种实现，使用了某种管程模型，这里指出是为了防止固化思维。  
monitor在底层，对应C++定义的objectMonitor。  
每个线程都会被抽象为一个对象（类似java的thread，C++也类似，下面指的线程就是一个被抽象出的对象而不是操作系统层面的线程），每个java对象关联的monitor也是一个对象，不过是C++对象。  
【1】count 记录重入次数（可重入锁的最大特点就是可以**防止多次调用而导致死锁**，非可重入锁通常是使用布尔值01进行标记锁的状态，而可重入锁使用一个计数器变量）  
【2】owner指向拥有该对象的线程  
【3】waitSet 等待队列（wait()调用后，线程被移入该队列，其实就是插入链表队尾，对应java线程的wait状态）  
【4】entryList 同步队列（进入synchronized后并且没有获取到锁，则会进入该队列，对应java线程的block状态）

一个线程进入synchronized后便进行**一次CAS**（CAS（owner,null,cur)试图让自己称为owner），没错，这里强调的就是一次。如果第一次CAS失败则说明抢占失败，通常会进行**自适应自旋（重试）**，如果仍然失败则进入entryList同步队列，并且调用park()阻塞当前线程，底层对应系统调用**将当前线程对象映射到的操作系统线程挂起，并让出CPU**，这一步通常代价比较大，因为涉及系统调用和线程切换。如果成功将owner修改为自己，则开始执行同步代码，并且将count加一。执行完毕将count减一，复位owner，并且唤起entryList阻塞的线程（实现上通常唤醒队头线程，不过如果没抢到还会进入entryList队尾，通常流动性很大，不会出现饥饿）。  
而如果owner线程调用wait，则进入waitSet并阻塞（同样对应park调用），同时让出CPU。只有其他线程调用notify它才会被唤醒，而且唤醒后进入entryList，当owner被复位后，同entryList其他线程进行竞争，当称为owner将从原执行位置继续向下执行。

注意：synchronized阻塞指的通常是synchronized抢占锁失败的行为，即不管互斥锁还是自旋锁指的都是**失败后的处理策略**。

#### 从操作系统看synchronized

**monitor的阻塞部分**底层依赖**操作系统的互斥量（mutex）实现，而**上锁部分**则依赖CPU的**CAS指令**。（LockSupport/unsafe提供的park()和atomic/unsafe提供的CAS底层其实也是这一套东西，只不过拿到明面上来了）

而synchronized的可见性和有序性都是CAS保证的（lock cmpxchg），[volatile的文章说的比较清楚，这里不展开了](https://blog.csdn.net/qq_44793993/article/details/117636156)。而原子性是由锁保证的（操作同步代码之前，需要先过monitor这一关，你不是owner就别想过去）


#### synchronized的优化
jdk1.6之后的优化是：
1. 锁升级机制
2. 锁消除。一些框架采用保守策略，将程序基于线程安全实现，锁消除是一种编译器优化，通过逃逸分析消除部分无必要的同步代码。
   ```markdown
   锁消除是发生在编译器级别的一种锁优化方式。  
有时候我们写的代码完全不需要加锁，却执行了加锁操作。

@Override
public synchronized StringBuffer append(String str) {
    toStringCache = null;
    super.append(str);
    return this;
}

从源码中可以看出，append方法用了synchronized关键词，它是线程安全的。但我们可能仅在线程内部把StringBuffer当作局部变量使用：
package com.leeib.thread;
public class Demo {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        int size = 10000;
        for (int i = 0; i < size; i++) {
            createStringBuffer("Hyes", "为分享技术而生");
        }
        long timeCost = System.currentTimeMillis() - start;
        System.out.println("createStringBuffer:" + timeCost + " ms");
    }
    public static String createStringBuffer(String str1, String str2) {
        StringBuffer sBuf = new StringBuffer();
        sBuf.append(str1);// append方法是同步操作
        sBuf.append(str2);
        return sBuf.toString();
    }
}

代码中createStringBuffer方法中的局部对象sBuf，就只在该方法内的作用域有效，不同线程同时调用createStringBuffer()方法时，都会创建不同的sBuf对象，因此此时的append操作若是使用同步操作，就是白白浪费的系统资源。

这时我们可以通过编译器将其优化，将锁消除，前提是java必须运行在server模式（server模式会比client模式作更多的优化），同时必须开启逃逸分析:

-server -XX:+DoEscapeAnalysis -XX:+EliminateLocks  

其中+DoEscapeAnalysis表示开启逃逸分析，+EliminateLocks表示锁消除。

逃逸分析：比如上面的代码，它要看sBuf是否可能逃出它的作用域？如果将sBuf作为方法的返回值进行返回，那么它在方法外部可能被当作一个全局对象使用，就有可能发生线程安全问题，这时就可以说sBuf这个对象发生逃逸了，因而不应将append操作的锁消除，但我们上面的代码没有发生锁逃逸，锁消除就可以带来一定的性能提升。
   ```
3. 锁粗化。在编译期间将相邻的同步代码块合并成一个大的同步代码块，减少反复申请、释放造成的开销。（即使每次都可以获得锁，那么频繁的操作底层同步队列也将造成不必要的消耗）
   ```markdown
   通常情况下，为了保证多线程间的有效并发，会要求每个线程持有锁的时间尽可能短，但是大某些情况下，一个程序对同一个锁不间断、高频地请求、同步与释放，会消耗掉一定的系统资源，因为锁的请求、同步与释放本身会带来性能损耗，这样高频的锁请求就反而不利于系统性能的优化了，虽然单次同步操作的时间可能很短。锁粗化就是告诉我们任何事情都有个度，有些情况下我们反而希望把很多次锁的请求合并成一个请求，以降低短时间内大量锁请求、同步、释放带来的性能损耗。
   public void doSomethingMethod(){
    synchronized(lock){
        //do some thing
    }
    //这是还有一些代码，做其它不需要同步的工作，但能很快执行完毕
    synchronized(lock){
        //do other thing
    }
}

上面的代码是有两块需要同步操作的，但在这两块需要同步操作的代码之间，需要做一些其它的工作，而这些工作只会花费很少的时间，那么我们就可以把这些工作代码放入锁内，将两个同步代码块合并成一个，以降低多次锁请求、同步、释放带来的系统性能消耗，合并后的代码如下:

public void doSomethingMethod(){
    //进行锁粗化：整合成一次锁请求、同步、释放
    synchronized(lock){
        //do some thing
        //做其它不需要同步但能很快执行完的工作
        //do other thing
    }
}

注意：这样做是有前提的，就是中间不需要同步的代码能够很快速地完成，如果不需要同步的代码需要花很长时间，就会导致同步块的执行需要花费很长的时间，这样做也就不合理了。


另一种需要锁粗化的极端的情况是：

for(int i=0;i<size;i++){
    synchronized(lock){
    }
}

上面代码每次循环都会进行锁的请求、同步与释放，看起来貌似没什么问题，且在jdk内部会对这类代码锁的请求做一些优化，但是还不如把加锁代码写在循环体的外面，这样一次锁的请求就可以达到我们的要求，除非有特殊的需要：循环需要花很长时间，但其它线程等不起，要给它们执行的机会。
synchronized(lock){
    for(int i=0;i<size;i++){
    }
}

   ```
4. 自适应自旋锁。synchronizedCAS占用owner失败后，会进行自旋尝试，这个时间不是固定的，而是**前一次在同一个锁上的自旋时间以及锁的拥有者的状态来决定的**
>自旋锁的开启：  
> JDK1.6中-XX:+UseSpinning开启；  
> -XX:PreBlockSpin=10 为自旋次数；  
> JDK1.7后，去掉此参数，由jvm控制；

同时，用户也可以具有一些优化意识，如：
锁分离。最常见的就是读写分离。
减少不必要的同步代码、减少同步代码大小，减少锁的粒度（例如jdk1.8concurrentHashMap基于synchronized实现分段加锁，将粒度压缩都了每一个桶）、尽量让同步代码短小精悍，减少锁的持有时间。


锁的状态取决于对象头的mark word低两位。

锁升级是单向的（也不一定，和JVM的实现有关）：

创建锁记录(Lock Record) 对象 , 每个线程的栈帧都会包含一个锁记录的结构,内部可以存储锁定对象的Mark Word

Synchronized经过编译，会在同步块的前后分别形成monitorenter和monitorexit这个两个字节码指令。在执行monitorenter指令时，首先要尝试获取对象锁。如果这个对象没被锁定，或者当前线程已经拥有了那个对象锁，把锁的计算器加1，相应的，在执行monitorexit指令时会将锁计算器就减1，当计算器为0时，锁就被释放了。如果获取对象锁失败，那当前线程就要阻塞，直到对象锁被另一个线程释放为止。

#### 特点

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


一个线程进入synchronized后便进行**一次CAS**（CAS（owner,null,cur)试图让自己称为owner），没错，这里强调的就是一次。如果第一次CAS失败则说明抢占失败，通常会进行**自适应自旋（重试）**，如果仍然失败则进入entryList同步队列，并且**调用park()阻塞当前线程，底层对应系统调用将当前线程对象映射到的操作系统线程挂起，并让出CPU**，这一步通常代价比较大，因为涉及系统调用和线程切换。如果成功将owner修改为自己，则开始执行同步代码，并且将count加一。执行完毕将count减一，复位owner，并且唤起entryList阻塞的线程（实现上通常唤醒队头线程，不过如果没抢到还会进入entryList队尾，通常流动性很大，不会出现饥饿）。  
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


借助AQS实现互斥量

```JAVA
class Mutex implements Lock {
    private Syn syn= new Syn();

    @Override
    public void lock() {
        syn.acquire(-1);//参数不被tryAcquire使用
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        syn.acquireInterruptibly(-1);
    }

    @Override
    public boolean tryLock() {
        return syn.tryAcquire(-1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return syn.tryAcquireNanos(-1,unit.toNanos(time));
    }

    @Override
    public void unlock() {
        syn.release(-1);
    }

    @Override
    public Condition newCondition() {
        return newCondition();
    }

    static class Syn extends AbstractQueuedSynchronizer {
        ConditionObject newCondition(){
            return new ConditionObject();
        }

        @Override
        protected boolean tryAcquire(int arg) {
            if(compareAndSetState(0,1)){
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if(getState()==1){
                if(getExclusiveOwnerThread()==Thread.currentThread()){
                    setExclusiveOwnerThread(null);
                    setState(0);
                    return true;
                }
            }
            throw new IllegalMonitorStateException();
        }

        @Override
        protected boolean isHeldExclusively() {
            return getState()==1;
        }
    }
}

    
    
```

【1】创建一个内部类实现AQS（如果想要具有公平和非公平实现，可以另外创建两个内部类，将差异方法空出了，然后让两个内部类再次继承Syn）  
【2】实现Lock接口，并且委托Syn对象去提供实现  
【3】Syn实现了AQS，就是一个AQS对象，因此可以直接调用AQS框架已经提供出来的方法

### 简述AQS原理

AQS队列同步器，它主要管理了两个队列/链表：同步队列和等待队列。并且维护了一个同步状态state。这个state是volatile修饰  
通过**读volatile**可以实现加锁的内存语义，而通过**写volatile**实现解锁的内存语义。  
volatile的写与释放锁具有相同的内存语义，而volatile的读与获取锁具有相同的内存语义。  
根据happens-before规则，**对一个volatile域的写，happens-before于任意后续对这个volatile的读**。

AQS维护了一个基于双向链表的同步队列，当线程未获取到同步状态时，则该线程会被封装成一个节点，CAS插入队尾，同时调用park()陷入阻塞。  
队列的首节点最开始是一个哨兵节点（延迟创建），两个队列外的线程同时去获取state，成功获取state的成为owner，而失败的封装成节点插入队列尾部并阻塞。

一般情况下，同步队列中头结点表示的是**获取到同步状态的线程节点**，当头结点代表的线程释放了state（state=0），此时线程在释放state后还需要唤醒**后继节点**（_队首元素释放节点后，只有后继节点有资格参与和外界的竞争_）去获取state。当有线程获取到state时，需要将自己代表的节点更新为头结点。

> 当前线程成功获取state，那么可能有队列外的线程获取失败，便会被封装入节点进入线程。由于队列中的节点都是延迟创建的，因此如果总是能避免竞争（交替获取）便不会创建任何节点入队。  
> 注意：state有可能是外界线程释放的，也可能是队首节点释放的。最终都会**唤醒头结点的后继节点**，当一个队内节点对应的线程抢占state成功则将自己置为队头元素，相当于变成了新的哨兵，同时将节点指向线程对象的指针置空（相当于线程出队），而一旦state被释放则哨兵（头结点）的后继元素将被唤醒。

这里以aquire为例

```java
public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }

    
    
```

一开始AQS实例的head和tail都是空，在addWaiter时需要初始化，head和tail会共同指向同一个空节点，这个空节点的waitStatus默认值就是0，可以看作哨兵节点。  
其中addWaiter就是一个底层数据结构入队的过程，返回当前已经插入尾部的节点Node的引用，然后acquireQueud使用这个node去执行抢占state和阻塞的逻辑。

当进入acquireQueud时，node的pre就是node空节点，因此可以直接tryAcquire尝试占有state，如果失败说明外部存在竞争。这时前面的head节点的waitStatus会被调整为signal，然后当前节点阻塞。

```java
compareAndSetWaitStatus(pred, ws, Node.SIGNAL);

    
    
```

一旦释放锁（不管是外部还是头部），都会唤醒头部的后继节点，被唤醒后的后继节点如果成功占用锁，那么将会变成新的头结点，并且释放当前头结点

```java
if (h != null && h.waitStatus != 0) //waitStatus=0不会响应，signal则会响应
    unparkSuccessor(h);

    
    
```

另一方面，如果head为-1（signal）那么后面一定有节点，如果head=0（默认值/初始状态），那么后面的节点一定在设置head=-1的路上，并且没有被阻塞的节点，因此不需要额外执行唤醒。  
这保证了，**如果waitStatus<0，则后继一定存在需要被唤醒的节点**。

```java
int ws = node.waitStatus; //头结点释放后会初始化，等同于哨兵——空节点
if (ws < 0)
    compareAndSetWaitStatus(node, ws, 0);

    
    
```

如果是外部释放锁，当前头结点即使是空节点，它的状态也是-1，因此具备唤醒后继节点的资格。如果是头部节点释放锁，同理。如果头部或者外部释放锁，而被外部线程拿到，则**头部的thread引用已经释放，本质上就是一个哨兵节点（同初始状态的空节点）**  
（是否是空节点，主要区别是thread引用是否有值）

考虑一种情况，如果同步队列没有等待节点，即线程总是能够**交替获得state**，因此**每当state释放后，同步队列就没有执行唤醒head后继节点的必要**

```java
shouldParkAfterFailedAcquire(p, node) &&
    parkAndCheckInterrupt()

    
    
```

如果有其他节点入队，则头部waitStatus=0的节点会被再次设置为-1，保证头结点后面的节点不会“死等”下去。前一个方法只有返回true时才会触发park()，即**只有当head（前驱节点）是signal，才能放心进入阻塞状态**，0或者1（cancel）都不可以，因为这会导致节点阻塞后不被唤醒。

对于共享模式，与独占模式主要的不同：自己拿到资源后，**如果还有剩余量，那么会接着唤醒后继节点**（后继接着唤醒后继…以此类推）。而且基于重入的考虑，独占模式下，释放完所有的资源（state=0）时才会唤醒其他线程，而共享模式下，**拥有资源的线程在释放掉部分资源时就可以唤醒后继等待结点**

#### await/signal

ConditionObject是AQS的内部类，每个conditionObject对象都是一个等待队列，只有同步队列队首元素才可以执行await()方法——封装成一个新的节点添加到condition等待队列的队尾，同时通过LockSupport.park()进行阻塞，并且释放state，唤醒同步队列中的后继节点。

> 注意：同步队列的首节点并不会直接加入等待队列，而是把当前线程构封装成一个新的节点并将其加入等待队列中

而当另外一个持有state的线程调用condition的signal方法，会将唤醒在**等待队列**中等待时间最长的节点（**首节点**），在唤醒节点之前，会将等待队列中的节点移动到同步队列的尾部，直到获取到state才会继续恢复执行。

wait和await的实现很相似，都是将线程节点对象在等待队列与同步队列之间移动，并且提供了一些其他特性：awaitNanos超时等待、awaitUninterruptibly()对中断不敏感（线程中断调用后不抛出异常，而是设置中断标志位true）

总结：Condition等待通知的本质就是等待队列 和 同步队列的交互的过程，跟object的wait()/notify()机制一样。Condition是基于同步锁state实现的，而objec是基于monitor实现的

#### 公平与非公平

reentrantLock是Lock接口的实现类，也是基于AQS框架实现的同步组件，可以看作是java代码层面对synchronized的高层次实现。内部有三个内部类，一个是继承了AQS的同步抽象父类syn，另外两个分别基于syn进行了公平与非公平实现。

synchronized默认就是非公平的，可以提供代码执行吞吐量和并发度。  
【1】lock上锁方法中，调用acquire获取锁之前，会先进行一次CAS尝试占用同步状态  
【2】重新tryAcquire方法中，如果发现state空闲则会进行一次CAS尝试占用同步状态。（tryAcquire在模板方法至少被调用了两次）  
以上两次抢占全部失败之后，才会走AQS模板方法的剩余流程。（创建节点、短暂自旋、阻塞）

而公平实现中，仅当队列中没有等待更久的节点时，才会尝试CAS占用（也就是说，只要队列中有其他节点正在排队，则当前线程就必须往后排队，不能插队）  
公平锁对应的同步队列，节点获取同步状态是有严格的顺序要求的，获取公平锁的线程几乎总是需要创建节点和阻塞，导致线程切换频繁、吞吐量下降、并发度下降。

### 同步组件原理简述
[Java并发编程的4个同步辅助类（CountDownLatch、CyclicBarrier、Semphore、Phaser） - looyee - 博客园 (cnblogs.com)](https://www.cnblogs.com/looyee/articles/9921910.html)


同步组件的实现，本质上都是依赖AQS框架，并且实现框架提供了钩子方法

#### semaphore

semaphore信号量，它的名字表明了它的功能——信号灯，因此它的作用更倾向于**通知**，不过二元信号量也可以用于实现互斥关系或前驱关系。  
Semaphore主要逻辑：获取state和释放state，可以看作一个共享锁组件。

```java
compareAndSetState(available, remaining))

    
    
```

#### countdownLatch

countDownLatch锁存器，用于同步一组任务，强制他们等待其他任务执行完毕，相当于jdk中的join函数

典型用法：

```java
CountDownLatch countDownLatch = new CountDownLatch(3);
Runnable runnable = () -> {
    countDownLatch.countDown(); //调用三次，await就可以返回了
};
for (int i = 0; i < 4; i++) {
    new Thread(runnable).start();
}
countDownLatch.await();

    
    
```

将一个程序分为n个互相独立的可解决任务，并创建值为n的CountDownLatch。**当每一个任务完成时，都会在这个锁存器上调用countDown**，被插队的任务调用这个锁存器的await，直至锁存器计数结束

其中**await就是申请一个permit，countDown就是释放一个permit**。创建countDownLatch时，构造函数传入的就是state的值，申请state时（调用await），**只有当state值为0时才能成功申请，否则阻塞**。而countDown就是将state减一。

总结：**创建countDownLatch时，它有若干个state，而调用await的线程将会阻塞直到state的值变成0，而另外一组线程则负责调用countDown将state减少**。

一旦state变成0，那么这个CountDownLatch就算使用完毕了，因此它是不能够被复用的。

#### cyclicBarrier

cyclicBarrier可以达到一种效果，N个线程调用cyclicBarrier.await()进入阻塞（相当于被堵在了一个栅栏处），当N个线程全部调用完毕，则“栅栏打开”，线程集中放行。

```java
CyclicBarrier cyclicBarrier = new CyclicBarrier(5,() -> System.out.println("放行"));
Runnable runnable = () -> {
    try {
        cyclicBarrier.await();//阻塞
    } catch (InterruptedException | BrokenBarrierException e) {
        e.printStackTrace();
    }
};
for (int i = 0; i < 5; i++) {
    new Thread(runnable).start();
}

    
    
```

当第N个线程调用await方法，则N个线程集体放行，并且第N个方法将执行回调函数（其实就是执行传入的runnable接口对应的run方法）

**cyclicBarrier依赖reentrantLock和condition对象实现**，每个cyclicBarrier底层对应一个reentrantLock实例。**可以循环使用，每一代绑定一个generation对象**。当调用reset时，将会将当前屏障设置为已经破坏状态，并且唤醒所有阻塞的线程，并且创建新的generation对象

```java
int index = --count;
if (index == 0) { // 释放屏障
    boolean ranAction = false;
    try {
        final Runnable command = barrierCommand;
        if (command != null)
            command.run();// 在最后一个线程上执行回调任务的run方法
        ranAction = true;
        nextGeneration();// 相当于自动重置
        return 0;
    } finally {
        if (!ranAction)
            breakBarrier(); //出异常，则将当前屏障设置为已破坏状态
    }
}

    
    
```

调用await底层对应count变量减一,当减少到0则唤醒所有的等待线程并重置。（parties保存总屏障数量，count对应剩余屏障数量）

```java
if (!timed)
    trip.await(); //阻塞于lock的condition同步队列
else if (nanos > 0L)
    nanos = trip.awaitNanos(nanos);
```

## 5. AQS

```JAVA
    // 共享模式下等待的标记
    static final Node SHARED = new Node();
    // 独占模式下等待的标记
    static final Node EXCLUSIVE = null;

    // 线程的等待状态 表示线程已经被取消
    static final int CANCELLED =  1;
    // 线程的等待状态 表示后继线程需要被唤醒
    static final int SIGNAL    = -1;
    // 线程的等待状态 表示线程在Condtion上
    static final int CONDITION = -2;

    // 表示下一个acquireShared需要无条件的传播
    static final int PROPAGATE = -3;

之所以初始化为0而不是-1，是为了在release()方法里去区分是否需要唤醒后继节点：
当前线程释放资源之后，去唤醒后继节点时，判断条件是！=0的，也就是说，对于一个没有后继节点的节点(状态为0就说明没有后继节点，因为如果有后继节点的话，前驱节点的状态就会被设置为-1)来说，是不需要去唤醒后继节点的

    /**
     * 链接到等在等待条件上的下一个节点,或特殊的值SHARED,因为条件队列只有在独占模式时才能被访问,
     * 所以我们只需要一个简单的连接队列在等待的时候保存节点,然后把它们转移到队列中重新获取
     * 因为条件只能是独占性的,我们通过使用特殊的值来表示共享模式
     */
    Node nextWaiter;



    /**
     * 如果节点处于共享模式下等待直接返回true
     */
    final boolean isShared() {
        return nextWaiter == SHARED;
    }

    /**
     * 返回当前节点的前驱节点,如果为空,直接抛出空指针异常
     */
    final Node predecessor() throws NullPointerException {
        Node p = prev;
        if (p == null)
            throw new NullPointerException();
        else
            return p;
    }







    public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }


/*

*/

```

```markdown

获取资源失败,则 进入 addWaiter

在 addWaiter(Node mode)方法中,第一步就是创建一个Node对象,将当前线程和独占模式赋给创建的Node对象

判断当前尾结点是否为空,若不为空,尝试CAS将自己设置为尾结点,若成功则返回 node对象; 若设置失败则进入 enq(node)方法中
若尾结点为空,直接进入enq方法

在enq(node)方法中, 先判断tail节点是否存在,若不存在则CAS设置一个空的头结点,然后设置tail等于head
若tail节点存在,则尝试将当前节点设置为尾结点,成功则返回,失败则再一轮循环,直至成功为止



拿到 node对象后,进入 acquireQueued方法, 首先拿到当前 node对象的前一个节点, 若前一个节点是头结点(主要是第一次添加进链表的时候会创建一个空节点当头结点和尾结点)并且当前尝试获取资源成功,那么就将当前node对象设置为头结点,(前一个节点的next设置为null), 返回 interrupted 的值,因为没阻塞前就获得了资源,所以该值为false

若前一个节点不为头结点或获取资源失败,则调用 shouldParkAfterFailedAcquire(前一个节点,当前节点),这个方法的功能是保证当前节点的唤醒是有前驱结点来负责

进入该方法后,首先是拿到前驱节点的状态,若前驱节点的状态为 signal,这表示已经通知了前驱结点要释放资源的时候通知自己,这时候当前线程就可以安心进入阻塞状态.所以直接返回true

若前驱结点的状态 > 0,这表明前驱结点取消了,这时候就要一直往前找,找到一个处于正常等待的节点,并排在它的后边, 然后返回false
若前驱结点的状态 不是 > 0, 那么说明是正常状态的节点,调用CAS,将其状态变为signal,返回false
此处有几种情况需要讨论:
1.若CAS设为signal前,前驱结点状态变化了,怎么办?
结果是失败,刚才说了,进入该方法是第一步就是拿到前驱结点的状态,CAS判断的原始值就是进入该方法时前驱结点的状态,只有状态不变才有可能CAS成功

2.若CAS成功后,当前线程还没有进入阻塞,当前驱结点又刚好释放了,这种情况怎么办?
不急,待会看看前驱结点释放时的行为我们就知道了

3.若CAS成功后,当前节点进入阻塞,这种情况和第2种其实都一样,往下看就知道

只有判断前驱结点状态为signal才可以调用 parkAndCheckInterrupt这个方法,这个方法就是阻塞当前线程
当前线程被唤醒后,就会返回 Thread.interrupted(). 在acquireQueue方法中(这个方法只会返回是否被 interrupt),调用了parkAndCheckInterrupt方法进行阻塞,唤醒后还是一套流程,前驱结点是头结点并且自己能成功拿到资源,那就返回interrupt的值

```

```markdown
在acquireQueued方法中,线程只有两个结果,一个是达到任务运行的条件,另一个是阻塞

若是发现当前轮次无法满足其条件,就会调用shouldParkAfterFailedAcquire方法,
调用该方法还是只有三个结果,一个是返回true,两个是返回false







[Java AQS unparkSuccessor 方法中for循环为什么是从tail开始而不_跳墙网 (tqwba.com)](https://www.tqwba.com/x_d/jishu/297947.html)
在该段方法中，将当前节点置于尾部使用了CAS来保证线程安全，但是请注意：**在if语句块中的代码并没有使用任何手段来保证线程安全！**

也就是说，在高并发情况下，可能会出现这种情况：

(当了尾结点,就是成功入队)
线程A通过CAS进入if语句块之后，发生上下文切换，此时线程B同样执行了该方法，并且执行完毕。然后线程C调用了`unparkSuccessor`方法。

**假如是从头到尾的遍历形式，线程A的next指针此时还是null！也就是说，会出现后续节点被漏掉的情况。**

从头部遍历会出现这种问题的原因我们找到了，最后我们再来说说为什么从尾部遍历不会出现这种问题呢？

其最根本的原因在于：  
`node.prev = t;`先于CAS执行，也就是说，你在将当前节点置为尾部之前就已经把前驱节点赋值了，自然不会出现prev=null的情况
```



```markdown
AQS支持两种获取资源的模式,一种是独占模式,另一种是共享模式

AQS中有个Node类,每个Node对象都包含了节点封装的线程,指向前后的指针,节点的等待状态


首先看获取同步资源的方法 acquire(int arg)

if(!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg)){
	selfInterrupt();
}

如果获取同步资源失败,那么就需要
1.调用addWaiter将当前线程封装成node节点加入等待队列中,返回将该节点返回
如何加入等待队列?
具体实现就是 先获取当前尾结点,判断是否为空:
若队列为空或者通过CAS设置尾结点失败,那么会通过enq()方法死循环,直至设置尾结点成功为止

2.拿到node节点后放进 acquireQueued方法中,该方法只有三种结果:
	1.当前节点的前驱结点是头结点并且尝试获取共享资源成功,那么当前节点将成为头结点,避免线程阻塞.
	  **为什么要按队头到队尾的顺序唤醒呢?**
	  因为获取同步资源成功后,当前节点将会成为头结点,这样一来就可能导致节点丢失的问题,所以必须前节点为头 
      结点才行
	2.如果当前节点的前驱结点不是头结点或者获取共享资源失败,那么将会调用shouldParkAfterFailedAcquire() 
      方法，判断线程能否进行阻塞，当线程能够被阻塞时，将会调用parkAndCheckInterrupt()方法阻塞线程

	  shouldParkAfterFailedAcquire(前驱结点,当前节点),该方法里面分3种情况,以两种结果呈现回来
		  1.只有确认了前驱结点的状态为signal,这意味着当前节点的唤醒已经有结点负责了,可以安心进入阻塞
		  2.若当前节点的前驱结点状态为cancelled,那么就需要不断往前寻找状态不为cancelled的节 点,将自己 
            放在该节点后面,然后返回false
		  3.前驱结点状态不为cancelled,则用CAS将其状态设为signal,该过程可能失败,所以还是返回false

	  当返回true,这意味着当前线程可以进入阻塞,调用parkAndCheckInterrupt,该方法会返回线程的中断标识
	  
	3.如果线程在执行该方法时出现异常,那么就会调用cancelAcquire()方法
	  该方法首先是从当前节点往前遍历,记录下第一个遇到的状态不为cancelled的节点,修改自身状态为cancelled

	  进入判断流程: 若当前节点是尾结点,则通过CAS将尾指针指向刚才记录的节点,若CAS成功,则将刚才记录的节 
      点的后继指针通过CAS设置为null;
	  若当前节点不是尾结点,则判断下当前节点的前驱结点是不是头结点,若不是头结点,则通过CAS将前驱结点的后 
      继指针指向当前节点的后继结点; 若是头结点,则调用unparkSusscessor(node)方法唤醒后继结点,让他来剔 
      除当前节点



释放节点调用release方法,若tryRelease方法成功,则判断一下头结点是否存在并且判断是否存在后继结点也就是其状态是否不等于0,若成立则调用unparkSuccessor唤醒后继结点

unparkSuccessor分为两类,若是非公平锁,则唤醒的是等待队列中从前往后的第一个状态不为cancelled的节点
若是公平锁,那么就唤醒离他最近的第一个状态不为cancelled的节点.这里要注意,若是发现后继结点为null,要从后往前再检验一遍是否真的为null
```



## CAS和锁
### 理解锁

锁是什么？一个变量。线程A看见这个变量已经有主人了，它要么等待、要么去sleep、要么放弃，线程B释放锁就是将这个锁变量的主人重新置空。那么无论是获取锁的操作还是释放锁的操作，本身都是应该是原子的，应该是一个事务！我们平时更关心的是加锁和解锁之间的代码，那么上锁和解锁本身如何保证原子性？我只能说方式有很多，不过主流的实现方案是基于CAS指令。

应用层次的锁，解决的是多个进/线程并发访问同一块内存的问题，而CPU层面的锁解决了多个核心并发访问同一块内存的问题。由于应用层面的锁是对底层的封装与抽象，因此一旦锁获取失败，操作系统都可以通过系统调用挂起一个线程，让出CPU。

> 起初程序是原子的，为了程序实现并发来提升效率，引入了“执行到一半”的第三种状态，而上锁的操作，本质上是使用一个原子指令来将锁变量置位，即保证程序原子性的原理就是**使用一个原子性指令来保证另外一堆非原子性指令的原子性**。

总结，锁就是一个变量，访问一个变量前先抢占锁，这种访问策略也称为悲观锁策略。阻塞和非阻塞主要指的是“抢占锁失败后”的处理策略。阻塞锁底层依赖系统调用（mutex互斥量），非阻塞锁一般都会继续尝试，这种锁也称为自旋锁。（既然都“上锁”了，那么无法上锁肯定是不退出的，尝试失败则退出一般称为tryLock，上锁的中途能够被外界中断则称为lockInterruptibly，这些都是Lock接口提供的行为，synchronized是没有的）

### CAS

CAS是什么？比较与交换，它是一个指令，不管从低级的cpu指令还是高级的代码都能看见它的身影。  
jdk层面的 CAS API 可以由unsafe类提供或者使用JUC的Atomic原子类提供的CAS相关方法。  
**CAS底层实现依赖处理器的指令集（cmpxchg）**，jdk的CAS方法无疑都采用本地实现，处理器的CAS是一条原子指令，也就是说比较和交换整个动作可以一次性完成。

CAS指令集需要三个操作数：需要修改资源的内存地址，预期值，目标值。  
CPU访问内存地址，当资源的实际值等于预期值时，CPU将内存地址上的资源修改为目标值。由于CAS是处理器的单条指令，不会被打断，因此可以保证原子性。

CAS是无阻塞同步的一种解决方案，它可以实现乐观锁。我的理解：**CAS可以完成上锁操作本身**。

> CAS是实现“上锁”的一种方法，也可以使用关中断、testAndSet等

### 乐观锁和悲观锁

什么是悲观锁？  
访问一个资源之前，一定要加锁，否则可能出现读写冲突或者写写冲突等线程安全问题。  
这个锁可以是_自旋锁_——定义一个**锁变量**，CAS自旋去修改这个变量，如果修改失败就一直自旋着，其中上锁成功的线程就相当于进入了同步代码块。  
这个锁也可以是_互斥锁/阻塞锁_——仍然是有一个锁变量，此时不再是无限CAS自旋，而是自旋若干次，如果修改失败就调用阻塞函数/系统调用，将线程阻塞起来，主动放弃CPU——这便是synchronized的基本原理

什么是乐观锁？  
访问一个资源之前，认为没有竞争发生，**不使用锁变量，而是直接CAS修改资源本身**（读操作不需要加锁）。如果失败了如何进行后序处理看具体业务场景。

### 内存语义

CAS不仅可以用于线程同步，而且可以用于线程通信。  
CAS操作同时具有volatile读和volatile写的内存语义，编译器不能对CAS前面和后面的任意指令进行重排序。如果程序运行在多处理器计算机，那么CAS操作被翻译为汇编指令时会加上**lock前缀**  
CAS在x86处理器的大致写法是lock cmpxchg a,b,c 。  
**单核处理器**是没必要写lock前缀的，因为cmpxchg本身是一个原子指令，这意味着执行这条指令时，能够一次性完成一次内存读和内存写，中间不会打断。  
但是多核处理器下，加上lock前缀，意味着：  
【1】将当前处理器缓存行的数据回写内存（写入内存前，通过锁总线，或者锁缓存行的方式保证同步）  
【2】其他核心存储该变量的相应缓存行标记为invalidate（MESI协议）

> 只要某个核心使用了cmpxchg，其他的核心都会停下来（类似自旋），因此多个CPU 核心同时执行这条指令（同一块内存），只有一个核心会成功，其他的将会排队失败。

volatile和CAS可以说是JUC实现的基石  
（注意：cmpxchg不是一个特权指令，不需要切换内核态）

#### MESI缓存一致性协议

（MESI只是缓存一致性协议的一种）  
**Modified已修改 - exclusive独占 - shared 共享 - invalidated 已失效**  
四个状态用于标志缓存行，协议对各个状态的转移做了详细的规定。

**修改**表示cache中的block已经被更新，但是没有更新到内存。  
**失效**表示block中的数据已经失效，不可以读取。（必须从内存读取）  
独占、共享表示block中的数据是一致的（**独占**：数据只存储在一个CPU核心的cache中，其他核心的cache没有该数据，写独占cache时不需要通知其他的核心。）独占状态下，如果有其他核心从内存中读取相同数据到cache中，则独占状态的数据变为共享状态（**共享**：当需要更新cache时，必须先进行一个**广播**，要求其他cache将block中的相同数据标记为无效，然后再更新当前cache的数据）

多个线程（核心）同时读写**同一个缓存行的不同变量**，导致缓存行**频繁失效**的现象称为伪共享。其中一个方案是空间换时间，通过空行填充，让某些变量独占一个缓存行，浪费一部分cache，换来性能的提升

**写缓冲区**：  
CPU缓冲区为了保证数据一致性，遵循MESI缓存一致性协议，**某个CPU更新数据后，需要向其他CPU发出invalidate信号，并且得到其他CPU确认信号后才会进行写缓冲区操作**，但是等待确认的这段时间CPU核心是无效的，因此引入写缓存区——**将数据先写入写缓冲区，等待确认完毕时，再将数据取出并写入本地缓存**。  
（引入写缓冲区后，一个写指令发出后，放入缓冲区后就直接往下执行了，也就是说写操作不是立即生效的）

写缓冲区不是无穷大的，而且处理器有时还是需要等待失效确认的返回（写缓冲区失效的情况），引入**失效队列**：  
【1】对于收到的所有invalidate请求，必须立即返回确认  
【2】invalidate并不会真正执行，而是放入失效队列，在方便的时候才回去执行  
【3】处理器不会发生任何消息给所处理的缓存条目，直到处理invalidate请求  
（相当于使用一个队列，临时存储invalidate请求，收到请求后立刻回复，之后CPU再异步处理这些请求，失效队列的引入导致线程读到**“本应该失效却还没有失效”的脏数据**）

写缓存的引入使得指令看起来是乱序执行的——**写缓冲区和本地缓存行的数据是不一致的**。  
**store forwarding（存储转发）**：当CPU执行读操作时，会从写缓冲区和缓存行中读。  
另一方面，机器指令本身也会被处理器重排序，因为CPU无法确定多线程环境下哪些变量具有相关性（只能保证单线程情况下，重排序不会影响最终结果），但是CPU设计者提供了**内存屏障**供程序员规范CPU行为。

内存屏障是CPU设计者提供给程序员的一组指令，让程序员去约束CPU的行为，**读类型的内存屏障**会使得**失效队列**的invalidate请求立即更新，而**写类型的内存屏障**会使**写缓冲区**的数据立即写入。

> 内存屏障是CPU设计者为程序员提供的一组方法，可以约束CPU的行为，不同的CPU具有不同的内存屏障，相当于为程序员提供相应工具，将保证线程安全的责任交给程序员。  
> 程序员通过使用内存屏障，告诉CPU哪些部分不应该被（重排序）优化，底层就是通过临时禁用失效队列、写缓冲区等实现的。

#### lock前缀

volatile、CAS（synchronized底层也是基于CAS上锁的）被编译为汇编指令后（即时编译器），都会在相应指令前增加一个lock前缀，lock前缀正是这些关键字实现有序性和可见性的基础。  
LOCK前缀在多核处理器中引发两件事  
【1】让当前处理器缓存行的数据回写入主存  
【2】其他核心维护该变量相应的缓存行过期/无效，下次取需要从主存中获取

CPU为了提升效率，通过增加高速缓存来缓解读写内存造成的（CPU计算和访存之间的）速度差，而告诉缓存的最小单位是缓存行，因此CPU读数据都是一块块读的，多核处理机中，每个核心都有自己独立的缓存（L1/L2），各个核心通过**环总线**连接在一起，并且**嗅探**总线上信号，为了保证各个核心缓存行中的数据都是一致的，有两种解决思路（这也是lock前缀执行上的，两种实现方式）。  
【1】锁总线  
执行指令期间，核心发出Lock信号，总线仲裁机构该核心独占总线，而其他核心必须等待，代价很大，非主流方案。  
【2】锁缓存，而且缓存之间需要遵守一个缓存一致性协议  
各个CPU核心都是通过环总线连接在一起的，每个核心都维护自己缓存的状态，一旦某个核心修改了自己缓存的内容，就会通过环总线向其他核心发出信号，其他核心根据MESI协议修改相应的缓存行状态。

**Lock前缀的汇编指令会强制写入主存，也可以避免前后指令的CPU重排序，并且及时让其他核心中的相应缓存行失效（从而利用MESI达到符合预期的效果）**。  
非lock前缀的汇编指令执行写操作时，可能不会立刻生效，因为存在写缓存区，**lock前缀的指令在功能上可以等价内存屏障**，让写操作立刻生效（或者说jvm插入内存屏障，平台通过CPU指令实现对应效果）。

总结：为什么volatile、synchronized、CAS等能保证可见性、有序性，因为它们共同的底层实现lock前缀，满足了MESI缓存一致性协议的触发条件，才使得变量具有缓存一致性。而普通的读写涉及各种优化，如写缓存、失效延迟处理等导致MESI条件无法触发，进而产生一系列数据不一致的问题。

### 特点

这里我们讨论API层面的CAS。因为CAS可以分为CAS修改锁变量和CAS乐观锁，我们这里讨论CAS乐观锁，而这里乐观锁指代CAS自旋乐观锁。  
在竞争不激烈的情况下，CAS可以提高系统的吞吐量——说白了，就是在一段时间内，让CPU多执行用户代码，少执行操作系统代码如（系统调用、切换上下文等）。如果竞争特别激烈，或者同步代码执行时间特别长，那么就使用自旋CAS乐观锁就是白白**浪费CPU资源**——虽然CPU一直在执行用户代码，但是执行循环啥也不干，还不如把CPU让给别人呢——这种情况不如主动申请阻塞、转让CPU给其他线程。

> 当多个核心针对同一内存地址指向CAS指令时，其实他们是在试图修改每个核心自己维护的缓存行，假如两个核心同时同时对同一内存地址执行CAS指令，则他们都会尝试向其他核心发出invalidate，仲裁获胜的核心将先一步发出invalid，失败者则需要对自己的缓存行invalidate，读取胜利者修改后的内存值，CAS指令执行失败。
> 
> 因此**锁并没有消失，只是转嫁到了环总线上的总线仲裁协议上，而多核同时针对一个地址CAS会导致对应的缓存行频繁失效，降低性能，因此CAS不能滥用**

另外，CAS指令提供的总是**一个变量的内存地址**，也就是说乐观锁只能CAS修改某一个变量的值——不如独占锁变量，想怎么修改就怎么修改来的爽快啊。

> 也可以将多个变量包装为一个对象（结构体），通过JUC的atomicReference来实现。当然了，肯定还是加锁更方便

### ABA

这个我想单独聊一聊。  
ABA问题：  
首先，CAS指令的三个参数实际上都是内存地址，比较两个内存地址的值，然后考虑要不要把第一个内存地址的值修改为第三个内存地址的值，既然涉及到寻址，那么**两次寻址之间必然具有时间间隔**，我们只能保证CAS指令执行是原子的，在CPU寻址过程中（三个地址的寻址过程），源地址上的值从A变成B，再变成A是可能的。

造成以上问题的主要原因，是因为我们使用CAS时的逻辑就是:**值相同，就交换**。如果我们的业务禁止ABA问题，我们完全可以将CAS的逻辑更改为：**值+时间戳或版本号 相同，则交换**。

> 严格意义上，ABA不能归于CAS，而是我们“错误”的编码。CAS没有错，因为无论我们传入值还是值+版本号，它看来就是“变量的实际值”，ABA问题更应该被归于业务逻辑范畴。

ABA解决思路就是：CAS输入不但考虑值本身，还附带具有标识意义的字段。例如JUC的atomicStampedReference（额外维护了一个时间戳）、mysql可以维护一个version字段（mybatis plus 提供了乐观锁功能，本质上就是维护额外版本号）

_总结：  
造成ABA问题的不是CAS指令本身，因为它只是一个原子指令，出现ABA问题不是执行CAS的时候，而是CPU为CAS指令加载值的过程中。_

### 写一个自旋锁

不要把自旋锁和乐观锁搞混，一般说乐观锁普遍指的是CAS自旋修改目标变量（不加锁直接修改资源），这里采用循环的方式更改锁变量

自旋锁使用场景：**并发度不高**，**临界代码执行时间不长的场景**。

这里使用计数器count实现可重入效果，如果直接使用布尔值表示状态那么就是不可重入锁，不可重入锁一旦连着调用两次lock()就会死锁，因此推荐使用可重入锁——避免死锁

```
    private AtomicReference<Thread> owner = new AtomicReference<>();//保证内存可见性
    private int count=0;//重入次数

    
```

上锁

```
    public void lock(){
        Thread cur = Thread.currentThread();
        if(owner.get()==cur){
            //重入
            count++;
            return;
        }
        //自旋获取锁:如果当前owner等于期望值null，则CAS设置为cur
        while (!owner.compareAndSet(null,cur)){
            System.out.println("自旋");
        }
    }

    
```

解锁

```
    public void unlock(){
        Thread cur =Thread.currentThread();
        //持有该锁的线程才可以解锁
        if(owner.get()==cur){
            if(count>0){
                count--;
            }else {
                owner.set(null);
            }
        }

    }


    
```

Volatile修饰数组或者集合只能保证指针（地址）的内存可见性，如果某一个线程将指针修改指向，其他线程可以立即知道。  
**而元素修改的可见性应该使用atomic变量来保证——atomicArray和atomicReference**  
atomic类封装了unsafe类提供的CAS、putObjectVolatile等方法，便于非框架开发用户（普通程序员）的使用。

**保证内存可见性的arr[i]=newValue**

```
public final void set(int i, E newValue) { 
    unsafe.putObjectVolatile(array, checkedByteOffset(i), newValue);
}

    
```

**CAS:如果arr[i]等于期望值except，则更新为update**

```
private boolean compareAndSetRaw(long offset, E expect, E update) {
    return unsafe.compareAndSwapObject(array, offset, expect, update);
}

    
```

JUC的atomic就是基于unsafe类实现的，而且封装了unsafe类的各种方法，其中原子操作就是基于CAS自旋实现的（乐观锁）  
Atomic调用unsafe方法，unsafe方法调用C语言，C语言再调用汇编语言，最终生成一条CPU指令cmpxchg，因此CAS是具有原子性，不会被打断。

atomicLong在**高并发**下，大量线程同时竞争更新同一个原子变量（因为long是64位的，底层会被拆分为两个32位，分别为高位和低位），CAS成功率小，失败的线程尝试自旋，会浪费很多CPU资源。（atomicDouble也有这样的问题）  
**LongAdder是jdk8引入的，是对atomicLong的改进，在高并发场景更加高效。**

LongAdder可以概括成这样：内部核心数据value分离成一个数组(Cell)，每个线程访问时,通过哈希等算法映射到其中一个数字进行计数，而最终的计数结果，则为这个数组的求和累加。

简单来说就是**将一个值分散成多个部分，每个线程操作这个值的一部分，最后值相加，在并发的时候就可以分散压力（只有在线程哈希冲突时才会产生竞争）**，性能有所提高。


## ThreadLocal
>[(59条消息) ThreadLocal-hash冲突与内存泄漏_为自己勇敢的博客-CSDN博客_threadlocal 哈希冲突](https://blog.csdn.net/Summer_And_Opencv/article/details/104632272)
>[ThreadLocal与引用类型相关知识点整理 - 东北小狐狸 - 博客园 (cnblogs.com)](https://www.cnblogs.com/hellxz/p/java-threadlocal.html)
>[(59条消息) Java8-ThreadLocal的Lambda构造方式：withInitial_Zebe的博客-CSDN博客_threadlocal.withinitial](https://blog.csdn.net/zebe1989/article/details/82692551)
>[【JUC剖析】ThreadLocal类 详解 - 在下右转，有何贵干 - 博客园 (cnblogs.com)](https://www.cnblogs.com/codderYouzg/p/14094442.html)

### 为什么使用弱引用？

从表面上看，发生内存泄漏，是因为Key使用了弱引用类型。但其实是因为整个Entry的key为null后，没有主动清除value导致。很多文章大多分析ThreadLocal使用了弱引用会导致内存泄漏，但为什么使用弱引用而不是强引用？

为了处理非常大和生命周期非常长的线程，哈希表使用弱引用作为 key。


下面我们分两种情况讨论：

-   key 使用强引用：引用的ThreadLocal的对象被回收了，但是ThreadLocalMap还持有ThreadLocal的强引用，如果没有手动删除，ThreadLocal不会被回收，导致Entry内存泄漏。
-   key 使用弱引用：引用的ThreadLocal的对象被回收了，由于ThreadLocalMap持有ThreadLocal的弱引用，即使没有手动删除，ThreadLocal也会被回收。value在下一次ThreadLocalMap调用set,get，remove的时候会被清除。  
    比较两种情况，我们可以发现：由于ThreadLocalMap的生命周期跟Thread一样长，如果都没有手动删除对应key，都会导致内存泄漏，但是使用弱引用可以多一层保障：弱引用ThreadLocal不会内存泄漏，对应的value在下一次ThreadLocalMap调用set,get,remove的时候会被清除。

因此，ThreadLocal内存泄漏的根源是：由于ThreadLocalMap的生命周期跟Thread一样长，如果没有手动删除对应key的value就会导致内存泄漏，而不是因为弱引用。

综合上面的分析，我们可以理解ThreadLocal内存泄漏的前因后果，那么怎么避免内存泄漏呢？

-   每次使用完ThreadLocal，都调用它的remove()方法，清除数据。

在使用线程池的情况下，没有及时清理ThreadLocal，不仅是内存泄漏的问题，更严重的是可能导致业务逻辑出现问题。所以，使用ThreadLocal就跟加锁完要解锁一样，用完就清理。



作用：
提供线程内的 **局部变量**，**不同的线程** 之间 **不会相互干扰**  
这种变量在 **线程的生命周期内** 起作用，  
减少 **同一个线程** 内 **多个函数或组件** 之间一些 **公共变量** 的 **传递复杂度**


1.  **`线程并发`**:  
    在 **多线程并发** 的场景下
2.  **`传递数据`**:  
    我们可以通过ThreadLocal在 **同一线程**，**不同组件** 中传递 **公共变量**
3.  **`线程隔离`**:  
    每个线程的变量都是 **独立** 的，**不会相互影响**

1.  传递数据 ： 保存每个线程绑定的数据，在需要的地方可以直接获取, 避免参数直接传递带来的代码耦合问题
    
2.  线程隔离 ： 各线程之间的数据相互隔离却又具备并发性，避免同步方式带来的性能损失

使用ThreadLocal的典型场景正如上面的数据库连接管理，线程会话管理等场景，只适用于独立变量副本的情况，如果变量为全局共享的，则不适用在高并发下使用。

适用于无状态，副本变量独立后不影响业务逻辑的高并发场景。如果如果业务逻辑强依赖于副本变量，则不适合用ThreadLocal解决，需要另寻解决方案。

## java线程同步方法

什么是线程同步?
多个线程并发访问共享数据时,保证共享数据在同一时刻只能被规定个数的线程使用(一般一条,信号量一些).互斥是实现同步的一种手段
临界区,互斥量,信号量都是互斥的实现方式

1. 互斥同步方法
   synchronized和reentranlock
2. 非阻塞同步方法
   乐观锁
3. 无同步方法
   采用线程本地存储ThreadLocal：如果一段代码中所需要的数据必须与其他代码共享，那就看看这些共享数据的代码是否能保证在同一个线程中执行完，如果能保证，就可以把共享数据的可见范围限制在同一个线程之内，这样，无需同步也能保证线程之间不出现数据争用




## 线程池
