# JAVA基础

## 基本数据类型

### 1.基本数据类型有哪些

java提供了8种基本数据类型,其中分为6,1,1(六种数字类型[四个整数型,两个浮点型],一种字符类型,一种布尔型)

这里采用横向比较:

* 数据类型(都是有符号的,以二进制补码表示的整数)

  > byte: 8位
  >
  > short:16
  >
  > int:32
  >
  > long:64

  这样就意味着数据类型超出范围后会从最小值开始,那有什么处理方案? 这里我的思路是如果要求100%精确,转成long,否则考虑用布隆过滤器或者bitmap之类的数据结构来存储

### 2.包装器类型

首先提出疑问: 为什么要使用包装器类型 ,其有哪些?

> Boolean，Character，Integer，Byte，Short，Long，Float，Double
>
> 因为有了包装器类型的话,一个数字就可以看做是一个对象,从而赋予它其他属性和功能



那接下来的问题是: 啥是装箱和拆箱,其实现方法?

> 装箱：自动将基本数据类型转换为包装器类型。
>
> 拆箱：自动将包装器类型转换为基本数据类型。



装箱的实现方式:

> * 使用构造器(这种方式就没办法利用缓存 , Integer的缓存是 -128 ~ 127)
> * Integer.valueOf(int)
> * 自动装箱:Integer i=8;



拆箱的实现方式:

> * 使用包装类的intValue()方法,eg: i a=i.intValue();
> * 自动拆箱



哪些地方触发自动拆装箱 : 

​	[int 和 integer ：装箱和拆箱的过程，会用到什么方法，你觉得这个会对性能有影响吗，原因是什么（百度一面） - 云+社区 - 腾讯云 (tencent.com)](https://cloud.tencent.com/developer/article/1690009)

1. 往集合类中存基本数据类型

2. 包装类型与基本数据类型进行大小的比较,包装类型自动拆箱(通过反编译看到)

3. 包装类型的运算

4. 三目运算符的使用

   >这其实是三目运算符的语法规范：当第二，第三位操作数分别为基本类型和对象时，其中的对象就会拆箱为基本类型进行操作。

   ```java
   boolean flag = true;
   Integer i = 0;
   int j = 1;
   int k = flag ? i : j;
   
   boolean flag = true;
   Integer i = Integer.valueOf(0);
   int j = 1;
   int k = flag ? i.intValue() : j;
   ```

5. 函数参数与返回值



**缓存机制**

为什么 Integer的缓存范围是[-128,127] ?

>因为这个范围的数字是最被广泛使用的。在程序中，第一次使用Integer的时候也需要一定的额外时间来初始化这个缓存。



性能方面: 对速度和内存有一定影响的



### 3. 逻辑运算符 和 三元运算符

- &: 逻辑与

- &&：短路与

  >当符号左边是false时，& 继续执行符号右边的运算，&&不再执行符号右边的运算

- |： 逻辑或
- ||：短路或
- ! ：逻辑非
- ^ : 逻辑异或



三元表达式

> 一: 格式：(条件表达式)?表达式1:表达式2
> 1.条件表达式为 true ,运算后的结果是表达式1
> 2.条件表达式为 false ,运算后的结果是表达式2
>
> 二: 表达式1和表达式2位同种类型
>
> 三: 三元运算符与if-else的联系与区别：
> 1.三元运算符可简化为 if-else 语句
> 2.三元运算符要求必须返回一个结果
> 3.if后的代码块可有多个语句
> 4.如果程序可以使用三元运算符，又可以使用if-else结构，那么优先选择三元运算符。原因：简单，执行效率高。



### 4. throw 和 throws 的区别？

throw:

在方法内抛出某种异常对象,如果异常对象是非RuntimeException ,则需要在方法申明时加上该异常的抛出,即 throws 语句 或在方法体内 try catch 处理该异常,若不对该异常进行处理,则编译报错. 在 try-catch 过程中,如果捕捉到该异常,则throw异常语句后面的语句块都不执行,直接执行catch中的语句块

throws：

方法的定义上使用 throws 表示这个方法可能抛出某种异常
需要由方法的调用者进行异常处理

### 5.  Java异常处理中try-catch-finally代码块与return语句

[(27条消息) Java异常处理中try，catch，finally代码块与return语句_rolling_kitten的博客-CSDN博客](https://blog.csdn.net/rolling_kitten/article/details/105737923)



从下面这段代码看,只要finally有 return 语句 ,那么这个方法的结束就是在 finally的return语句执行后

这里提出个问题:如果catch里面有return语句，请问finally的代码还会执行吗?如果会，请问是在return前还是return后？

答案从上面的链接找

```java
public static void main(String[] args) {
        System.out.println(testFinally());
    }
    public static int testFinally(){
        int i=0;
        {
            try {
                System.out.println(10/i);
                i=10;
                return i;
            } catch (Exception e) {
                i=20;
                return i;
            }
            finally {
                i=30;
                System.out.println("finally代码块执行了");
                return i;
            }
        }

        // ans = 30
```



### 6. final，finally，finalize

[Java中final、finally、finalize的区别与用法 - smart_hwt - 博客园 (cnblogs.com)](https://www.cnblogs.com/smart-hwt/p/8257330.html)

final

> final是java中的关键字,下面分为几点来介绍:
>
> 对于加上 final的类,意味着该类无法被其他类所继承,也就是没有子类,String就是例子
>
> 变量加上final就意味着成为常量
>
> *方法加上final就代表只能使用,无法重载这个得查查*

finally

> 是java的一种异常处理机制,无论是否出现异常都会执行finally中的代码,所以一般非内存资源的关闭都是放在finally中

finalize

> Java中的方法名,在垃圾收集器将对象从内存中清除前所要做的工作.
>
> 何时调用? 在垃圾收集器判断这个对象已经没被引用时调用该对象的finalize()方法,但只有第一次调用有效(也就是第一次调用可以自救成功),**第二次要清除该对象不会再调用该方法**

### 7. == 和equals，深拷贝和浅拷贝



### 8. 两个for循环，如何跳出

## String

### 1. String 不可变性的好处

1. 便于实现String常量池

   > 该常量池的实现使得不同字符串变量都可以指向池中的同一个字符串,大大减少 heap空间

2. 避免网络安全问题

   > 由于字符串的不可变性,其指向对象的值是无法改变的,这就避免了其值被黑客修改

3. 保证了在多线程环境下的并发安全问题

   > 同一个字符串实例可以被多个线程共用,不用担心线程安全问题而使用同步

4. 避免本地安全性问题

   > 原本要加载的类被修改成其他,可能会对jvm或数据库造成影响

5. 加快字符串处理速度

   > 因为字符串是不可变的，所以在它创建的时候hashcode就被缓存了，不需要重新计算。这就使得字符串很适合作为Map中的键，字符串的处理速度要快过其它的键对象。这就是HashMap中的键往往都使用字符串。



### 2. string，stringBuilder(jdk1.0)，StringBuffer(jdk1.5)区别

1. 可变性与不可变性方面

   > string是不可变
   >
   > 后两个是可变

2. 线程安全方面

   > 前两个是线程安全的,string是由于其不可变性,stringBuilder的线程安全是由于公共方法都是同步的
   >
   > StringBuffer是线程不安全的

3. 字符串连接符 '+'在内部用的是stringBuilder类



### 3. String和byte的转换

1. 构造器

   ```java
   String s=new String(bytes, StandardCharsets.UTF_8)
   ```

2. 用String.getBytes()方法将字符串转换为byte数组，通过String构造函数将byte数组转换成String

   注意：这种方式使用平台默认字符集

   ```java
   //string 转 byte[]
   
   String str = "Hello";
   
   byte[] srtbyte = str.getBytes();
   
   // byte[] 转 string
   
   String res = new String(srtbyte);
   
   System.out.println(res);
   
   
   
   //当然还有可以设定编码方式
   的
   
   String str = "hello";
   
   byte[] srtbyte = null;
   
   try {
   
   	srtbyte = str.getBytes("UTF-8");
   
   	String res = new String(srtbyte,"UTF-8");
   
   	System.out.println(res);
   
       } catch (UnsupportedEncodingException e) {
   
   	// TODO Auto-generated catch block
   
   	e.printStackTrace();
   
       }
   
   ```

   



## 容器

### 1. ArrayList 和 LinkedList 的区别

1. 数据结构方面

   > ArrayList是实现了基于动态数组的数据结构，LinkedList是基于链表结构
   >
   > 对于随机访问(的get和set方法)，ArrayList要优于LinkedList，因为LinkedList要不断移动指针查找。
   >
   > 对于新增和删除操作(add和remove)，LinkedList比较占优势，因为ArrayList要移动数据。

2. 浪费空间方面

   > ArrayList的空间浪费是在 数组的结尾留有一定的空闲位置,而LinkedList则是每一个元素都需要消耗一定的空间

3. 扩容方面

   > LinkedList 是链表结构,不需要扩容

   ArrayList扩容机制 : 

   添加元素时会使用 ensureCapacityInternal()方法来保证容量足够,如果不够,需要使用 grow()方法进行扩容,新容量大小为原来容量的1.5倍,底层使用**Arrays.copyOf()这个方法来拷贝原数组,该方法是浅拷贝**



### 2. fail-fast(快速失败机制)

当迭代集合的过程中发现该集合在结构上发生改变(增,删)时,就有可能发生 fail-fast,即抛出ConcurrentModificationException异常

这里举 ArrayList 为例子:

增删就不说了,这里要讲的是 在使用 Iterator 遍历 集合的过程中,发生 fail-fast的情况

单线程环境:

```java
public static void main(String[] args) {
    List<String> list = new ArrayList<>();
    for (int i = 0 ; i < 10 ; i++ ) {
        list.add(i + "");
    }
    Iterator<String> iterator = list.iterator();
    int i = 0 ;
    while(iterator.hasNext()) {
        if (i == 3) {
             list.remove(3);
        }
        System.out.println(iterator.next());
        i ++;
    }
} 
```

这里使用 集合的remove()方法为什么就会发生fail-fast呢?



下面这段代码,每一个方法都用到了checkForComodification();来检查expectedModCount == modCount是否成立,直接用 ArrayList的remove方法删除一个元素,是会造成 modCount更新,这样一来 内部类Itr的expectedModCount没有跟着更新自然就造成了两个数不等于,从而抛出异常.

这里的解决办法是: 调用 Itr自己的remove方法,为什么这个方法就没问题了呢? 该方法虽然也是调用 ArrayList的remove方法,但在最后是会更新 expectedModCount 的值,使其和当前 modCount的值一样,这样就不会出现两值不相等的情况从而抛出异常了

(调用了集合类的remove()方法，modCount在remove()中是被重新赋值了的。expectedModCount在迭代器的remove()方法里被重新赋值ModCount保证的相等成立,这句话也可以)

```java
    /**
     * An optimized version of AbstractList.Itr
     */
    private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;
 
        public boolean hasNext() {
            return cursor != size;
        }
 
        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) elementData[lastRet = i];
        }
 
        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();
 
            try {
                ArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
 
        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = ArrayList.this.size;
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            while (i != size && modCount == expectedModCount) {
                consumer.accept((E) elementData[i++]);
            }
            // update once at end of iteration to reduce heap write traffic
            cursor = i;
            lastRet = i - 1;
            checkForComodification();
        }
 
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }
```



### 3. fast-safe(安全失败)

使用java并发包(java.util.concurrent)中的类来代替 ArrayList 和hashMap。
比如使用 CopyOnWriterArrayList代替 ArrayList， CopyOnWriterArrayList在是使用上跟 ArrayList几乎一样， CopyOnWriter是写时复制的容器(COW)，在读写时是线程安全的。该容器在对add和remove等操作时，并不是在原数组上进行修改，而是将原数组拷贝一份，在新数组上进行修改，待完成后，才将指向旧数组的引用指向新数组，所以对于 CopyOnWriterArrayList在迭代过程并不会发生fail-fast现象。但 CopyOnWrite容器只能保证数据的最终一致性，不能保证数据的实时一致性。
对于HashMap，可以使用ConcurrentHashMap， ConcurrentHashMap采用了锁机制，是线程安全的。在迭代方面，ConcurrentHashMap使用了一种不同的迭代方式。在这种迭代方式中，当iterator被创建后集合再发生改变就不再是抛出ConcurrentModificationException，取而代之的是在改变时new新的数据从而不影响原有的数据 ，iterator完成后再将头指针替换为新的数据 ，这样iterator线程可以使用原来老的数据，而写线程也可以并发的完成改变。即迭代不会发生fail-fast，但不保证获取的是最新的数据。
