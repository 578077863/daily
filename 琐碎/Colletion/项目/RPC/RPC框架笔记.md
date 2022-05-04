服务注册：在服务提供方启动的时候，将对外暴露的接口注册到注册中心之中，注册中 心将这个服务节点的 IP 和接口保存下来。

服务订阅：在服务调用方启动的时候，去注册中心查找并订阅服务提供方的 IP，然后缓 存到本地，并用于后续的远程调用。

为什么不使用 DNS？
既然服务发现这么“厉害”，那是不是很难实现啊？其实类似机制一直在我们身边，我们回 想下服务发现的本质，就是完成了接口跟服务提供者 IP 的映射。那我们能不能把服务提供 者 IP 统一换成一个域名啊，利用已经成熟的 DNS 机制来实现？

如果我们用 DNS 来实现服务发现，所有的服务提供者节点都配置在了同一个域名下，调用 方的确可以通过 DNS 拿到随机的一个服务提供者的 IP，并与之建立长连接，这看上去并没 有太大问题，但在我们业界为什么很少用到这种方案呢？不知道你想过这个问题没有，如果 没有，现在可以停下来想想这样两个问题：

1. 如果这个 IP 端口下线了，服务调用者能否及时摘除服务节点呢？ 
2. 如果在之前已经上线了一部分服务节点，这时我突然对这个服务进行扩容，那么新上线 的服务节点能否及时接收到流量呢？

这时你可能会想，我是不是可以加一个负载均衡设备呢？将域名绑定到这台负载均衡设备 上，通过 DNS 拿到负载均衡的 IP。这样服务调用的时候，服务调用方就可以直接跟 VIP 建立连接，然后由 VIP 机器完成 TCP 转发


这个方案确实能解决 DNS 遇到的一些问题，但在 RPC 场景里面也并不是很合适，原因有 以下几点：

搭建负载均衡设备或 TCP/IP 四层代理，需求额外成本； 
请求流量都经过负载均衡设备，多经过一次网络传输，会额外浪费些性能； 
负载均衡添加节点和摘除节点，一般都要手动添加，当大批量扩容和下线时，会有大量 的人工操作和生效延迟； 
我们在服务治理的时候，需要更灵活的负载均衡策略，目前的负载均衡设备的算法还满 足不了灵活的需求





在使用 RPC 框架的时候，我们要确保被调用的服务的业务逻辑是 幂等的，这样我们才能考虑根据事件情况开启 RPC 框架的异常重试功能。这一点你要格外 注意，这算是一个高频误区了。





## RpcReferenceBean
```java

package com.mini.rpc.consumer;

import com.mini.rpc.provider.registry.RegistryFactory;
import com.mini.rpc.provider.registry.RegistryService;
import com.mini.rpc.provider.registry.RegistryType;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class RpcReferenceBean implements FactoryBean<Object> {

    private Class<?> interfaceClass;

    private String serviceVersion;

    private String registryType;

    private String registryAddr;

    private long timeout;

    private Object object;

    @Override
    public Object getObject() throws Exception {
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    public void init() throws Exception {
        RegistryService registryService = RegistryFactory.getInstance(this.registryAddr, RegistryType.valueOf(this.registryType));
        this.object = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcInvokerProxy(serviceVersion, timeout, registryService));
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public void setRegistryAddr(String registryAddr) {
        this.registryAddr = registryAddr;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}

```



## RpcReference
```java

package com.mini.rpc.consumer.annotation;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Autowired
public @interface RpcReference {

    String serviceVersion() default "1.0";

    String registryType() default "ZOOKEEPER";

    String registryAddress() default "127.0.0.1:2181";

    long timeout() default 5000;

}
```



##

```java

package com.mini.rpc.consumer;

import com.mini.rpc.common.MiniRpcFuture;
import com.mini.rpc.common.MiniRpcRequest;
import com.mini.rpc.common.MiniRpcRequestHolder;
import com.mini.rpc.common.MiniRpcResponse;
import com.mini.rpc.consumer.Limiter.ConfineManager;
import com.mini.rpc.protocol.MiniRpcProtocol;
import com.mini.rpc.protocol.MsgHeader;
import com.mini.rpc.protocol.MsgType;
import com.mini.rpc.protocol.ProtocolConstants;
import com.mini.rpc.provider.registry.RegistryService;
import com.mini.rpc.serialization.SerializationTypeEnum;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RpcInvokerProxy implements InvocationHandler {

    private final String serviceVersion;
    private final long timeout;
    private final RegistryService registryService;

    public RpcInvokerProxy(String serviceVersion, long timeout, RegistryService registryService) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.registryService = registryService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {

        // 构造 RPC 协议对象
        MiniRpcProtocol<MiniRpcRequest> protocol = new MiniRpcProtocol<>();
        MsgHeader header = new MsgHeader();
        long requestId = MiniRpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(requestId);
        header.setSerialization((byte) SerializationTypeEnum.HESSIAN.getType());
        header.setMsgType((byte) MsgType.REQUEST.getType());
        header.setStatus((byte) 0x1);
        protocol.setHeader(header);

        //报文数据内容部分
        MiniRpcRequest request = new MiniRpcRequest();
        request.setServiceVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParams(args);
        protocol.setBody(request);

        //TODO 大量代理类实例的创建就是从 RpcRequestHandler中调用invoke跑到这里来,这时的RpcConsumer就是新创建的,所以其map就无法共享
        RpcConsumer rpcConsumer = new RpcConsumer();
        MiniRpcFuture<MiniRpcResponse> future = new MiniRpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);
        MiniRpcRequestHolder.REQUEST_MAP.put(requestId, future);


        //TODO:这一块其实可根据不同方法进行不同的令牌桶数量选择
        ConfineManager.acquire(method.getName(),10);
        log.info("获取令牌1枚, 当前令牌桶剩余数量: {}", ConfineManager.rateLimiterMap.get(method.getName()).getToken());

        // 发起 RPC 远程调用
//        rpcConsumer.sendRequest(protocol, this.registryService);

        // TODO hold request by ThreadLocal

        // 等待 RPC 调用执行结果  TODO:方法调用失败重试机制

        int count = 1;
        Object data = null;


        for(; count < 3; count++){
            try {
                log.info("重试第 {} 次",count);
                    rpcConsumer.sendRequest(protocol,this.registryService);
//                data = future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS).getData();
                data = future.getPromise().get(1000, TimeUnit.MILLISECONDS).getData();
                break;
            }catch (Exception e){
                log.info("远程调用失败");
            }
        }

        return data;
    }
}

```



[spring中InitializingBean接口使用理解 - 明志健致远 - 博客园 (cnblogs.com)](https://www.cnblogs.com/study-everyday/p/6257127.html)


## 总体架构
-   rpc-provider，服务提供者。负责发布 RPC 服务，接收和处理 RPC 请求。
-   rpc-consumer，服务消费者。使用动态代理发起 RPC 远程调用，帮助使用者来屏蔽底层网络通信的细节。
-   rpc-registry，注册中心模块。提供服务注册、服务发现、负载均衡的基本功能。
-   rpc-protocol，网络通信模块。包含 RPC 协议的编解码器、序列化和反序列化工具等。
-   rpc-core，基础类库。提供通用的工具类以及模型定义，例如 RPC 请求和响应类、RPC 服务元数据类等。
-   rpc-facade，RPC 服务接口。包含服务提供者需要对外暴露的接口，本模块主要用于模拟真实 RPC 调用的测试。

rpc-provider 和 rpc-consumer 两个模块独立启动，模拟服务端和客户端。rpc-provider 通过 @RpcService 注解暴露 RPC 服务 HelloFacade，rpc-consumer 通过 @RpcReference 注解引用 HelloFacade 服务并发起调用，基本与我们常用的 RPC 框架使用方式保持一致。




## 服务发布者

服务提供者 rpc-provider 需要完成哪些事情呢？主要分为四个核心流程：

-   服务提供者启动服务，并暴露服务端口；
-   启动时扫描需要对外发布的服务，并将服务元数据信息发布到注册中心；
-   接收 RPC 请求，解码后得到请求消息；
-   提交请求至自定义线程池进行处理，并将处理结果写回客户端。

#### 服务提供者启动
服务提供者采用的是主从 Reactor 线程模型，启动过程包括配置线程池、Channel 初始化、端口绑定三个步骤


#### 参数配置
服务提供者启动需要配置一些参数，我们不应该把这些参数固定在代码里，而是以命令行参数或者配置文件的方式进行输入。我们可以使用 Spring Boot 的 @ConfigurationProperties 注解很轻松地实现配置项的加载，并且可以把相同前缀类型的配置项自动封装成实体类


我们一共提取了三个参数，分别为服务暴露的端口 servicePort、注册中心的地址 registryAddr 和注册中心的类型 registryType。@ConfigurationProperties 注解最经典的使用方式就是通过 prefix 属性指定配置参数的前缀，默认会与全局配置文件 application.properties 或者 application.yml 中的参数进行一一绑定。如果你想自定义一个配置文件，可以通过 @PropertySource 注解指定配置文件的位置。下面我们在 rpc-provider 模块的 resources 目录下创建全局配置文件 application.properties，并配置以上三个参数

application.properties 配置文件中的属性必须和实体类的成员变量是一一对应的，可以采用以下常用的命名规则，例如驼峰命名 rpc.servicePort=2781；或者虚线 - 分割的方式 rpc.service-port=2781；以及大写加下划线的形式 RPC_Service_Port，建议在环境变量中使用。@ConfigurationProperties 注解还可以支持更多复杂结构的配置，并且可以 Validation 功能进行参数校验，如果有兴趣可以自行研究。

有了 RpcProperties 实体类，我们接下来应该如何使用呢？如果只配置 @ConfigurationProperties 注解，Spring 容器并不能获取配置文件的内容并映射为对象，这时 @EnableConfigurationProperties 注解就登场了。@EnableConfigurationProperties 注解的作用就是将声明 @ConfigurationProperties 注解的类注入为 Spring 容器中的 Bean


我们通过 @EnableConfigurationProperties 注解使得 RpcProperties 生效，并通过 @Configuration 和 @Bean 注解自定义了 RpcProvider 的生成方式。@Configuration 主要用于定义配置类，配置类内部可以包含多个 @Bean 注解的方法，可以替换传统 XML 的定义方式。**被 @Bean 注解的方法会返回一个自定义的对象，@Bean 注解会将这个对象注册为 Bean 并装配到 Spring 容器中，@Bean 比 @Component 注解的自定义功能更强。**


#### 发布服务
在服务提供者启动时，我们需要思考一个核心问题，服务提供者需要将服务发布到注册中心，怎么知道哪些服务需要发布呢？服务提供者需要定义发布服务类型、服务版本等属性，主流的 RPC 框架都采用 XML 文件或者注解的方式进行定义。以注解的方式暴露服务现在最为常用，省去了很多烦琐的 XML 配置过程。例如 Dubbo 框架中使用 @Service 注解替代 dubbo:service 的定义方式，服务消费者则使用 @Reference 注解替代 dubbo:reference。接下来我们看看作为服务提供者，如何**通过注解暴露服务**，首先给出我们自定义的 @RpcService 注解定义

@RpcService 提供了两个必不可少的属性：服务类型 serviceInterface 和服务版本 serviceVersion，服务消费者必须指定完全一样的属性才能正确调用。有了 @RpcService 注解之后，我们就可以在服务实现类上使用它，@RpcService 注解本质上就是 @Component，可以将服务实现类注册成 Spring 容器所管理的 Bean，那么 serviceInterface、serviceVersion 的属性值怎么才能和 Bean 关联起来呢？这就需要我们就 Bean 的生命周期以及 Bean 的可扩展点有所了解。


RpcProvider 重写了 BeanPostProcessor 接口的 postProcessAfterInitialization 方法，对所有初始化完成后的 Bean 进行扫描。如果 Bean 包含 @RpcService 注解，那么通过注解读取服务的元数据信息并构造出 ServiceMeta 对象，接下来准备将服务的元数据信息发布至注册中心，注册中心的实现后续再细说。此外，RpcProvider 还维护了一个 rpcServiceMap，存放服务初始化后所对应的 Bean，rpcServiceMap 起到了缓存的角色，在处理 RPC 请求时可以直接通过 rpcServiceMap 拿到对应的服务进行调用。



## 服务消费者订阅服务
与服务提供者不同的是，服务消费者并不是一个常驻的服务，每次发起 RPC 调用时它才会去选择向哪个远端服务发送数据。所以服务消费者的实现要复杂一些，对于声明 @RpcReference 注解的成员变量，我们需要构造出一个可以真正进行 RPC 调用的 Bean，然后将它注册到 Spring 的容器中。

@RpcReference 注解提供了服务版本 serviceVersion、注册中心类型 registryType、注册中心地址 registryAddress 和超时时间 timeout 四个属性，接下来我们需要使用这些属性构造出一个自定义的 Bean，并对该 Bean 执行的所有方法进行拦截。

Spring 的 FactoryBean 接口可以帮助我们实现自定义的 Bean，FactoryBean 是一种特种的工厂 Bean，通过 getObject() 方法返回对象，而并不是 FactoryBean 本身。


 RpcReferenceBean 的 init() 方法需要实现动态代理对象，并通过代理对象完成 RPC 调用。对于使用者来说只是通过 @RpcReference 订阅了服务，并不感知底层调用的细节。对于如何实现 RPC 通信、服务寻址等，都是在动态代理类中完成的，动态代理的实现在后面会详细讲解。

有了 @RpcReference 注解和 RpcReferenceBean 之后，我们可以使用 Spring 的扩展点 BeanFactoryPostProcessor 对 Bean 的定义进行修改。上文中服务提供者使用的是 BeanPostProcessor，BeanFactoryPostProcessor 和 BeanPostProcessor 都是 Spring 的核心扩展点，它们之间有什么区别呢？**BeanFactoryPostProcessor 是 Spring 容器加载 Bean 的定义之后以及 Bean 实例化之前执行，所以 BeanFactoryPostProcessor 可以在 Bean 实例化之前获取 Bean 的配置元数据，并允许用户对其修改。而 BeanPostProcessor 是在 Bean 初始化前后执行，它并不能修改 Bean 的配置信息**。

现在我们需要对声明 @RpcReference 注解的成员变量构造出 RpcReferenceBean，所以需要实现 BeanFactoryPostProcessor 修改 Bean 的定义

RpcConsumerPostProcessor 类中重写了 BeanFactoryPostProcessor 的 postProcessBeanFactory 方法，从 beanFactory 中获取所有 Bean 的定义信息，然后分别对每个 Bean 的所有 field 进行检测。如果 field 被声明了 @RpcReference 注解，通过 BeanDefinitionBuilder 构造 RpcReferenceBean 的定义，并为 RpcReferenceBean 的成员变量赋值，包括服务类型 interfaceClass、服务版本 serviceVersion、注册中心类型 registryType、注册中心地址 registryAddr 以及超时时间 timeout。构造完 RpcReferenceBean 的定义之后，会将RpcReferenceBean 的 BeanDefinition 重新注册到 Spring 容器中。

至此，我们已经将服务提供者服务消费者的基本框架搭建出来了，并且着重介绍了服务提供者使用 @RpcService 注解是如何发布服务的，服务消费者相应需要一个能够注入服务接口的注解 @RpcReference，被 @RpcReference 修饰的成员变量都会被构造成 RpcReferenceBean，并为它生成动态代理类



## RPC 通信方案设计
搭建好了服务提供者和服务消费者的基本框架以后，就可以建立两个模块之间的通信机制了。通过向 ChannelPipeline 添加自定义的业务处理器，来完成 RPC 框架的远程通信机制。需要实现的主要功能如下：

-   服务消费者实现协议编码，向服务提供者发送调用数据。
-   服务提供者收到数据后解码，然后向服务消费者发送响应数据，暂时忽略 RPC 请求是如何被调用的。
-   服务消费者收到响应数据后成功返回。

RPC 请求的过程对于服务消费者来说是出站操作，对于服务提供者来说是入站操作。数据发送前，服务消费者将 RPC 请求信息封装成 MiniRpcProtocol 对象，然后通过编码器 MiniRpcEncoder 进行二进制编码，最后直接向发送至远端即可。服务提供者收到请求数据后，将二进制数据交给解码器 MiniRpcDecoder，解码后再次生成 MiniRpcProtocol 对象，然后传递给 RpcRequestHandler 执行真正的 RPC 请求调用。

与 RPC 请求过程相反，是由服务提供者将响应结果封装成 MiniRpcProtocol 对象，然后通过 MiniRpcEncoder 编码发送给服务消费者。服务消费者对响应结果进行解码，因为 RPC 请求是高并发的，所以需要 RpcRequestHandler 根据响应结果找到对应的请求，最后将响应结果返回。 

综合 RPC 请求调用和结果响应的处理过程来看，编码器 MiniRpcEncoder、解码器 MiniRpcDecoder 以及通信协议对象 MiniRpcProtocol 都可以设计成复用的

### 自定义 RPC 通信协议
协议是服务消费者和服务提供者之间通信的基础，主流的 RPC 框架都会自定义通信协议，相比于 HTTP、HTTPS、JSON 等通用的协议，自定义协议可以实现更好的性能、扩展性以及安全性。结合 RPC 请求调用与结果响应的场景，我们设计一个简易版的 RPC 自定义协议，如下所示：

```
+---------------------------------------------------------------+

| 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |

+---------------------------------------------------------------+

| 状态 1byte |        消息 ID 8byte     |      数据长度 4byte     |

+---------------------------------------------------------------+

|                   数据内容 （长度不定）                          |

+---------------------------------------------------------------+
```



### 动态代理的选择
客户端动态代理选择JDK是因为创建个匿名类执行动态代理类的invoke方法给目标类的方法增强功能,这里是进行一个数据发送

服务端选择CGLib是因为反射调用方法的性能较低,而通过FastClass避免了反射调用,而是生成新的类,向类中写入委托类实例直接调用的语句,根据方法签名找到索引并调用对应的方法





### 序列化算法选择

```markdown
要想使用网络框架的 API 来传输结构化的数据，必须得先实现结构化的数据与字节流之间的双向转换。这种将结构化数据转换成字节流的过程，我们称为序列化，反过来转换，就是反序列化。


序列化的用途除了用于在网络上传输数据以外，另外的一个重要用途是，将结构化数据保存在文件中，因为在文件内保存数据的形式也是二进制序列，和网络传输过程中的数据是一样的，所以序列化同样适用于将结构化数据保存在文件中。

很多处理海量数据的场景中，都需要将对象序列化后，把它们暂时从内存转移到磁盘中，等需要用的时候，再把数据从磁盘中读取出来，反序列化成对象来使用，这样不仅可以长期保存不丢失数据，而且可以节省有限的内存空间。

面对这么多种序列化实现，我们该如何选择呢？
你需要权衡这样几个因素：
1.序列化后的数据最好是易于人类阅读的；
2.实现的复杂度是否足够低；
3.序列化和反序列化的速度越快越好；
4.序列化后的信息密度越大越好，也就是说，同样的一个结构化数据，序列化之后占用的存储空间越小越好；

当然，不会存在一种序列化实现在这四个方面都是最优的，否则我们就没必要来纠结到底选择哪种实现了。因为，大多数情况下，易于阅读和信息密度是矛盾的，实现的复杂度和性能也是互相矛盾的。所以，我们需要根据所实现的业务，来选择合适的序列化实现。

像 JSON、XML 这些序列化方法，可读性最好，但信息密度也最低。像 Kryo、**Hessian 这些通用的二进制序列化实现，适用范围广，使用简单，性能比 JSON、XML 要好一些，但是肯定不如专用的序列化实现。**

对于一些强业务类系统，比如说电商类、社交类的应用系统，这些系统的特点是，业务复杂，需求变化快，但是对性能的要求没有那么苛刻。这种情况下，我推荐你使用 JSON 这种实现简单，数据可读性好的序列化实现，这种实现使用起来非常简单，序列化后的 JSON 数据我们都可以看得懂，无论是接口调试还是排查问题都非常方便。付出的代价就是多一点点 CPU 时间和存储空间而已。

如果 JSON 序列化的性能达不到你系统的要求，可以采用性能更好的二进制序列化实现，实现的复杂度和 JSON 序列化是差不多的，都很简单，但是序列化性能更好，信息密度也更高，代价就是失去了可读性。

```




项目分为以下模块：

服务提供者：负责类的发布，RPC请求的接收和处理

服务消费者：通过自定义注解和动态代理，帮助使用者屏蔽底层网络通信的细节

注册中心模块：提供服务注册，服务发现，负载均衡功能

网络通信模块：RPC协议的编解码器，序列化和反序列化工具

基础类库：提供通用工具类和模型定义，比如RPC请求响应类，RPC服务元数据

RPC服务接口模块：服务提供者需要对外暴露的接口


rpc-provider 通过 @RpcService 注解暴露 RPC 服务 HelloFacade，rpc-consumer 通过 @RpcReference 注解引用 HelloFacade 服务并发起调用



服务提供者：
扫描需要对外发布的服务，并将服务元数据注册到服务中心

接收RPC请求，解码后得到请求信息

提交请求至线程池执行，并将处理结果写回客户端


服务消费者：

通过检查 bean中的file是否带有 RpcReference注解，BeanDefinitionBuilder 构造 RpcReferenceBean 的定义，并为 RpcReferenceBean 的成员变量赋值，包括服务类型 interfaceClass、服务版本 serviceVersion、注册中心类型 registryType、注册中心地址 registryAddr 以及超时时间 timeout。构造完 RpcReferenceBean 的定义之后，会将RpcReferenceBean 的 BeanDefinition 重新注册到 Spring 容器中。

当调用接口提供的方法时，利用动态代理将数据发出去，拿到数据或超时后返回



通信协议设计：

分为协议头，协议体：

协议头：魔数，协议版本号，序列化算法，消息ID，报文类型，状态，数据长度

协议体：数据内容，分为请求报文，响应报文

编解码：
编码 header部分直接 ByteBuf  write进去，body部分拿到序列化算法实例做序列化操作，写进长度和数据内容

解码比较复杂一点：

先读取 ByteBuf中数据长度是否大于 header长度，小于的话直接返回

记录下当前位置，读取魔数，看魔数是否匹配，读完header后判断一下剩下的数据长度是否达到 len，不是的话指针重置，返回

读取到body部分，先判断是否是我们支持的类型，不是返回

创建出header，根据报文类型，选择序列化出不同的类实例。创建出一个协议实例，将header和body放进去，传给下一个handler



```markdown

解码器 MiniRpcDecoder 相比于编码器 MiniRpcEncoder 要复杂很多，MiniRpcDecoder 的目标是将字节流数据解码为消息对象，并传递给下一个 Inbound 处理器。整个 MiniRpcDecoder 解码过程有几个要点要特别注意：

只有当 ByteBuf 中内容大于协议头 Header 的固定的 18 字节时，才开始读取数据。
即使已经可以完整读取出协议头 Header，但是协议体 Body 有可能还未就绪。所以在刚开始读取数据时，需要使用 markReaderIndex() 方法标记读指针位置，当 ByteBuf 中可读字节长度小于协议体 Body 的长度时，再使用 resetReaderIndex() 还原读指针位置，说明现在 ByteBuf 中可读字节还不够一个完整的数据包。
根据不同的报文类型 MsgType，需要反序列化出不同的协议体对象。在 RPC 请求调用的场景下，服务提供者需要将协议体内容反序列化成 MiniRpcRequest 对象；在 RPC 结果响应的场景下，服务消费者需要将协议体内容反序列化成 MiniRpcResponse 对象。
```



服务消费者在发起调用时，维护了请求 requestId 和 MiniRpcFuture<MiniRpcResponse>的映射关系，RpcResponseHandler 会根据请求的 requestId 找到对应发起调用的 MiniRpcFuture，然后为 MiniRpcFuture 设置响应结果。

我们采用 Netty 提供的 Promise 工具来实现 RPC 请求的同步等待，Promise 基于 JDK 的 Future 扩展了更多新的特性，帮助我们更好地以同步的方式进行异步编程。Promise 模式本质是一种异步编程模型，我们可以先拿到一个查看任务执行结果的凭证，不必等待任务执行完毕，当我们需要获取任务执行结果时，再使用凭证提供的相关接口进行获取。


