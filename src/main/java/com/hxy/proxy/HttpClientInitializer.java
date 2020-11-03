package com.hxy.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;

import java.util.concurrent.atomic.AtomicReference;

public class HttpClientInitializer extends ChannelInitializer<SocketChannel> {

    private final AtomicReference<ChannelHandlerContext> reponseChannelRef;

    public HttpClientInitializer(AtomicReference<ChannelHandlerContext> responseChannelRef) {
        this.reponseChannelRef = responseChannelRef;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpClientCodec());
        p.addLast(new HttpContentDecompressor());
        p.addLast(new HttpClientHandler(reponseChannelRef));
    }

}
