package com.mini.rpc.handler;

import com.mini.rpc.common.MiniRpcRequest;
import com.mini.rpc.common.MiniRpcResponse;
import com.mini.rpc.common.RpcServiceHelper;
import com.mini.rpc.protocol.MiniRpcProtocol;
import com.mini.rpc.protocol.MsgHeader;
import com.mini.rpc.protocol.MsgStatus;
import com.mini.rpc.protocol.MsgType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;

import java.util.Map;

@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<MiniRpcProtocol<MiniRpcRequest>> {

    /** 空闲次数 */
    private int idle_count = 1;
    /** 发送次数 */
    private int count = 1;

    private final Map<String, Object> rpcServiceMap;

    public RpcRequestHandler(Map<String, Object> rpcServiceMap) {
        this.rpcServiceMap = rpcServiceMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MiniRpcProtocol<MiniRpcRequest> protocol) {

        byte msgType = protocol.getHeader().getMsgType();

        MsgType msgTypeEnum = MsgType.findByType(msgType);

        if (MsgType.HEARTBEAT.equals(msgTypeEnum)) {
            log.info("收到来自客户端 : {} 的心跳包", ctx.channel().remoteAddress());
        } else {

            //基于一个线程池去执行
            RpcRequestProcessor.submitRequest(() -> {
                MiniRpcProtocol<MiniRpcResponse> resProtocol = new MiniRpcProtocol<>();
                MiniRpcResponse response = new MiniRpcResponse();
                MsgHeader header = protocol.getHeader();
                header.setMsgType((byte) MsgType.RESPONSE.getType());
                try {
                    Object result = handle(protocol.getBody());
                    response.setData(result);

                    header.setStatus((byte) MsgStatus.SUCCESS.getCode());
                    resProtocol.setHeader(header);
                    resProtocol.setBody(response);
                } catch (Throwable throwable) {
                    header.setStatus((byte) MsgStatus.FAIL.getCode());
                    response.setMessage(throwable.toString());
                    log.error("process request {} error", header.getRequestId(), throwable);
                }
                ctx.writeAndFlush(resProtocol);
            });
        }
    }

    private Object handle(MiniRpcRequest request) throws Throwable {
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getServiceVersion());
        Object serviceBean = rpcServiceMap.get(serviceKey);

        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }

        //拿到对应
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParams();

        //https://www.jianshu.com/p/0604d79435f1 我滴乖乖，这大有学问啊   https://xie.infoq.cn/article/8b19942af9e5b9ce290aa1f94?utm_source=related_read&utm_medium=article
        //FastClass 可以提供比 Java 中反射更快的执行速度。
        // Java 中的反射是通过 JNI 本地调用来执行反射的代码，而 FastClass 则是直接生成字节码文件被 JVM 执行。

        /**

         rpcServiceMap 中存放着服务提供者所有对外发布的服务接口，我们可以通过服务名和服务版本找到对应的服务接口。
         通过服务接口、方法名、方法参数列表、参数类型列表，我们一般可以使用反射的方式执行方法调用。为了加速服务接口调用的性能，我们采用 Cglib 提供的 FastClass 机制直接调用方法，
         Cglib 中 MethodProxy 对象就是采用了 FastClass 机制，它可以和 Method 对象完成同样的事情，但是相比于反射性能更高。

         FastClass 机制并没有采用反射的方式调用被代理的方法，而是运行时动态生成一个新的 FastClass 子类，
         向子类中写入直接调用目标方法的逻辑。同时该子类会为代理类分配一个 int 类型的 index 索引，FastClass 即可通过 index 索引定位到需要调用的方法。
         */

        FastClass fastClass = FastClass.create(serviceClass);
        int methodIndex = fastClass.getIndex(methodName, parameterTypes);
        return fastClass.invoke(methodIndex, serviceBean, parameters);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){

            IdleStateEvent event = (IdleStateEvent) evt;

            if(IdleState.READER_IDLE.equals(event.state())) {

                if (idle_count > 2) {
                    log.info("{} 超过两次无客户端请求，关闭该channel", ctx.channel().remoteAddress());
                    ctx.channel().close();
                } else {
                    log.info("{} 经过一定时间还没有发送消息", ctx.channel().remoteAddress());
                    idle_count++;
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info(cause.getMessage());

        rpcServiceMap.remove(ctx.channel().remoteAddress().toString());

        log.info("{} 出现异常已断开", ctx.channel().remoteAddress());
    }
}
