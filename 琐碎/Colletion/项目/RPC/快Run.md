
```markdown
**别tm看了，直接看原文更好**

对于容错，有很多不同的概念可以表述。这些表述中，有一个共同的思想就是可用性（Availability）。某些系统经过精心的设计，这样在特定的错误类型下，系统仍然能够正常运行，仍然可以像没有出现错误一样，为你提供完整的服务。
某些系统通过这种方式提供可用性。比如，你构建了一个有两个拷贝的多副本系统，其中一个故障了，另一个还能运行。当然如果两个副本都故障了，你的系统就不再有可用性。所以，可用系统通常是指，在特定的故障范围内，系统仍然能够提供服务，系统仍然是可用的。如果出现了更多的故障，系统将不再可用。

除了可用性之外，另一种容错特性是自我可恢复性（recoverability）。这里的意思是，如果出现了问题，服务会停止工作，不再响应请求，之后有人来修复，并且在修复之后系统仍然可以正常运行，就像没有出现过问题一样。这是一个比可用性更弱的需求，因为在出现故障到故障组件被修复期间，系统将会完全停止工作。但是修复之后，系统又可以完全正确的重新运行，所以可恢复性是一个重要的需求。

对于一个可恢复的系统，通常需要做一些操作，例如将最新的数据存放在磁盘中，这样在供电恢复之后（假设故障就是断电），才能将这些数据取回来。甚至说对于一个具备可用性的系统，为了让系统在实际中具备应用意义，也需要具备可恢复性。因为可用的系统仅仅是在一定的故障范围内才可用，如果故障太多，可用系统也会停止工作，停止一切响应。但是当足够的故障被修复之后，系统还是需要能继续工作。所以，一个好的可用的系统，某种程度上应该也是可恢复的。当出现太多故障时，系统会停止响应，但是修复之后依然能正确运行。这是我们期望看到的。

为了实现这些特性，有很多工具。其中最重要的有两个：

一个是非易失存储（non-volatile storage，类似于硬盘）。这样当出现类似电源故障，甚至整个机房的电源都故障时，我们可以使用非易失存储，比如硬盘，闪存，SSD之类的。我们可以存放一些checkpoint或者系统状态的log在这些存储中，这样当备用电源恢复或者某人修好了电力供给，我们还是可以从硬盘中读出系统最新的状态，并从那个状态继续运行。所以，这里的一个工具是非易失存储。因为更新非易失存储是代价很高的操作，所以相应的出现了很多非易失存储的管理工具。同时构建一个高性能，容错的系统，聪明的做法是避免频繁的写入非易失存储。在过去，甚至对于今天的一个3GHZ的处理器，写入一个非易失存储意味着移动磁盘臂并等待磁碟旋转，这两个过程都非常缓慢。有了闪存会好很多，但是为了获取好的性能，仍然需要许多思考。
    
对于容错的另一个重要工具是复制（replication），不过，管理复制的多副本系统会有些棘手。任何一个多副本系统中，都会有一个关键的问题，比如说，我们有两台服务器，它们本来应该是有着相同的系统状态，现在的关键问题在于，这两个副本总是会意外的偏离同步的状态，而不再互为副本。对于任何一种使用复制实现容错的系统，我们都面临这个问题。lab2和lab3都是通过管理多副本来实现容错的系统，你将会看到这里究竟有多复杂。
```




### 协议的序列化和反序列化

header
    +---------------------------------------------------------------+
    | 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
    +---------------------------------------------------------------+
    | 状态 1byte |        消息 ID 8byte     |      数据长度 4byte     |
    +---------------------------------------------------------------+


协议组成
  +---------------------------------------------------------------+
    | 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
    +---------------------------------------------------------------+
    | 状态 1byte |        消息 ID 8byte     |      数据长度 4byte     |
    +---------------------------------------------------------------+
    |                   数据内容 （长度不定）                          |
    +---------------------------------------------------------------+





```java

public class MiniRpcProtocol<T> implements Serializable {  
    private MsgHeader header;  
    private T body;  
}


@Data  
public class MiniRpcResponse implements Serializable {  
    private Object data;  
    private String message;  
}


MiniRpcProtocol 内的body就是 MiniRpcResponse
```



#### 编解码器(进门第一关字节流转成协议再进, 出门最后一关由协议转成字节流再出)
```java

## 编码器

package com.mini.rpc.codec;

import com.mini.rpc.protocol.MiniRpcProtocol;
import com.mini.rpc.protocol.MsgHeader;
import com.mini.rpc.serialization.RpcSerialization;
import com.mini.rpc.serialization.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MiniRpcEncoder extends MessageToByteEncoder<MiniRpcProtocol<Object>> {

    /*
    +---------------------------------------------------------------+
    | 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
    +---------------------------------------------------------------+
    | 状态 1byte |        消息 ID 8byte     |      数据长度 4byte     |
    +---------------------------------------------------------------+
    |                   数据内容 （长度不定）                          |
    +---------------------------------------------------------------+
    */
    @Override
    protected void encode(ChannelHandlerContext ctx, MiniRpcProtocol<Object> msg, ByteBuf byteBuf) throws Exception {
        MsgHeader header = msg.getHeader();
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getVersion());
        byteBuf.writeByte(header.getSerialization());
        byteBuf.writeByte(header.getMsgType());
        byteBuf.writeByte(header.getStatus());
        byteBuf.writeLong(header.getRequestId());

		//工厂模式,根据不同的序列化算法返回不同的序列化类
        RpcSerialization rpcSerialization = SerializationFactory.getRpcSerialization(header.getSerialization());
        byte[] data = rpcSerialization.serialize(msg.getBody());
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}


### 解码器
package com.mini.rpc.codec;

import com.mini.rpc.common.MiniRpcRequest;
import com.mini.rpc.common.MiniRpcResponse;
import com.mini.rpc.protocol.MiniRpcProtocol;
import com.mini.rpc.protocol.MsgHeader;
import com.mini.rpc.protocol.MsgType;
import com.mini.rpc.protocol.ProtocolConstants;
import com.mini.rpc.serialization.RpcSerialization;
import com.mini.rpc.serialization.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MiniRpcDecoder extends ByteToMessageDecoder {

    /*
    +---------------------------------------------------------------+
    | 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
    +---------------------------------------------------------------+
    | 状态 1byte |        消息 ID 8byte     |      数据长度 4byte     |
    +---------------------------------------------------------------+
    |                   数据内容 （长度不定）                          |
    +---------------------------------------------------------------+
    */
    @Override
    public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < ProtocolConstants.HEADER_TOTAL_LEN) {
            return;
        }
        in.markReaderIndex();

        short magic = in.readShort();

        //判断魔数是否匹配
        if (magic != ProtocolConstants.MAGIC) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }

        byte version = in.readByte();
        byte serializeType = in.readByte();
        byte msgType = in.readByte();
        byte status = in.readByte();
        long requestId = in.readLong();

        //数据长度
        int dataLength = in.readInt();

        //readableBytes():返回可读区域的字节数. 当 ByteBuf 中可读字节长度小于协议体 Body 的长度时，
        // 再使用 resetReaderIndex() 还原读指针位置，说明现在 ByteBuf 中可读字节还不够一个完整的数据包。
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLength];
        in.readBytes(data);

        //先判断是不是我们支持的报文类型,目前规定三种 : 请求,响应,心跳,找不到就返回null
        MsgType msgTypeEnum = MsgType.findByType(msgType);
        if (msgTypeEnum == null) {
            return;
        }

        //将报文头信息获取
        MsgHeader header = new MsgHeader();
        header.setMagic(magic);
        header.setVersion(version);
        header.setSerialization(serializeType);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(msgType);
        header.setMsgLen(dataLength);

        //工厂模式,获取对应的序列化类,在根据报文类型做序列化或者反序列化
        RpcSerialization rpcSerialization = SerializationFactory.getRpcSerialization(serializeType);
        switch (msgTypeEnum) {
            case REQUEST:
                MiniRpcRequest request = rpcSerialization.deserialize(data, MiniRpcRequest.class);

                //如果报文body部分不为null,则将 创建一个 协议类,将报文头和body放进去, out.add(协议)
                if (request != null) {
                    MiniRpcProtocol<MiniRpcRequest> protocol = new MiniRpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(request);
                    
                    //传到下一个handler吧应该是
                    out.add(protocol);
                }
                break;
            case RESPONSE:
                MiniRpcResponse response = rpcSerialization.deserialize(data, MiniRpcResponse.class);
                if (response != null) {
                    MiniRpcProtocol<MiniRpcResponse> protocol = new MiniRpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(response);
                    out.add(protocol);
                }
                break;
            case HEARTBEAT:
                // TODO
                break;
        }
    }
}


```



#### 请求响应类 MiniRpcRequest
```java
package com.mini.rpc.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class MiniRpcRequest implements Serializable {
    private String serviceVersion;
    private String className;
    private String methodName;
    private Object[] params;
    private Class<?>[] parameterTypes;
}
```


#### 报文类型类 MsgType
```java
package com.mini.rpc.protocol;  
  
import lombok.Getter;  
  
public enum MsgType {  
    REQUEST(1),  
    RESPONSE(2),  
    HEARTBEAT(3);  //心跳包
  
    @Getter  
 private final int type;  
  
    MsgType(int type) {  
        this.type = type;  
    }  
  
    public static MsgType findByType(int type) {  
        for (MsgType msgType : MsgType.values()) {  
            if (msgType.getType() == type) {  
                return msgType;  
            }  
        }  
        return null;  
    }  
}
```


#### 请求处理状态类 MsgStatus
```java
package com.mini.rpc.protocol;  
  
import lombok.Getter;  
  
public enum MsgStatus {  
    SUCCESS(0),  
    FAIL(1);  
  
    @Getter  
 private final int code;  
  
    MsgStatus(int code) {  
        this.code = code;  
    }  
  
}

```


### 提交执行任务的类 RpcRequestProcessor
```java

package com.mini.rpc.handler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcRequestProcessor {
    private static ThreadPoolExecutor threadPoolExecutor;

    public static void submitRequest(Runnable task) {
        if (threadPoolExecutor == null) {
            synchronized (RpcRequestProcessor.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));
                }
            }
        }
        threadPoolExecutor.submit(task);
    }
}

```


### 提供服务与发布服务的类 RpcProvider
```java



```



### 自定义注解 RpcService
```java
package com.mini.rpc.provider.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface RpcService {

    Class<?> serviceInterface() default Object.class;

    String serviceVersion() default "1.0";
}

```



# 服务发布与订阅

服务提供者 rpc-provider 需要完成哪些事情呢？主要分为四个核心流程：

-   服务提供者启动服务，并暴露服务端口；
-   启动时扫描需要对外发布的服务，并将服务元数据信息发布到注册中心；
-   接收 RPC 请求，解码后得到请求消息；
-   提交请求至自定义线程池进行处理，并将处理结果写回客户端。


### 服务提供者发布服务
```java

package com.mini.rpc.provider;

/**
 当一个类实现这个接口之后，Spring启动后，初始化Bean时，
 若该Bean实现InitialzingBean接口，会自动调用afterPropertiesSet()方法，完成一些用户自定义的初始化操作。

 */
@Slf4j
public class RpcProvider implements InitializingBean, BeanPostProcessor {

    private String serverAddress;
    private final int serverPort;
    private final RegistryService serviceRegistry;

    private final Map<String, Object> rpcServiceMap = new HashMap<>();

    public RpcProvider(int serverPort, RegistryService serviceRegistry) {
        this.serverPort = serverPort;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void afterPropertiesSet() {
        new Thread(() -> {
            try {
                startRpcServer();
            } catch (Exception e) {
                log.error("start rpc server error.", e);
            }
        }).start();
    }

    private void startRpcServer() throws Exception {
        this.serverAddress = InetAddress.getLocalHost().getHostAddress();

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new MiniRpcEncoder())
                                    .addLast(new MiniRpcDecoder())
                                    .addLast(new RpcRequestHandler(rpcServiceMap));
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = bootstrap.bind(this.serverAddress, this.serverPort).sync();
            log.info("server addr {} started on port {}", this.serverAddress, this.serverPort);
            channelFuture.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }


    /**
     * 对所有初始化完成后的 Bean 进行扫描。
     * 如果 Bean 包含 @RpcService 注解，那么通过注解读取服务的元数据信息并构造出 ServiceMeta 对象，接下来准备将服务的元数据信息发布至注册中心。
     * 此外，RpcProvider 还维护了一个 rpcServiceMap，存放服务初始化后所对应的 Bean，rpcServiceMap 起到了缓存的角色，
     * 在处理 RPC 请求时可以直接通过 rpcServiceMap 拿到对应的服务进行调用。
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
        if (rpcService != null) {

            //拿到自定义注解的信息,可以看HelloFacadeImpl
            //第一个是接口名,注意,只是接口名而不是具体实现类
            String serviceName = rpcService.serviceInterface().getName();
            String serviceVersion = rpcService.serviceVersion();

            try {

                //这个就是要注册到zookeper上的吧
                ServiceMeta serviceMeta = new ServiceMeta();
                serviceMeta.setServiceAddr(serverAddress);
                serviceMeta.setServicePort(serverPort);
                serviceMeta.setServiceName(serviceName);
                serviceMeta.setServiceVersion(serviceVersion);

                serviceRegistry.register(serviceMeta);
                rpcServiceMap.put(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion()), bean);
            } catch (Exception e) {
                log.error("failed to register service {}#{}", serviceName, serviceVersion, e);
            }
        }
        return bean;
    }

}


```





### 服务消费者订阅服务
与服务提供者不同的是，服务消费者并不是一个常驻的服务，每次发起 RPC 调用时它才会去选择向哪个远端服务发送数据。所以服务消费者的实现要复杂一些，对于声明 @RpcReference 注解的成员变量，我们需要构造出一个可以真正进行 RPC 调用的 Bean，然后将它注册到 Spring 的容器中。

```java
package com.mini.rpc.consumer.annotation;  

@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.FIELD)  
@Autowired  
public @interface RpcReference {  
  
    String serviceVersion() default "1.0";  
  
    String registryType() default "ZOOKEEPER";  
  
    String registryAddress() default "127.0.0.1:2181";  
  
    long timeout() default 5000;  
  
}

@RpcReference 注解提供了服务版本 serviceVersion、注册中心类型 registryType、注册中心地址 registryAddress 和超时时间 timeout 四个属性，接下来我们需要使用这些属性构造出一个自定义的 Bean，并对该 Bean 执行的所有方法进行拦截。

Spring 的 FactoryBean 接口可以帮助我们实现自定义的 Bean，FactoryBean 是一种特种的工厂 Bean，通过 getObject() 方法返回对象，而并不是 FactoryBean 本身。






package com.mini.rpc.consumer;  
  
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


@RpcReference 注解提供了服务版本 serviceVersion、注册中心类型 registryType、注册中心地址 registryAddress 和超时时间 timeout 四个属性，接下来我们需要使用这些属性构造出一个自定义的 Bean，并对该 Bean 执行的所有方法进行拦截。

Spring 的 FactoryBean 接口可以帮助我们实现自定义的 Bean，FactoryBean 是一种特种的工厂 Bean，通过 getObject() 方法返回对象，而并不是 FactoryBean 本身。

```


```java
package com.mini.rpc.consumer;  


@Component  
@Slf4j  
public class RpcConsumerPostProcessor implements ApplicationContextAware, BeanClassLoaderAware, BeanFactoryPostProcessor {  
  
    private ApplicationContext context;  
  
    private ClassLoader classLoader;  
  
    private final Map<String, BeanDefinition> rpcRefBeanDefinitions = new LinkedHashMap<>();  
  
    @Override  
 public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {  
        this.context = applicationContext;  
    }  
  
    @Override  
 public void setBeanClassLoader(ClassLoader classLoader) {  
        this.classLoader = classLoader;  
    }  
  
    @Override  
 public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {  
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {  
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);  
            String beanClassName = beanDefinition.getBeanClassName();  
            if (beanClassName != null) {  
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.classLoader);  
                ReflectionUtils.doWithFields(clazz, this::parseRpcReference);  
            }  
        }  
  
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;  
        this.rpcRefBeanDefinitions.forEach((beanName, beanDefinition) -> {  
            if (context.containsBean(beanName)) {  
                throw new IllegalArgumentException("spring context already has a bean named " + beanName);  
            }  
            registry.registerBeanDefinition(beanName, rpcRefBeanDefinitions.get(beanName));  
            log.info("registered RpcReferenceBean {} success.", beanName);  
        });  
    }  
  
    private void parseRpcReference(Field field) {  
        RpcReference annotation = AnnotationUtils.getAnnotation(field, RpcReference.class);  
        if (annotation != null) {  
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcReferenceBean.class);  
            builder.setInitMethodName(RpcConstants.INIT_METHOD_NAME);  
            builder.addPropertyValue("interfaceClass", field.getType());  
            builder.addPropertyValue("serviceVersion", annotation.serviceVersion());  
            builder.addPropertyValue("registryType", annotation.registryType());  
            builder.addPropertyValue("registryAddr", annotation.registryAddress());  
            builder.addPropertyValue("timeout", annotation.timeout());  
  
            BeanDefinition beanDefinition = builder.getBeanDefinition();  
            rpcRefBeanDefinitions.put(field.getName(), beanDefinition);  
        }  
    }  
  
}


有了 @RpcReference 注解和 RpcReferenceBean 之后，我们可以使用 Spring 的扩展点 BeanFactoryPostProcessor 对 Bean 的定义进行修改。上文中服务提供者使用的是 BeanPostProcessor，BeanFactoryPostProcessor 和 BeanPostProcessor 都是 Spring 的核心扩展点，它们之间有什么区别呢？BeanFactoryPostProcessor 是 Spring 容器加载 Bean 的定义之后以及 Bean 实例化之前执行，所以 BeanFactoryPostProcessor 可以在 Bean 实例化之前获取 Bean 的配置元数据，并允许用户对其修改。而 BeanPostProcessor 是在 Bean 初始化前后执行，它并不能修改 Bean 的配置信息。

现在我们需要对声明 @RpcReference 注解的成员变量构造出 RpcReferenceBean，所以需要实现 BeanFactoryPostProcessor 修改 Bean 的定义


RpcConsumerPostProcessor 类中重写了 BeanFactoryPostProcessor 的 postProcessBeanFactory 方法，从 beanFactory 中获取所有 Bean 的定义信息，然后分别对每个 Bean 的所有 field 进行检测。如果 field 被声明了 @RpcReference 注解，通过 BeanDefinitionBuilder 构造 RpcReferenceBean 的定义，并为 RpcReferenceBean 的成员变量赋值，包括服务类型 interfaceClass、服务版本 serviceVersion、注册中心类型 registryType、注册中心地址 registryAddr 以及超时时间 timeout。构造完 RpcReferenceBean 的定义之后，会将RpcReferenceBean 的 BeanDefinition 重新注册到 Spring 容器中。

```



# 服务发现

### 注册中心配置与实例化
```java

读取配置文件

package com.mini.rpc.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rpc")
public class RpcProperties {

    private int servicePort;

    private String registryAddr;

    private String registryType;

}



package com.mini.rpc.provider;

import com.mini.rpc.common.RpcProperties;
import com.mini.rpc.provider.registry.RegistryFactory;
import com.mini.rpc.provider.registry.RegistryService;
import com.mini.rpc.provider.registry.RegistryType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class RpcProviderAutoConfiguration {

    @Resource
    private RpcProperties rpcProperties;

    @Bean
    public RpcProvider init() throws Exception {

        //注册中心类型
        RegistryType type = RegistryType.valueOf(rpcProperties.getRegistryType());

        //构建单例注册服务, 还是工厂模式,可选择多种注册中心类型
        RegistryService serviceRegistry = RegistryFactory.getInstance(rpcProperties.getRegistryAddr(), type);
        
        return new RpcProvider(rpcProperties.getServicePort(), serviceRegistry);
    }
}



有了 RpcProperties 实体类，我们接下来应该如何使用呢？如果只配置 @ConfigurationProperties 注解，Spring 容器并不能获取配置文件的内容并映射为对象，这时 @EnableConfigurationProperties 注解就登场了。@EnableConfigurationProperties 注解的作用就是将声明 @ConfigurationProperties 注解的类注入为 Spring 容器中的 Bean。具体用法如下：

我们通过 @EnableConfigurationProperties 注解使得 RpcProperties 生效，并通过 @Configuration 和 @Bean 注解自定义了 RpcProvider 的生成方式。@Configuration 主要用于定义配置类，配置类内部可以包含多个 @Bean 注解的方法，可以替换传统 XML 的定义方式。被 @Bean 注解的方法会返回一个自定义的对象，@Bean 注解会将这个对象注册为 Bean 并装配到 Spring 容器中，@Bean 比 @Component 注解的自定义功能更强。


// 注册中心接口
public interface RegistryService {

    void register(ServiceMeta serviceMeta) throws Exception;

    void unRegister(ServiceMeta serviceMeta) throws Exception;

    ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception;

    void destroy() throws IOException;
}



//存储服务的元数据信息
@Data
public class ServiceMeta {

    private String serviceName;

    private String serviceVersion;

    private String serviceAddr;

    private int servicePort;

}
```

```java
package com.mini.rpc.provider.registry;

import com.mini.rpc.common.RpcServiceHelper;
import com.mini.rpc.common.ServiceMeta;
import com.mini.rpc.provider.registry.loadbalancer.ZKConsistentHashLoadBalancer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class ZookeeperRegistryService implements RegistryService {
    public static final int BASE_SLEEP_TIME_MS = 1000;//重试之间的等待时间
    public static final int MAX_RETRIES = 3;//最大重试次数
    public static final String ZK_BASE_PATH = "/mini_rpc";

    private final ServiceDiscovery<ServiceMeta> serviceDiscovery;


    /**
     通过 CuratorFrameworkFactory 采用工厂模式创建 CuratorFramework 实例，构造客户端唯一需你指定的是重试策略，
     创建完 CuratorFramework 实例之后需要调用 start() 进行启动。然后我们需要创建 ServiceDiscovery 对象，
     由 ServiceDiscovery 完成服务的注册和发现，在系统退出的时候需要将初始化的实例进行关闭，destroy() 方法实现非常简单

     @param registryAddr
     @throws Exception
     */
    public ZookeeperRegistryService(String registryAddr) throws Exception {
                                                                                    //重试策略，在重试间隔睡眠时间增加的情况下重试设定次数
        CuratorFramework client = CuratorFrameworkFactory.newClient(registryAddr, new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
        client.start();


        JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(ServiceMeta.class);

        /**
         https://blog.csdn.net/qq_23536449/article/details/106306570

         主要的抽象类是ServiceProvider。 它封装了特定命名服务的发现服务以及提供者策略。
         提供者策略是一种用于为给定服务从一组实例中选择一个实例的方案。 共有三种策略：Round Robin，Random和Sticky（始终选择相同的策略）。

         通过ServiceProviderBuilder实例化ServiceProvider。 您可以从ServiceDiscovery获得ServiceProviderBuilder（请参见下文）。
         ServiceProviderBuilder允许您设置服务名称和其他几个可选值。必须通过调用start（）来启动ServiceProvider。 完成后，您应该调用close（）。


         返回单个可用的服务实例，请注意，不要在使用过程中一直持有返回的
         单个对象，而是重新获取新的服务实例。
         ServiceInstance<T> getInstance() throws Exception;



         为了分配ServiceProvider，您必须具有ServiceDiscovery。
         它由ServiceDiscoveryBuilder创建。您必须在对象上调用start（），并在完成后调用close（）。


         */
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
                .client(client)
                .serializer(serializer)
                .basePath(ZK_BASE_PATH)
                .build();
        this.serviceDiscovery.start();
    }

    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion()))
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(serviceMeta.getServiceName())
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    @Override
    public ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception {
        Collection<ServiceInstance<ServiceMeta>> serviceInstances = serviceDiscovery.queryForInstances(serviceName);
        ServiceInstance<ServiceMeta> instance = new ZKConsistentHashLoadBalancer().select((List<ServiceInstance<ServiceMeta>>) serviceInstances, invokerHashCode);
        if (instance != null) {
            return instance.getPayload();
        }
        return null;
    }

    @Override
    public void destroy() throws IOException {
        serviceDiscovery.close();
    }
}

```

# 负载均衡
```java
package com.mini.rpc.provider.registry.loadbalancer;

public class ZKConsistentHashLoadBalancer implements ServiceLoadBalancer<ServiceInstance<ServiceMeta>> {
    private final static int VIRTUAL_NODE_SIZE = 10;
    private final static String VIRTUAL_NODE_SPLIT = "#";


	//select() 方法的传入参数是一批服务节点以及客户端对象的 hashCode，针对 Zookeeper 的场景，我们可以实现一个比较通用的一致性 Hash 算法。
    @Override
    public ServiceInstance<ServiceMeta> select(List<ServiceInstance<ServiceMeta>> servers, int hashCode) {
        TreeMap<Integer, ServiceInstance<ServiceMeta>> ring = makeConsistentHashRing(servers);//构造哈希环
        return allocateNode(ring, hashCode);// 根据 hashCode 分配节点
    }

    private ServiceInstance<ServiceMeta> allocateNode(TreeMap<Integer, ServiceInstance<ServiceMeta>> ring, int hashCode) {
        Map.Entry<Integer, ServiceInstance<ServiceMeta>> entry = ring.ceilingEntry(hashCode);// 顺时针找到第一个节点
        if (entry == null) {
            entry = ring.firstEntry();// 如果没有大于 hashCode 的节点，直接取第一个
        }
        return entry.getValue();
    }

    private TreeMap<Integer, ServiceInstance<ServiceMeta>> makeConsistentHashRing(List<ServiceInstance<ServiceMeta>> servers) {
        TreeMap<Integer, ServiceInstance<ServiceMeta>> ring = new TreeMap<>();
        for (ServiceInstance<ServiceMeta> instance : servers) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                ring.put((buildServiceInstanceKey(instance) + VIRTUAL_NODE_SPLIT + i).hashCode(), instance);
            }
        }
        return ring;
    }

    private String buildServiceInstanceKey(ServiceInstance<ServiceMeta> instance) {
        ServiceMeta payload = instance.getPayload();
        return String.join(":", payload.getServiceAddr(), String.valueOf(payload.getServicePort()));
    }

}


JDK 提供了 TreeMap 数据结构，可以非常方便地构造哈希环。通过计算出每个服务实例 ServiceInstance 的地址和端口对应的 hashCode，然后直接放入 TreeMap 中，TreeMap 会对 hashCode 默认从小到大进行排序。在为客户端对象分配节点时，通过 TreeMap 的 ceilingEntry() 方法找出大于或等于客户端 hashCode 的第一个节点，即为客户端对应要调用的服务节点。如果没有找到大于或等于客户端 hashCode 的节点，那么直接去 TreeMap 中的第一个节点即可。
```




### 思考

当RpcConsumer这个类发送request时，若


# 坑
```markdown
-   介绍一下项目的架构设计
-   讲解了我怎么设计负载均衡[算法](https://www.nowcoder.com/jump/super-jump/word?word=%E7%AE%97%E6%B3%95)的，以及每种策略的适用场景
-   注册中心是如何实现服务的发现和引入的
-   注册中心能否处理容灾情况（不能，我都没有那么多服务器，哪里来的容灾功能）

```



## 魔数的作用
**快速** 识别字节流是否是程序能够处理的，能处理才进行后面的 **耗时** 业务操作，如果不能处理，尽快执行失败，断开连接等操作。




### 服务提供者发布服务

服务提供者 rpc-provider 需要完成哪些事情呢？主要分为四个核心流程：

-   服务提供者启动服务，并暴露服务端口；
-   启动时扫描需要对外发布的服务，并将服务元数据信息发布到注册中心；
-   接收 RPC 请求，解码后得到请求消息；
-   提交请求至自定义线程池进行处理，并将处理结果写回客户端。



### 动态代理
   Jdk动态代理的拦截对象是通过反射的机制来调用被拦截方法的，反射的效率比较低，所以cglib采用了FastClass的机制来实现对被拦截方法的调用。FastClass机制就是对一个类的方法建立索引，通过索引来直接调用相应的方法，

**简单理解一下FastClass：为一个对象A创建它的FastClass对象，这个FastClass对象相当于A的方法索引，根据A的方法名生成并关联一个index、每个index对应A的一个方法。后续只要根据这个index以及A的实例，就可以调用fastClass的`invoke(instanceOfA, index, args)`方法来快速的调用A的方法了。实现了Java反射的“运行时动态调用指定类的方法”的功能，但是使用了不同的机制。**我们知道，java反射调用方法是比较“重”的操作，要经过一系列的权限验证、通过native方法请求jvm去方法区查找方法定义、以及最后的invoke仍然可能要通过JNI调用native方法。而相比之下，FastClass方式则跟一般的一个普通的对象方法调用没啥区别、只是多了一步根据index判断调用委托类的哪个方法这一步骤、性能损耗基本没有。

FastClass机制算是一种技巧层面的东西，在java内存里边维护一个index值和对象的方法之间的逻辑映射，然后运行期可以根据index和实例来动态调用方法、且不用使用比较“重”的Java反射功能。



## 一些无语的特性

[Java中的::符号挺好理解的 - 知乎 (zhihu.com)](https://zhuanlan.zhihu.com/p/252280264)