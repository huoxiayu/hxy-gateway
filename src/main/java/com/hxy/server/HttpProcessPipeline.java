package com.hxy.server;

import com.hxy.filter.FilterChain;
import com.hxy.proxy.HttpProxy;
import com.hxy.route.UrlMappingHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpProcessPipeline {

    private final FilterChain filterChain = new FilterChain();
    private final UrlMappingHandler urlMappingHandler = new UrlMappingHandler();
    private final HttpProxy httpProxy = new HttpProxy();

    public void process(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        filterChain.filter(fullRequest, ctx);

        String mappingUrl = urlMappingHandler.mapping(fullRequest);

        httpProxy.forwardHttpRequest(mappingUrl, fullRequest, ctx);
    }

}
