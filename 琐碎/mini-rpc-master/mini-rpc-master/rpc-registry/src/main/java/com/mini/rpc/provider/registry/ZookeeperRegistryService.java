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

        // name 就是服务接口名称#版本号
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

    /**
     首先找出被调用服务所有的节点列表，然后通过 ZKConsistentHashLoadBalancer 提供的一致性 Hash 算法找出相应的服务节点
     @param serviceName
     @param invokerHashCode
     @return
     @throws Exception
     */
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
