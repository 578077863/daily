
## 对象创建方式
```java
        People people = new People();
        //1. 通过new关键字
        //People people = new People();

        //2. 通过Class类的newInstance调用无参构造函数创建对象
        //People people = (People) Class.forName("javabase.StringTest.java").newInstance();

        //3.通过构造器
        //Constructor<People> constructor = People.class.getConstructor(int.class);
        //People people = constructor.newInstance(2);


        //4.clone  继承Object的clone方法,实现浅拷贝,使用的类必须继承Cloneable接口; 重写clone变为深拷贝
        //People people = new People(2);
        //People clone = (People) people;
        //clone.age = 3;
        //System.out.println(people.age);

//        //5.反序列化
//        People people = new People(2);
//
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//
//        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
//
//        objectOutputStream.writeObject(people);
//
//        byte[] bytes = byteArrayOutputStream.toByteArray();
//
//        System.out.println(bytes);

```

## 文件IO
>[(53条消息) java文件创建、RandomAccessFile的使用_渴望飞的鱼的博客-CSDN博客](https://blog.csdn.net/qq_36411874/article/details/71077072)
>[Java中的文件操作（一）RandomAccessFile - 无语的风 - 博客园 (cnblogs.com)](https://www.cnblogs.com/myFavoriteBlog/p/6004347.html)



## 代理模式
>[轻松理解 Java 静态代理/动态代理 - 阿dun - 博客园 (cnblogs.com)](https://www.cnblogs.com/aduner/p/14646877.html#jdk%E5%8E%9F%E7%94%9F%E5%8A%A8%E6%80%81%E4%BB%A3%E7%90%86)
### 静态代理
```java

package javabase;

/**
 *
 *@author:ZJF
 *@Date 2022-02-23-18:44
 *@description:
 */
public class Proxy implements Rent{

    private Landlord landlord;

    public Proxy(){}

    public Proxy(Landlord landlord){
        this.landlord = landlord;
    }
    @Override
    public void rent() {
        seeHouse();
        landlord.rent();
        fare();
    }

    //看房
    public void seeHouse(){
        System.out.println("带房客看房");
    }
    //收中介费
    public void fare(){
        System.out.println("收中介费");
    }
}


interface Rent{
    public void rent();
}


class Landlord implements Rent{

    @Override
    public void rent() {
        System.out.println("房屋出租");
    }
}


class Client{
    public static void main(String[] args) {

        //房东要租房
        Landlord landlord = new Landlord();
        //中介帮助房东
        Proxy proxy = new Proxy(landlord);
        //客户找中介
        proxy.rent();
    }
}
```

### 动态代理


```java

package javabase;
/**
 @Description
 @author ZJF
 @create 2022-02-23-18:44
 @version */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 *@author:ZJF
 *@Date 2022-02-23-18:44
 *@description:
 */

class Landlord implements Rent{

    @Override
    public void rent() {
        System.out.println("看房子");
    }
}

interface Rent{
    public void rent();
}

class RentInvocationHandler implements InvocationHandler{

    private Rent rent;

    public RentInvocationHandler(Rent rent){
        this.rent = rent;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        seeHouse();
        Object result = method.invoke(rent, args);
        fare();
        return result;
    }


    public Object getProxy(){
        return java.lang.reflect.Proxy.newProxyInstance(this.getClass().getClassLoader(),rent.getClass().getInterfaces(),this);
    }
    //看房
    public void seeHouse(){
        System.out.println("带房客看房");
    }
    //收中介费
    public void fare(){
        System.out.println("收中介费");
    }
}

public class Client {
    public static void main(String[] args) {
        Landlord landlord = new Landlord();
        //代理实例的调用处理程序
        RentInvocationHandler pih = new RentInvocationHandler(landlord);
        Rent proxy = (Rent)pih.getProxy(); //动态生成对应的代理类！
        proxy.rent();
    }
}
```

上述代码的核心关键是Proxy.newProxyInstance方法，该方法会根据指定的参数动态创建代理对象。

它三个参数的意义如下：

1.  `loader`，指定代理对象的类加载器
2.  `interfaces`，代理对象需要实现的接口，可以同时指定多个接口
3.  `handler`，方法调用的实际处理者，代理对象的方法调用都会转发到这里

`Proxy.newProxyInstance`会返回一个实现了指定接口的代理对象，对该对象的所有方法调用都会转发给`InvocationHandler.invoke()`方法。

因此，在`invoke()`方法里我们可以加入任何逻辑，比如修改方法参数，加入日志功能、安全检查功能等等等等……


显而易见，对于静态代理而言，我们需要**手动编写代码**让**代理**实现**抽象角色**的接口。

而在动态代理中，我们可以让程序在运行的时候**自动在内存中创建**一个实现**抽象角色接口**的代理，而不需要去单独定义这个类，代理对象是在程序运行时产生的，而不是编译期。
>对于从Object中继承的方法，JDK Proxy会把`hashCode()`、`equals()`、`toString()`这三个非接口方法转发给`InvocationHandler`，其余的Object方法则不会转发。








```java

public class RentMethodInterceptor implements MethodInterceptor {
    private Object target;//维护一个目标对象
    public RentMethodInterceptor(Object target) {
        this.target = target;
    }
    //为目标对象生成代理对象
    public Object getProxyInstance() {
        //工具类
        Enhancer en = new Enhancer();
        //设置父类
        en.setSuperclass(target.getClass());
        //设置回调函数
        en.setCallback(this);
        //创建子类对象代理
        return en.create();
    }
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("看房");
        // 执行目标对象的方法
        Object returnValue = method.invoke(target, objects);
        System.out.println("中介费");
        return null;
    }
}

class Landlord {
    public void rent() {
        System.out.println("房屋出租");
    }
}

class Client {
    public static void main(String[] args) {
        Landlord target = new Landlord();
        System.out.println(target.getClass());
        //代理对象
        Landlord proxy = (Landlord) new RentMethodInterceptor(target).getProxyInstance();
        System.out.println(proxy.getClass());
        //执行代理对象方法
        proxy.rent();
    }
}
```


>对于从Object中继承的方法，CGLIB代理也会进行代理，如`hashCode()`、`equals()`、`toString()`等，但是`getClass()`、`wait()`等方法不会，因为它是final方法，CGLIB无法代理。



其实CGLIB和JDK代理的思路大致相同

上述代码中，通过CGLIB的`Enhancer`来指定要代理的目标对象、实际处理代理逻辑的对象。

最终通过调用`create()`方法得到代理对象，**对这个对象所有非final方法的调用都会转发给`MethodInterceptor.intercept()`方法**。

在`intercept()`方法里我们可以加入任何逻辑，同JDK代理中的`invoke()`方法

通过调用`MethodProxy.invokeSuper()`方法，我们将调用转发给原始对象，具体到本例，就是`Landlord`的具体方法。CGLIG中`MethodInterceptor`的作用跟JDK代理中的`InvocationHandler`很类似，都是方法调用的中转站。



CGLIB是通过继承的方式来实现动态代理的，有继承就不得不考虑final的问题。我们知道final类型不能有子类，所以CGLIB不能代理final类型，遇到这种情况会抛出类似如下异常：
`java.lang.IllegalArgumentException: Cannot subclass final class cglib.HelloConcrete`
同样的，final方法是不能重载的，所以也不能通过CGLIB代理，遇到这种情况不会抛异常，而是会跳过final方法只代理其他方法。



### 尾声
动态代理是[Spring AOP](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop)(Aspect Orient Programming, 面向切面编程_)的实现方式，了解动态代理原理，对理解Spring AOP大有帮助。

-   如spring等这样的框架，要增强具体业务的逻辑方法，不可能在框架里面去写一个静态代理类，太蠢了，只能按照用户的注解或者xml配置来动态生成代理类。
-   业务代码内，当需要增强的业务逻辑非常通用（如:添加log，重试，统一权限判断等）时，使用动态代理将会非常简单，如果每个方法增强逻辑不同，那么静态代理更加适合。
-   使用静态代理时，如果代理类和被代理类同时实现了一个接口，当接口方法有变动时，代理类也必须同时修改，代码将变得臃肿且难以维护。



## @interface

场景:为了理解@interface使用  
1.@interface自定义注解  
 <1>@interface自定义注解自动继承了java.lang.annotation.Annotation接口,由编译程序自动完成其他细节。  
 <2>在定义注解时,不能继承其他的注解或接口。  
 <3>使用@interface来声明一个注解  
 1>.每一个方法实际上是声明了一个配置参数,  
 2>.方法的名称就是参数的名称,  
 3>.返回值类型就是参数的类型,(返回值类型只能是**基本类型、Class、String、enum**)  
 4>.可以通过default来声明参数的默认值

 2.举例说明,分别作用在类,方法,属性上的注解  

```java

<1>.作用在属性上注解
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FiledAnnotation {
	 String value() default "GetFiledAnnotation";   
}


<2>.作用在方法上注解
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MethodAnnotation {
	String name() default "MethodAnnotation";   
    String url() default "https://www.cnblogs.com";
}

<3>.作用在类上注解
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TypeAnnotation {
	
	String value() default "Is-TypeAnnotation";
	
}

<4>.使用自定义注解
@TypeAnnotation(value = "doWork")
public class Worker {
 
	@FiledAnnotation(value = "CSDN博客")
	private String myfield = "";
 
	@MethodAnnotation()
	public String getDefaultInfo() {
		return "do the getDefaultInfo method";
	}
 
	@MethodAnnotation(name = "百度", url = "www.baidu.com")
	public String getDefineInfo() {
		return "do the getDefineInfo method";
	}
}

<5>.测试自定义注解
public class TestMain {
	
	public static void main(String[] args) throws Exception {
		
        Class cls = Class.forName("com.zbz.annotation.pattern3.Worker");
        Method[] method = cls.getMethods();
        /**判断Worker类上是否有TypeAnnotation注解*/
        boolean flag = cls.isAnnotationPresent(TypeAnnotation.class);
        /**获取Worker类上是TypeAnnotation注解值*/
        if (flag) {
        	TypeAnnotation typeAnno = (TypeAnnotation) cls.getAnnotation(TypeAnnotation.class);
        	System.out.println("@TypeAnnotation值:" + typeAnno.value());
        }
        
        /**方法上注解*/
        List<Method> list = new ArrayList<Method>();
        for (int i = 0; i < method.length; i++) {
            list.add(method[i]);
        }
        
        for (Method m : list) {
        	MethodAnnotation methodAnno = m.getAnnotation(MethodAnnotation.class);
            if (methodAnno == null)
                continue;
            System.out.println( "方法名称:" + m.getName());
            System.out.println("方法上注解name = " + methodAnno.name());
            System.out.println("方法上注解url = " + methodAnno.url());
        }
        /**属性上注解*/
        List<Field> fieldList = new ArrayList<Field>();
        for (Field f : cls.getDeclaredFields()) {// 访问所有字段
        	FiledAnnotation filedAno = f.getAnnotation(FiledAnnotation.class);
        	System.out.println( "属性名称:" + f.getName());
        	System.out.println("属性注解值FiledAnnotation = " + filedAno.value());
        } 
	}
}
```





## 查看JVM内存使用情况
>[查看JVM内存使用状况 - 玄同太子 - 博客园 (cnblogs.com)](https://www.cnblogs.com/zhi-leaf/p/10629033.html)