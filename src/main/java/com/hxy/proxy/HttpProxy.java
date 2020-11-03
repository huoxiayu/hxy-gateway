package com.hxy.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.internal.SystemPropertyUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpProxy {

    private final HttpClient httpClient;

    public HttpProxy() {
        boolean netty = SystemPropertyUtil.getBoolean("use.netty.client", false);
        this.httpClient = netty ? new NettyHttpClient() : new OkHttpClient();
        log.info("choose http client {}", this.httpClient.getClass().getSimpleName());
    }

    public void forwardHttpRequest(String url,
                                   FullHttpRequest fullRequest,
                                   ChannelHandlerContext ctx) {
        httpClient.processHttpRequest(url, fullRequest, ctx);
    }

}
