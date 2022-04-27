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