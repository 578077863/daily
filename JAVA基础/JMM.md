# JMM

## 琐碎

i++：如果是static i的话，JVM字节码指令把静态变量和常量1都放到操作数栈，修改后的值再存入静态变量i；如果局部变量 i 的话，就是调用iinc在局部变量槽上更新



System.out.println()用了 synchronized（）使得工作内存直接到主存读取数据



在线程数少于cpu核数的时候,CAS划算

## 特性

原子性

可见性

有序性



## 指令重排序

为什么有这个东西?

因为现代CPU支持多级指令流水线,例如支持同时执行 取指令 - 指令译码 - 执行指令 - 内存访问 - 数据写回的处理器 , 就可以称为 五级指令流水线,变相提高指令地吞吐率





## volatile 原理

volatile的底层实现原理是**内存屏障**，Memory Barrier（Memory Fence）

- 对 volatile 变量的写指令后会加入写屏障
- 对 volatile 变量的读指令前会加入读屏障





## happens-before

规定了对共享变量的写操作对其他线程的读操作可见

* 线程解锁m之前对变量的写,对于接下来对m加锁的其他线程对该变量的读可见
* 线程对 volatile变量的写,对接下来其他线程对该变量的读可见
* 线程start前对变量的写,对该线程开始后对该变量的读可见
* 线程结束前对变量的写,对其他线程得知他结束后的读可见(比如其他线程调用 t1.isAlive() 或 t1.join()等待他结束)
* 线程t1打断t2前对变量的写,对于其他线程得知t2被打断后对变量的读可见(通过 t2.interrupted , t2.isInterrupted)
* 对变量默认值(0,null,false)的写,对其他线程对该变量的读可见
* 具有传递性![image-20211115184456634](images/JMM/image-20211115184456634.png)





## ----------------------------------------------------------------------------------------------------------------------------



## as-if-serial规则

as-if-serial语义的意思指：不管怎么重排序（编译器和处理器为了提高并行度），（单线程）程序的执行结果不能被改变。编译器、runtime和处理器都必须遵守as-if-serial语义。
为了遵守as-if-serial语义，编译器和处理器不会对存在数据依赖关系的操作做重排序，因为这种重排序会改变执行结果。但是，如果操作之间不存在数据依赖关系，这些操作可能被编译器和处理器重排序



## happens-before（先行发生）规则

### 定义

JMM可以通过happens-before关系向程序员提供跨线程的内存可见性保证（如果A线程的写操作a与B线程的读操作b之间存在happens-before关系，尽管a操作和b操作在不同的线程中执行，但JMM向程序员保证a操作将对b操作可见）。具体的定义为：

1. 如果一个操作happens-before另一个操作，那么第一个操作的执行结果将对第二个操作可见，而且第一个操作的执行顺序排在第二个操作之前。

2. 两个操作之间存在happens-before关系，并不意味着Java平台的具体实现必须要按照happens-before关系指定的顺序来执行。**如果重排序之后的执行结果，与按happens-before关系来执行的结果一致，那么JMM允许这种重排序**。



### 六大规则

1. **程序顺序规则**：一个线程中的每个操作，happens-before于该线程中的任意后续操作。
2. **监视器锁规则**：对一个锁的解锁，happens-before于随后对这个锁的加锁。
3. **volatile变量规则**：对一个volatile域的写，happens-before于任意后续对这个volatile域的读。
4. **传递性规则**：如果A happens-before B，且B happens-before C，那么A happens-before C。
5. **start()规则**：如果线程A执行操作ThreadB.start()（启动线程B），那么A线程的ThreadB.start()操作happens-before于线程B中的任意操作。
6. **join()规则**：如果线程A执行操作ThreadB.join()并成功返回，那么线程B中的任意操作happens-before于线程A从ThreadB.join()操作成功返回。



### as-if-serial规则和happens-before规则的区别

1. as-if-serial语义保证**单线程**内程序的执行结果不被改变，happens-before关系保证**正确同步的多线程**程序的执行结果不被改变。
2. as-if-serial语义给编写单线程程序的程序员创造了一个幻觉：单线程程序是按程序的顺序来执行的。happens-before关系给编写正确同步的多线程程序的程序员创造了一个幻觉：正确同步的多线程程序是按happens-before指定的顺序来执行的。
3. as-if-serial语义和happens-before这么做的目的，都是为了在不改变程序执行结果的前提下，尽可能地提高程序执行的并行度。
