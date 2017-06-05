package com.test.communication.client;


import com.iflytek.cdstd.hungrycell.tcpserver.buff.GameBuff;
import com.iflytek.cdstd.hungrycell.tcpserver.buff.GameServer;
import com.test.communication.sever.GameChannelHandlerRegister;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by lsq on 2017/5/9.
 */
public class GameServerClient {
    static final String HOST = System.getProperty("proxy.server.host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("proxy.server.port", "12345"));

    static final int RETRY_TIME = 1000;
    final List<Channel> channels = new ArrayList<>();
    Bootstrap b = new Bootstrap();

    public GameChannelHandlerRegister getRegister() {
        return register;
    }

    public void setRegister(GameChannelHandlerRegister register) {
        this.register = register;
    }

    GameChannelHandlerRegister register = new GameChannelHandlerRegister(null);

    public void init() {
        initBootstrap();
        new Thread(() -> {
            try {
                Thread.sleep(RETRY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Channel channel = connect();
            register(channel);
            channel = null;
        }).start();
    }

    private void register(Channel channel) {
        if (channel == null) return;
        synchronized (channels) {
            channels.add(channel);
        }
        if (heartFuture != null) {
            return;
        }
        heartToServer();
    }

    private class HeartRunnable implements Runnable {

        @Override
        public void run() {
            send1();
            send1();
            send2();
            send3();
        }
    }

    private void send1() {
        GameBuff.MessageInfo.Builder builder = GameBuff.MessageInfo.newBuilder();
        builder.setSN(0);
        builder.setService(12);
        builder.setGameStateResponse(GameBuff.GameStateResponse.newBuilder().setState(0));
        GameServer.ServerData.Builder serverbuilder=  GameServer.ServerData.newBuilder();
        serverbuilder.setService(1);
        serverbuilder.setMessageInfo(builder);
        channels.get(0).writeAndFlush(serverbuilder.build());
    }

    private void send2() {
        GameBuff.MessageInfo.Builder builder = GameBuff.MessageInfo.newBuilder();
        builder.setSN(2);
        builder.setService(2);
        builder.setCode(200).setMsg("加入中，请等待");
        GameServer.ServerData.Builder serverbuilder=  GameServer.ServerData.newBuilder();
        serverbuilder.setService(1);
        serverbuilder.setMessageInfo(builder);
        channels.get(0).writeAndFlush(serverbuilder.build());
    }
    private void send3() {
        GameServer.ServerData.Builder serverbuilder = GameServer.ServerData.newBuilder();
        serverbuilder.setService(1);
        for(int i = 0;i < 100000; i++) {
            serverbuilder.addUids(i);
        }
        channels.get(0).writeAndFlush(serverbuilder.build());
    }
    private void heartToServer() {
        heartFuture = group.schedule(heartRunnable, 1, TimeUnit.SECONDS);
    }

    ScheduledFuture heartFuture = null;
    EventLoopGroup group = new NioEventLoopGroup();
    HeartRunnable heartRunnable = new HeartRunnable();

    private void initBootstrap() {
        b.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(register);
    }

    public Channel connect() {
        try {

            return b.connect(HOST, PORT).sync().channel();
        } catch (Exception e) {

        }
        return null;
    }

    public void reconnect(Channel disConnectChannel) {
        remove(disConnectChannel);
        try {
            Thread.sleep(RETRY_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Channel channel = connect();
        register(channel);
    }

    private void remove(Channel disConnectChannel) {
        synchronized (channels) {
            channels.remove(disConnectChannel);
        }
        if (heartFuture != null) {
            heartFuture.cancel(true);
            heartFuture = null;
        }
    }
}
