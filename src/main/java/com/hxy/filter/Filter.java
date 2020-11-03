package com.hxy.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface Filter {

    void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx);

}
