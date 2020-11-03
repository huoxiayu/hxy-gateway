package com.hxy.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface HttpClient {

    void processHttpRequest(String url,
                            FullHttpRequest fullRequest,
                            ChannelHandlerContext ctx);

}
