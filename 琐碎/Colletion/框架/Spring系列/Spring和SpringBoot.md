# 注解
#### @EnableConfigurationProperties
[关与 @EnableConfigurationProperties 注解 - 简书 (jianshu.com)](https://www.jianshu.com/p/7f54da1cb2eb)
如果一个配置类只配置@ConfigurationProperties注解，而没有使用@Component，那么在IOC容器中是获取不到properties 配置文件转化的bean。说白了 @EnableConfigurationProperties 相当于把使用 @ConfigurationProperties 的类进行了一次注入。  
测试发现 @ConfigurationProperties 与 @EnableConfigurationProperties 关系特别大。




#### IOC和DI
![[Pasted image 20220330174707.png]]



#### AOP
OOP编程思想可以解决⼤多数的代码重复问题，但是有⼀些情况是处理不了的，⽐如下⾯的在顶级⽗类 Animal中的多个⽅法中相同位置出现了重复代码，OOP就解决不了
![[Pasted image 20220330175351.png]]

横切逻辑代码存在什么问题： 横切代码重复问题 横切逻辑代码和业务代码混杂在⼀起，代码臃肿，维护不⽅便


AOP出场，AOP独辟蹊径提出横向抽取机制，将横切逻辑代码和业务逻辑代码分析


代码拆分容易，那么如何在不改变原有业务逻辑的情况下，悄⽆声息的把横切逻辑代码应⽤到原有的业 务逻辑中，达到和原来⼀样的效果，这个是⽐较难的

**AOP在解决什么问题** 
在不改变原有业务逻辑情况下，增强横切逻辑代码，根本上解耦合，避免横切逻辑代码重复
**为什么叫做⾯向切⾯编程** 
「切」：指的是横切逻辑，原有业务逻辑代码我们不能动，只能操作横切逻辑代码，所以⾯向横切逻辑 
「⾯」：横切逻辑代码往往要影响的是很多个⽅法，每⼀个⽅法都如同⼀个点，多个点构成⾯，有⼀个 ⾯的概念在⾥⾯

#### 循环依赖
循环依赖其实就是循环引⽤，也就是两个或者两个以上的 Bean 互相持有对⽅，最终形成闭环。⽐如A 依赖于B，B依赖于C，C⼜依赖于A

注意，这⾥不是函数的循环调⽤，是对象的相互依赖关系。循环调⽤其实就是⼀个死循环，除⾮有终结 条件。 Spring中循环依赖场景有： 构造器的循环依赖（构造器注⼊） Field 属性的循环依赖（set注⼊） 其中，构造器的循环依赖问题⽆法解决，只能拋出 BeanCurrentlyInCreationException 异常，在解决 属性循环依赖时，spring采⽤的是提前暴露对象的⽅法。


spring维护了一个临时的map缓存，存放未完成依赖注入的bean引用，和一个工厂bean的map缓存。当spring为某个bean的依赖（创建并）注入bean时，如果发现未完成bean缓存能够找到对应引用说明存在循环引用，则直接从工厂bean缓存拿到循环依赖bean的工厂bean，通过getObject注入依赖（此时还是半成品，全部返回后就是依赖关系完整的bean）后返回。当bean创建结束后，两个互相依赖的bean的字段中都保存一个完成的bean对象的引用。


单例bean通过setXxx或者@Autowired进⾏循环依赖

场景：A < == > B

Spring 的循环依赖的理论依据基于 Java 的引⽤传递，当获得对象的引⽤时，对象的属性是可以延 后设置的，但是构造器必须是在获取引⽤之前 

Spring通过setXxx或者@Autowired⽅法解决循环依赖其实是通过提前暴露⼀个ObjectFactory对 象来完成的，简单来说ClassA在调⽤构造器完成对象初始化之后，在调⽤ClassA的setClassB⽅法 之前就把ClassA实例化的对象通过ObjectFactory提前暴露到Spring容器中


Spring容器初始化ClassA通过构造器初始化对象后提前暴露到Spring容器,也就是三级缓存

ClassA调⽤setClassB⽅法，Spring⾸先尝试从容器中获取ClassB，此时ClassB不存在Spring 容器中。

Spring容器初始化ClassB，同时也会将ClassB提前暴露到Spring容器

ClassB调⽤setClassA⽅法，Spring从容器中获取ClassA ，因为第⼀步中已经提前暴露了 ClassA，因此可以获取到ClassA实例，然后将其升级到二级缓存
beanB创建完成后，放到一级缓存
ClassA通过spring容器依次从三级缓存，二级缓存，一级缓存查找ClassB，完成了对象初始化操作，移到一级缓存

这样ClassA和ClassB都完成了对象初始化操作，解决了循环依赖问题

#### springboot自动装配原理

Springboot的主配置类标注了springBootApplication注解，其主要由两个作用。  
一个是标注这是一个**应用的中心配置类**，框架识别该注解后将其作为一个配置组件放入IOC容器。另一个是**启动自动配置功能**，框架将springBootApplication及其子包下的组件扫描进入IOC容器（元信息存入beanDefinition容器），同时还会扫描META-INF/spring.factories文件，并将其中每一项都加载进一个properties对象，其中key是自动配置（enableAutoConfiguration）类的全限定类名。最后将properties的值存入IOC容器。

【1】springBoot启动时，通过 **@enableAutoConfiguration注解找到jar包中spring.factories配置文件中所有的自动配置类 ，并对其加载。因此springBoot启动时，会加载大量自动配置类 。
【2】自动配置类以autoConfiguration结尾来命名，**XXXautoConfiguration配置类**中通过**EnableConfigurationProperties注解**取得**XXXProperties类**在全局配置文件中配置的属性。  
【3】XXXProperties类通过@configurationProperties注解与全局配置文件中对应的属性绑定。**给容器中自动配置类添加组件的时候，会从XXXproperties类中获取某些属性。我们就可以在配置文件中指定这些属性的值**