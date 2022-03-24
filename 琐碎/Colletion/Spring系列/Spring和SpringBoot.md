# 注解
## @EnableConfigurationProperties
[关与 @EnableConfigurationProperties 注解 - 简书 (jianshu.com)](https://www.jianshu.com/p/7f54da1cb2eb)
如果一个配置类只配置@ConfigurationProperties注解，而没有使用@Component，那么在IOC容器中是获取不到properties 配置文件转化的bean。说白了 @EnableConfigurationProperties 相当于把使用 @ConfigurationProperties 的类进行了一次注入。  
测试发现 @ConfigurationProperties 与 @EnableConfigurationProperties 关系特别大。




#### springboot自动装配原理

Springboot的主配置类标注了springBootApplication注解，其主要由两个作用。  
一个是标注这是一个**应用的中心配置类**，框架识别该注解后将其作为一个配置组件放入IOC容器。另一个是**启动自动配置功能**，框架将springBootApplication及其子包下的组件扫描进入IOC容器（元信息存入beanDefinition容器），同时还会扫描META-INF/spring.factories文件，并将其中每一项都加载进一个properties对象，其中key是自动配置（enableAutoConfiguration）类的全限定类名。最后将properties的值存入IOC容器。

【1】springBoot启动时，通过 **@enableAutoConfiguration注解找到jar包中spring.factories配置文件中所有的自动配置类 ，并对其加载。因此springBoot启动时，会加载大量自动配置类 。
【2】自动配置类以autoConfiguration结尾来命名，**XXXautoConfiguration配置类**中通过**EnableConfigurationProperties注解**取得**XXXProperties类**在全局配置文件中配置的属性。  
【3】XXXProperties类通过@configurationProperties注解与全局配置文件中对应的属性绑定。**给容器中自动配置类添加组件的时候，会从XXXproperties类中获取某些属性。我们就可以在配置文件中指定这些属性的值**