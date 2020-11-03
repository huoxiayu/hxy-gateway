package com.hxy.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpHeaderFilter implements Filter {

    @Override
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        fullRequest.headers().add("timestamp", System.currentTimeMillis());
    }

}
