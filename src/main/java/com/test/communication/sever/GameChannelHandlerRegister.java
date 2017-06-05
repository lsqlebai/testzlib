package com.test.communication.sever;


import io.netty.channel.*;
import io.netty.handler.codec.compression.JdkZlibDecoder;
import io.netty.handler.codec.compression.JdkZlibEncoder;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * Created by lsq on 2017/5/9.
 */

public class GameChannelHandlerRegister extends ChannelInitializer {

    SimpleChannelInboundHandler handler;

    private static final int ALL_IDLE_TIME_OUT = 0;
    private static final int READ_IDLE_TIME_OUT = 150;
    private static final int WRITE_IDLE_TIME_OUT = 0;

    public GameChannelHandlerRegister(SimpleChannelInboundHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //receive
        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        pipeline.addLast(new JdkZlibDecoder(ZlibWrapper.NONE));
        //change to jzlib can be work
        //pipeline.addLast(new JZlibDecoder(ZlibWrapper.NONE));
        pipeline.addLast(new ProtobufDecoder(com.iflytek.cdstd.hungrycell.tcpserver.buff.GameServer.ServerData.getDefaultInstance()));
        //send

        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast(new JdkZlibEncoder(ZlibWrapper.NONE));
        //change to jzlib can be work
        //pipeline.addLast(new JZlibDecoder(ZlibWrapper.NONE));
        pipeline.addLast(new ProtobufEncoder());

//            pipeline.addLast("timeout", new IdleStateHandler(READ_IDLE_TIME_OUT, WRITE_IDLE_TIME_OUT, ALL_IDLE_TIME_OUT));

        pipeline.addLast("handler", new SimpleChannelInboundHandler() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println(msg);
            }
        });
    }
}
