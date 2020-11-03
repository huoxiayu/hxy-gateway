package com.hxy.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private static final HttpProcessPipeline HTTP_PROCESS_PIPELINE = new HttpProcessPipeline();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            HTTP_PROCESS_PIPELINE.process((FullHttpRequest) msg, ctx);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
