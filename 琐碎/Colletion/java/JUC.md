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

```markdown
header中hashcode是类似懒加载的模式，对象被创建时在header中的值是0，第一次被调用的时候才会计算出值，后续每次调用都是这个值，当然这是在没重写hashcode方法的前提下。
另外对象刚被创建的时候，header中不一定会存hashcode，比如使用jvm命令禁用偏向锁，又或者在偏向锁期间发生锁批量撤销，都会导致创建的对象直接分配轻量级锁，而轻量级锁的header只存了lock record地址和锁标志位，hashcode只是间接存储。
```

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
