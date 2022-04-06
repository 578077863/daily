package com.mini.rpc.consumer.handler;
/**
 @Description
 @author ZJF
 @create 2022-04-05-20:52
 @version */

import com.mini.rpc.common.MiniRpcRequest;
import com.mini.rpc.common.MiniRpcRequestHolder;
import com.mini.rpc.consumer.RpcConsumer;
import com.mini.rpc.protocol.*;
import com.mini.rpc.serialization.SerializationTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.TimeUnit;

/**
 *
 *@author:ZJF
 *@Date 2022-04-05-20:52
 *@description:
 */
public class RpcHeartBeatHandler extends ChannelInboundHandlerAdapter{

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        doHeartBeatTask(ctx);
    }

    //TODO
    public void doHeartBeatTask(ChannelHandlerContext ctx) {
//        ctx.executor().schedule(() -> {
//            if (ctx.channel().isActive()) {
//                HeartBeatData heartBeatData = buildHeartBeatData();
//
//                ctx.channel().writeAndFlush(heartBeatData.getRpcProtocol());
////                ctx.writeAndFlush(heartBeatData.getRpcProtocol());
//
//                System.out.println("发了,别催了");
//                doHeartBeatTask(ctx);
//            }
//        }, 2, TimeUnit.SECONDS);

        ctx.executor().schedule(()->{

            if(ctx.channel().isActive()){
                MiniRpcProtocol heartBeatData = buildHeartBeatData();

                ctx.channel().writeAndFlush(heartBeatData);
                //                ctx.writeAndFlush(heartBeatData.getRpcProtocol());


                doHeartBeatTask(ctx);
            }
        },2,TimeUnit.SECONDS);

//        while(true){
//            if(ctx.channel().isActive()){
//                HeartBeatData heartBeatData = buildHeartBeatData();
//
//                ctx.channel().writeAndFlush(heartBeatData.getRpcProtocol());
//                //                ctx.writeAndFlush(heartBeatData.getRpcProtocol());
//
//                System.out.println("发了,别催了");
//
//                Thread.sleep(100);
//            }
//        }
    }


    public MiniRpcProtocol buildHeartBeatData(){


        HeartBeatData heartBeatData = new HeartBeatData();

        MiniRpcProtocol rpcProtocol = new MiniRpcProtocol();

        MsgHeader header = new MsgHeader();

        long requestId = MiniRpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(requestId);
        header.setSerialization((byte) SerializationTypeEnum.HESSIAN.getType());
        header.setMsgType((byte) MsgType.HEARTBEAT.getType());
        header.setStatus((byte) 0x1);

        MiniRpcRequest request = new MiniRpcRequest();
        //整成心跳包
        rpcProtocol.setHeader(header);
        rpcProtocol.setBody(request);

        return rpcProtocol;
    }
}
