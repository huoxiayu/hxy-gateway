package com.hxy.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class NettyHttpClient implements HttpClient {

    private static final int MAX_REQ_PER_HOST = 10;
    private static final ConcurrentHashMap<String, List<InnerNettyClient>> HOST_AND_PORT_2_CLIENTS = new ConcurrentHashMap<>();

    private static class InnerNettyClient {
        private static final AtomicInteger CNT = new AtomicInteger();

        private final AtomicReference<ChannelHandlerContext> responseChannel = new AtomicReference<>();
        private final Channel channel;

        private InnerNettyClient(String host, int port) {
            this.channel = initChannel(host, port);
        }

        private Channel initChannel(String host, int port) {
            log.info("current channel cnt {}", CNT.incrementAndGet());
            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new HttpClientInitializer(responseChannel));
            try {
                return b.connect(host, port).sync().channel();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private void process(FullHttpRequest request) {
            this.channel.writeAndFlush(request);
        }

        private void updateResponseChannelReference(ChannelHandlerContext ctx) {
            responseChannel.compareAndSet(null, ctx);
        }

    }

    @Override
    public void processHttpRequest(String url, FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            int port = uri.getPort();
            InnerNettyClient client = chooseClient(host, port);
            client.updateResponseChannelReference(ctx);
            client.process(fullRequest);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private InnerNettyClient chooseClient(String host, int port) {
        String hostAndPort = host + "-" + port;
        List<InnerNettyClient> clients = HOST_AND_PORT_2_CLIENTS.get(hostAndPort);
        if (clients == null) {
            synchronized (this) {
                clients = HOST_AND_PORT_2_CLIENTS.get(hostAndPort);
                if (clients == null) {
                    clients = new ArrayList<>(MAX_REQ_PER_HOST);
                    for (int i = 0; i < MAX_REQ_PER_HOST; i++) {
                        clients.add(new InnerNettyClient(host, port));
                    }
                    HOST_AND_PORT_2_CLIENTS.put(hostAndPort, clients);
                    log.info("add clients for {}", hostAndPort);
                }
            }
        }

        int idx = Math.abs(ThreadLocalRandom.current().nextInt()) % MAX_REQ_PER_HOST;
        return clients.get(idx);
    }

}
