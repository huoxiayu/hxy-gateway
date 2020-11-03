package com.hxy.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
public final class OkHttpClient implements HttpClient {

    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    private static final long THREAD_POOL_KEEP_ALIVE_TIMES_IN_MINUTES = 5L;
    private static final int QUEUE_SIZE = 1000;
    private static final int MAX_REQ_PER_HOST = 10;
    private static final int MAX_IDLE_CONNECTIONS = 2;
    private static final long KEEP_ALIVE_IN_MILLIS = 30L;
    private static final long TIME_OUT_IN_MILLIS = 200L;

    private final okhttp3.OkHttpClient client = client();

    private okhttp3.OkHttpClient client() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            THREAD_POOL_KEEP_ALIVE_TIMES_IN_MINUTES,
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(QUEUE_SIZE),
            Util.threadFactory("ok-http-dispatcher", false)
        );

        Dispatcher dispatcher = new Dispatcher(threadPoolExecutor);
        dispatcher.setMaxRequestsPerHost(MAX_REQ_PER_HOST);

        return new okhttp3.OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .connectionPool(new ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE_IN_MILLIS, TimeUnit.MILLISECONDS))
            .connectTimeout(TIME_OUT_IN_MILLIS, TimeUnit.MILLISECONDS)
            .readTimeout(TIME_OUT_IN_MILLIS, TimeUnit.MILLISECONDS)
            .writeTimeout(TIME_OUT_IN_MILLIS, TimeUnit.MILLISECONDS)
            .followRedirects(true)
            .build();
    }

    private void get(String url, Callback callback) {
        getCall(url).enqueue(callback);
    }

    private Call getCall(String url) {
        return client.newCall(new Request.Builder().url(url).build());
    }

    @Override
    public void processHttpRequest(String url, FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        get(url, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                log.error("unexpected exception {}", e);
                ctx.close();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    FullHttpResponse fullHttpResponse = okHttpResp2FullHttpResponse(response);
                    ctx.writeAndFlush(fullHttpResponse);
                } catch (IOException e) {
                    onFailure(call, e);
                }
            }

        });
    }

    private FullHttpResponse okHttpResp2FullHttpResponse(Response response) throws IOException {
        byte[] bytes = response.body().string().getBytes();
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(bytes));
        HttpHeaders httpHeaders = fullHttpResponse.headers();
        httpHeaders.set("Content-Type", "application/json;charset=UTF-8");
        httpHeaders.setInt("Content-Length", bytes.length);
        log.debug("fullHttpResponse: {}", fullHttpResponse);
        return fullHttpResponse;
    }

}
