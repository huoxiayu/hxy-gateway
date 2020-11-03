package com.hxy.filter;

import com.google.common.collect.ImmutableList;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;

public class FilterChain implements Filter {

    private final List<Filter> filters = ImmutableList.of(new HttpHeaderFilter());

    @Override
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        filters.forEach(filter -> filter.filter(fullRequest, ctx));
    }

}
