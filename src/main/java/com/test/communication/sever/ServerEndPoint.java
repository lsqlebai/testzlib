package com.test.communication.sever;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;


/**
 * Created by lsq on 2017/5/9.
 */
public class ServerEndPoint {
    GameChannelHandlerRegister register = new GameChannelHandlerRegister(null);
    public GameChannelHandlerRegister getRegister() {
        return register;
    }

    public void setRegister(GameChannelHandlerRegister register) {
        this.register = register;
    }



    static final int PSIZE = Integer.parseInt(System.getProperty("parent_size", "1"));
    static final int CSIZE = Integer.parseInt(System.getProperty("child_size", "5"));
    //    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("proxy.server.port", "12345"));
    private EventLoopGroup parent = null;
    private EventLoopGroup child = null;
    private ChannelFuture channelFuture = null;

    public void start() {
        new Thread() {
            @Override
            public void run() {
                try {
                    parent = new NioEventLoopGroup(PSIZE);
                    child = new NioEventLoopGroup(CSIZE);
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(parent, child);
                    bootstrap.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10);
                    bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
                    bootstrap.channel(NioServerSocketChannel.class);
                    bootstrap.childHandler(register);
                    channelFuture = bootstrap.bind(PORT).sync();

                    channelFuture.channel().closeFuture().sync();
                } catch (Exception ex) {

                }
            }
        }.start();
    }

    public void stop() {
        try {
            child.shutdownGracefully();
            parent.shutdownGracefully();
            channelFuture.channel().close().sync();
//            serverRuntimeContext.getEndpointRegistry().unregister(this);
        } catch (InterruptedException e) {

        }
    }
}
