## Java 语言的特点
面向对象（封装，继承，多态）；
平台无关性（ Java 虚拟机实现平台无关性）；
支持多线程（而 Java 语言却提供了多线程支持）；；
支持网络编程并且很方便（ Java 语言诞生本身就是为简化网络编程设计的，因此 Java 语言不仅支持网络编程而且很方便）；
编译与解释并存；



### 为什么说 Java 语言“解释与编译并存”

高级编程语言按照程序的执行方式分为编译型和解释型两种。简单来说，编译型语言是指编译器针对特定的操作系统将源代码一次性翻译成可被该平台执行的机器码；解释型语言是指解释器对源程序逐行解释成特定平台的机器码并立即执行。比如，你想阅读一本英文名著，你可以找一个英文翻译人员帮助你阅读， 有两种选择方式，你可以先等翻译人员将全本的英文名著（也就是源码）都翻译成汉语，再去阅读，也可以让翻译人员翻译一段，你在旁边阅读一段，慢慢把书读完。
Java 语言既具有编译型语言的特征，也具有解释型语言的特征，因为 Java 程序要经过先编译，后解释两个步骤，由 Java 编写的程序需要先经过编译步骤，生成字节码（\*.class 文件），这种字节码必须由 Java 解释器来解释执行。因此，我们可以认为 Java 语言编译与解释并存。

## 比较 JVM 和 JDK 以及 JRE

### JVM

Java 虚拟机（JVM）是运行 Java 字节码的虚拟机。
JVM 有针对不同系统的特定实现（Windows，Linux，macOS），目的是使用相同的字节码，它们都会给出相同的结果。



#### 什么是字节码?采用字节码的好处是什么?

在 Java 中，JVM 可以理解的代码就叫做**字节码**（即扩展名为 .class 的文件），它不面向任何特定的处理器，只面向虚拟机。Java 语言通过字节码的方式，在一定程度上解决了传统解释型语言执行效率低的问题，同时又保留了解释型语言可移植的特点。所以 Java 程序运行时比较高效，而且，由于字节码并不针对一种特定的机器，因此，Java 程序无须重新编译便可在多种不同操作系统的计算机上运行。
![[Pasted image 20220206184110.png]]


我们需要格外注意的是 .class->机器码 这一步。在这一步 JVM 类加载器首先加载字节码文件，然后通过解释器逐行解释执行，这种方式的执行速度会相对比较慢。而且，有些方法和代码块是经常需要被调用的(也就是所谓的热点代码)，所以后面引进了 JIT 编译器，而 JIT 属于运行时编译。当 JIT 编译器完成第一次编译后，其会将字节码对应的机器码保存下来，下次可以直接使用。而我们知道，机器码的运行效率肯定是高于 Java 解释器的。这也解释了我们为什么经常会说 Java 是编译与解释共存的语言。 HotSpot 采用了惰性评估(Lazy Evaluation)的做法，根据二八定律，消耗大部分系统资源的只有那一小部分的代码（热点代码），而这也就是 JIT 所需要编译的部分。JVM 会根据代码每次被执行的情况收集信息并相应地做出一些优化，因此执行的次数越多，它的速度就越快。JDK 9 引入了一种新的编译模式 AOT(Ahead of Time Compilation)，它是直接将字节码编译成机器码，这样就避免了 JIT 预热等各方面的开销。JDK 支持分层编译和 AOT 协作使用。但是 ，AOT 编译器的编译质量是肯定比不上 JIT 编译器的。

### JDK和JRE

JDK 是 Java Development Kit 缩写，它是功能齐全的 Java SDK。它拥有 JRE 所拥有的一切，还有编译器（javac）和工具（如 javadoc 和 jdb）。它能够创建和编译程序。 JRE 是 Java 运行时环境。它是运行已编译 Java 程序所需的所有内容的集合，包括 Java 虚拟机（JVM），Java 类库，java 命令和其他的一些基础构件。但是，它不能用于创建新程序。 如果你只是为了运行一下 Java 程序的话，那么你只需要安装 JRE 就可以了。如果你需要进行一些 Java 编程方面的工作，那么你就需要安装 JDK 了。但是，这不是绝对的。有时，即使您不打算在计算机上进行任何 Java 开发，仍然需要安装 JDK。例如，如果要使用 JSP 部署 Web 应用程序，那么从技术上讲，您只是在应用程序服务器中运行 Java 程序。那你为什么需要 JDK 呢？因为应用程序服务器会将 JSP 转换为 Java servlet，并且需要使用 JDK 来编译 servlet。



## Java 基本类型有哪几种，各占多少位？

Java 中有 8 种基本数据类型，分别为：

1. 6 种数字类型 ：`byte`、`short`、`int`、`long`、`float`、`double`
2. 1 种字符类型：`char`
3. 1 种布尔型：`boolean`。
![[Pasted image 20220206190701.png]]

另外，对于 `boolean`，官方文档未明确定义，它依赖于 JVM 厂商的具体实现。逻辑上理解是占用 1 位，但是实际中会考虑计算机高效存储因素。

**注意：**

1.  Java 里使用 `long` 类型的数据一定要在数值后面加上 **L**，否则将作为整型解析。
    
2.  `char a = 'h'`char :单引号，`String a = "hello"` :双引号。
    

这八种基本类型都有对应的包装类分别为：`Byte`、`Short`、`Integer`、`Long`、`Float`、`Double`、`Character`、`Boolean` 。

包装类型不赋值就是 `Null` ，而基本类型有默认值且不是 `Null`。

另外，这个问题建议还可以先从 JVM 层面来分析。

基本数据类型直接存放在 Java 虚拟机栈中的局部变量表中，而包装类型属于对象类型，我们知道对象实例都存在于堆中。相比于对象类型， 基本数据类型占用的空间非常小。

> 《深入理解 Java 虚拟机》 ：局部变量表主要存放了编译期可知的基本数据类型 **（boolean、byte、char、short、int、float、long、double）**、**对象引用**（reference 类型，它不同于对象本身，可能是一个指向对象起始地址的引用指针，也可能是指向一个代表对象的句柄或其他与此对象相关的位置）。


## Java 泛型，类型擦除
Java 泛型（generics）是 JDK 5 中引入的一个新特性, 泛型提供了编译时类型安全检测机制，该机制允许程序员在编译时检测到非法的类型。泛型的本质是参数化类型，也就是说所操作的数据类型被指定为一个参数。

Java 的泛型是伪泛型，这是因为 Java 在运行期间，所有的泛型信息都会被擦掉，这也就是通常所说类型擦除 。
```java
List<Integer> list = new ArrayList<>();

list.add(12);
//这里直接添加会报错
list.add("a");
Class<? extends List> clazz = list.getClass();
Method add = clazz.getDeclaredMethod("add", Object.class);
//但是通过反射添加，是可以的
add.invoke(list, "kl");

System.out.println(list);

```

泛型一般有三种使用方式:泛型类、泛型接口、泛型方法。

**1.泛型类**：
```java
//此处T可以随便写为任意标识，常见的如T、E、K、V等形式的参数常用于表示泛型
//在实例化泛型类时，必须指定T的具体类型
public class Generic<T> {

    private T key;

    public Generic(T key) {
        this.key = key;
    }

    public T getKey() {
        return key;
    }
}


//如何实例化泛型类：
Generic<Integer> genericInteger = new Generic<Integer>(123456);

```

**2.泛型接口** ：
```java
public interface Generator<T> {
    public T method();
}


//实现泛型接口，不指定类型：
class GeneratorImpl<T> implements Generator<T>{
    @Override
    public T method() {
        return null;
    }
}

//实现泛型接口，指定类型：
class GeneratorImpl implements Generator<String>{
    @Override
    public String method() {
        return "hello";
    }
}

```

**3.泛型方法** ：
```java
public static <E> void printArray(E[] inputArray) {
    for (E element : inputArray) {
        System.out.printf("%s ", element);
    }
    System.out.println();
}

//使用：
// 创建不同类型数组： Integer, Double 和 Character
Integer[] intArray = { 1, 2, 3 };
String[] stringArray = { "Hello", "World" };
printArray(intArray);
printArray(stringArray);

```

**常用的通配符为： T，E，K，V，？**

-   ？ 表示不确定的 java 类型
    
-   T (type) 表示具体的一个 java 类型
    
-   K V (key value) 分别代表 java 键值中的 Key Value
    
-   E (element) 代表 Element



## == 和 equals() 的区别

对于基本数据类型来说，==比较的是值。对于引用数据类型来说，==比较的是对象的内存地址。

> 因为 Java 只有值传递，所以，对于 == 来说，不管是比较基本数据类型，还是引用数据类型的变量，其本质比较的都是值，只是引用类型变量存的值是对象的地址。

**`equals()`** 作用不能用于判断基本数据类型的变量，只能用来判断两个对象是否相等。`equals()`方法存在于`Object`类中，而`Object`类是所有类的直接或间接父类。

`Object` 类 `equals()` 方法：
```java
public boolean equals(Object obj) {
     return (this == obj);
}

```

`equals()` 方法存在两种使用情况：

-   **类没有覆盖 `equals()`方法** ：通过`equals()`比较该类的两个对象时，等价于通过“==”比较这两个对象，使用的默认是 `Object`类`equals()`方法。
    
-   **类覆盖了 `equals()`方法** ：一般我们都覆盖 `equals()`方法来比较两个对象中的属性是否相等；若它们的属性相等，则返回 true(即，认为这两个对象相等)。

```java
public class test1 {
    public static void main(String[] args) {
        String a = new String("ab"); // a 为一个引用
        String b = new String("ab"); // b为另一个引用,对象的内容一样
        String aa = "ab"; // 放在常量池中
        String bb = "ab"; // 从常量池中查找
        if (aa == bb) // true
            System.out.println("aa==bb");
        if (a == b) // false，非同一对象
            System.out.println("a==b");
        if (a.equals(b)) // true
            System.out.println("aEQb");
        if (42 == 42.0) { // true
            System.out.println("true");
        }
    }
}

```

-   `String` 中的 `equals` 方法是被重写过的，因为 `Object` 的 `equals` 方法是比较的对象的内存地址，而 `String` 的 `equals` 方法比较的是对象的值。
    
-   当创建 `String` 类型的对象时，虚拟机会在常量池中查找有没有已经存在的值和要创建的值相同的对象，如果有就把它赋给当前引用。如果没有就在常量池中重新创建一个 `String` 对象。


## hashCode() 和 equals()

面试官可能会问你：“你重写过 `hashcode` 和 `equals`么，为什么重写 `equals` 时必须重写 `hashCode` 方法？”

**1)hashCode()介绍:**

`hashCode()` 的作用是获取哈希码，也称为散列码；它实际上是返回一个 int 整数。这个哈希码的作用是确定该对象在哈希表中的索引位置。`hashCode()`定义在 JDK 的 `Object` 类中，这就意味着 Java 中的任何类都包含有 `hashCode()` 函数。另外需要注意的是： `Object` 的 hashcode 方法是本地方法，也就是用 c 语言或 c++ 实现的，该方法通常用来将对象的 内存地址 转换为整数之后返回。

散列表存储的是键值对(key-value)，它的特点是：能根据“键”快速的检索出对应的“值”。这其中就利用到了散列码！（可以快速找到所需要的对象）

**2)为什么要有 hashCode？**

我们以“`HashSet` 如何检查重复”为例子来说明为什么要有 hashCode？

当你把对象加入 `HashSet` 时，`HashSet` 会先计算对象的 hashcode 值来判断对象加入的位置，同时也会与其他已经加入的对象的 hashcode 值作比较，如果没有相符的 hashcode，`HashSet` 会假设对象没有重复出现。但是如果发现有相同 hashcode 值的对象，这时会调用 `equals()` 方法来检查 hashcode 相等的对象是否真的相同。如果两者相同，`HashSet` 就不会让其加入操作成功。如果不同的话，就会重新散列到其他位置。（摘自我的 Java 启蒙书《Head First Java》第二版）。这样我们就大大减少了 equals 的次数，相应就大大提高了执行速度。

**3)为什么重写 `equals` 时必须重写 `hashCode` 方法？**

我们以“**类的用途**”来将“hashCode() 和 equals()的关系”分2种情况来说明。

**1. 第一种 不会创建“类对应的散列表”**

这里所说的“不会创建类对应的散列表”是说：我们不会在HashSet, Hashtable, HashMap等等这些本质是散列表的数据结构中，用到该类。例如，不会创建该类的HashSet集合。

在这种情况下，该类的“hashCode() 和 equals() ”没有半毛钱关系的！ ​ 这种情况下，equals() 用来比较该类的两个对象是否相等。而hashCode() 则根本没有任何作用，所以，不用理会hashCode()。

**2. 第二种 会创建“类对应的散列表”**

这里所说的“会创建类对应的散列表”是说：我们会在HashSet, Hashtable, HashMap等等这些本质是散列表的数据结构中，用到该类。例如，会创建该类的HashSet集合。

在这种情况下，该类的“hashCode() 和 equals() ”是有关系的： ​ 1)、如果两个对象相等，那么它们的hashCode()值一定相同。 ​ **这里的相等是指，通过equals()比较两个对象时返回true。** ​ 2)、如果两个对象hashCode()相等，它们并不一定相等。 ​ 因为在散列表中，hashCode()相等，即两个键值对的哈希值相等。然而哈希值相等，并不一定能得出键值对相等。补充说一句：“两个不同的键值对，哈希值相等”，这就是哈希冲突。

此外，在这种情况下。若要判断两个对象是否相等，除了要覆盖equals()之外，也要覆盖hashCode()函数。否则，equals()无效。 例如，创建Person类的HashSet集合，必须同时覆盖Person类的equals() 和 hashCode()方法。 ​ 如果单单只是覆盖equals()方法。我们会发现，equals()方法没有达到我们想要的效果。
```markdown
重写hashcode后，equals()生效了，HashSet中没有重复元素。
比较p1和p2，我们发现：它们的hashCode()相等，通过equals()比较它们也返回true。所以，p1和p2被视为相等。
比较p1和p4，我们发现：虽然它们的hashCode()相等；但是，通过equals()比较它们返回false。所以，p1和p4被视为不相等。
```


## 重载和重写的区别
重载就是同样的一个方法能够根据输入数据的不同，做出不同的处理

重写就是当子类继承自父类的相同方法，输入数据一样，但要做出有别于父类的响应时，你就要覆盖父类方法

### 重载
发生在同一个类中（或者父类和子类之间），方法名必须相同，参数类型不同、个数不同、顺序不同，**方法返回值和访问修饰符可以不同。**
![[Pasted image 20220206191102.png]]
综上：重载就是同一个类中多个同名方法根据不同的传参来执行不同的逻辑处理。


### 重写
重写**发生在运行期**，是子类对父类的允许访问的方法的实现过程进行重新编写。

1.  **返回值类型、方法名、参数列表必须相同**，抛出的异常范围小于等于父类，访问修饰符范围大于等于父类。
    
2.  如果父类方法访问修饰符为 `private/final/static` 则子类就不能重写该方法，但是被 static 修饰的方法能够被再次声明。
    
3.  构造方法无法被重写
    

综上：重写就是子类对父类方法的重新改造，外部样子不能改变，内部逻辑可以改变
![[Pasted image 20220206191133.png]]
**方法的重写要遵循“两同两小一大”**

-   “两同”即方法名相同、形参列表相同；
    
-   “两小”指的是子类方法返回值类型应比父类方法返回值类型更小或相等，子类方法声明抛出的异常类应比父类方法声明抛出的异常类更小或相等；
    
-   “一大”指的是子类方法的访问权限应比父类方法的访问权限更大或相等。
    

⭐️ 关于 **重写的返回值类型** 这里需要额外多说明一下，上面的表述不太清晰准确：如果方法的返回类型是 void 和基本数据类型，则返回值重写时不可修改。但是如果方法的返回值是引用类型，重写时是可以**返回该引用类型的子类**的。




## 深拷贝和浅拷贝

## 面向对象和面向过程的区别

## 成员变量与局部变量的区别

## 面向对象三大特性是什么。并解释这三大特性

## **`String`、`StringBuffer` 和 `StringBuilder` 的区别**

## Java 异常
不会问的特别细。经常的问法是异常可以分为哪几种，然后你答了可检查异常和不可检查异常以后，会让你举例可检查异常有哪些，不可检查有哪些。然后，异常的代码要会写，有一场字节的面试，直接让我写一个把异常捕获了然后抛出去的代码。

## 序列化和反序列化

## 反射
面试官可能会问你什么是反射，它的优缺点是什么，有哪些应用场景。

## List、Set、 Map 的区别


## `ArrayList` 和 `LinkedList` 的区别
答清楚每个分别采用什么数据结构，对比相应的优点和缺点。


## 比较 `HashSet`、`LinkedHashSet` 和 `TreeSet` 三者的异同


## HashMap 多线程操作导致死循环问题
jdk 1.8 后解决了这个问题，但是还是不建议在多线程下使用 `HashMap`,因为多线程下使用 `HashMap` 还是会存在其他问题比如数据丢失。并发环境下推荐使用 `ConcurrentHashMap` 。

## HashMap 的长度为什么是 2 的幂次方


## **`HashMap`、`HashTable`、以及 `ConcurrentHashMap` 的区别。**【⭐⭐⭐⭐⭐】：现在面试的超高频考点。当面试官问到这个问题的时候，展现你背面试八股文能力的机会来了。你可以展开去讲在 Java7 和 Java8 中 `HashMap` 分别采用什么数据结构，为什么 Java8 把之前的`头插法`改成了`尾插法`，怎样实现`扩容`，为什么`负载因子`是 `0.75`，为什么要用`红黑树`等等一系列的东西。只要面试官不打断我，我在这个知识点上能背到面试官下班




