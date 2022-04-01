package com.mini.rpc.consumer;

import com.mini.rpc.codec.MiniRpcDecoder;
import com.mini.rpc.codec.MiniRpcEncoder;
import com.mini.rpc.common.MiniRpcRequest;
import com.mini.rpc.common.RpcServiceHelper;
import com.mini.rpc.common.ServiceMeta;
import com.mini.rpc.handler.RpcResponseHandler;
import com.mini.rpc.protocol.MiniRpcProtocol;
import com.mini.rpc.provider.registry.RegistryService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcConsumer {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public RpcConsumer() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new MiniRpcEncoder())
                                .addLast(new MiniRpcDecoder())
                                .addLast(new RpcResponseHandler());
                    }
                });
    }

    public void sendRequest(MiniRpcProtocol<MiniRpcRequest> protocol, RegistryService registryService) throws Exception {
        MiniRpcRequest request = protocol.getBody();

        //获取参数值，注意：不是参数类型
        Object[] params = request.getParams();
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getServiceVersion());

        /**
         这里有一个小技巧，为了尽可能使所有服务节点收到的请求流量更加均匀，需要为 discovery() 提供一个 invokerHashCode，
         一般可以采用 RPC 服务接口参数列表中第一个参数的 hashCode 作为参考依据
         大于0：请求调用服务，  <= 0 的情况还没想好，可能是心跳包吗
         */
        int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();


        /**
         发起 RPC 调用之前，我们需要找到最合适的服务节点，直接调用注册中心服务 RegistryService
         的 discovery() 方法即可，默认是采用一致性 Hash 算法实现的服务发现
         找到服务节点
         */
        ServiceMeta serviceMetadata = registryService.discovery(serviceKey, invokerHashCode);

        //接下来通过 Netty 建立 TCP 连接，然后调用 writeAndFlush() 方法将数据发送到远端服务节点。
        if (serviceMetadata != null) {
            ChannelFuture future = bootstrap.connect(serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort()).sync();

            //https://blog.csdn.net/lgj123xj/article/details/78577945 channel知识点
            //监听channel的情况，一旦 ！isSuccess，则代表通道关闭，需要优雅关掉线程池，停止请求服务
            future.addListener((ChannelFutureListener) arg0 -> {
                if (future.isSuccess()) {
                    log.info("connect rpc server {} on port {} success.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                } else {
                    log.error("connect rpc server {} on port {} failed.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            });
            future.channel().writeAndFlush(protocol);
        }
    }
}
