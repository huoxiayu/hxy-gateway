package com.hxy.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
public class HttpClientHandler extends ChannelInboundHandlerAdapter {

    private final AtomicReference<ChannelHandlerContext> responseChannel;

    public HttpClientHandler(AtomicReference<ChannelHandlerContext> responseChannel) {
        this.responseChannel = responseChannel;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            log.debug("response status {}", response.status());
        }

        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            ByteBuf bytes = content.content();

            if (log.isDebugEnabled()) {
                log.debug("response entity {}", bytes.toString(CharsetUtil.UTF_8));
            }

            if (content instanceof LastHttpContent) {
                FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(bytes));
                HttpHeaders httpHeaders = fullHttpResponse.headers();
                httpHeaders.set("Content-Type", "application/json;charset=UTF-8");
                httpHeaders.setInt("Content-Length", bytes.capacity());
                log.debug("fullHttpResponse: {}", fullHttpResponse);

                ChannelHandlerContext responseCtx = responseChannel.get();
                responseCtx.writeAndFlush(fullHttpResponse);

                // ctx.close();

                responseChannel.set(null);

                Object unlock = responseChannel;
                synchronized (unlock) {
                    unlock.notifyAll();
                    log.info("unlock obj {}", System.identityHashCode(unlock));

                }
            }
        }
    }

}
