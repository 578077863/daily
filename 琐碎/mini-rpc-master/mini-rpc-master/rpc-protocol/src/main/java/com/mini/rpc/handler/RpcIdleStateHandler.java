package com.mini.rpc.handler;
/**
 @Description
 @author ZJF
 @create 2022-04-05-20:50
 @version */

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 *
 *@author:ZJF
 *@Date 2022-04-05-20:50
 *@description:超过60秒没有读时间,服务提供者就关掉通道
 */
@Slf4j
public class RpcIdleStateHandler extends ChannelInboundHandlerAdapter {

    /** 空闲次数 */
    private int idle_count = 1;
    /** 发送次数 */
    private int count = 1;

    public RpcIdleStateHandler(){

        //        super(2,0,0,TimeUnit.SECONDS);
    }

//    @Override
//    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
//
//        log.info("20 秒未读到数据，关闭连接");
//        ctx.channel().close();
//    }

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
//            }else if(IdleState.WRITER_IDLE.equals(event.state())){

//                ctx.channel().writeAndFlush(HEAP)
//            }
        }
    }
}
