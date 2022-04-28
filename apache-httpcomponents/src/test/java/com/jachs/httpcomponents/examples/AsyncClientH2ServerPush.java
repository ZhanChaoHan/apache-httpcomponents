package com.jachs.httpcomponents.examples;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.async.methods.AbstractBinPushConsumer;
import org.apache.hc.client5.http.async.methods.AbstractCharResponseConsumer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

/**
 * This example demonstrates handling of HTTP/2 message exchanges pushed by the server.
 * 此示例演示如何处理服务器推送的HTTP/2消息交换。
 */
public class AsyncClientH2ServerPush {

    public static void main(final String[] args) throws Exception {

        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();

        final H2Config h2Config = H2Config.custom()
                .setPushEnabled(true)
                .build();

        final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_2)
                .setH2Config(h2Config)
                .build();

        client.start();

        client.register("*", new Supplier<AsyncPushConsumer>() {

            @Override
            public AsyncPushConsumer get() {
                return new AbstractBinPushConsumer() {

                    @Override
                    protected void start(
                            final HttpRequest promise,
                            final HttpResponse response,
                            final ContentType contentType) throws HttpException, IOException {
                        System.out.println(promise.getPath() + " (push)->" + new StatusLine(response));
                    }

                    @Override
                    protected int capacityIncrement() {
                        return Integer.MAX_VALUE;
                    }

                    @Override
                    protected void data(final ByteBuffer data, final boolean endOfStream) throws IOException {
                    }

                    @Override
                    protected void completed() {
                    }

                    @Override
                    public void failed(final Exception cause) {
                        System.out.println("(push)->" + cause);
                    }

                    @Override
                    public void releaseResources() {
                    }

                };
            }

        });

        final HttpHost target = new HttpHost("nghttp2.org");
        final String requestURI = "/httpbin/";
        final Future<Void> future = client.execute(
                new BasicRequestProducer(Method.GET, target, requestURI),
                new AbstractCharResponseConsumer<Void>() {

                    @Override
                    protected void start(
                            final HttpResponse response,
                            final ContentType contentType) throws HttpException, IOException {
                        System.out.println(requestURI + "->" + new StatusLine(response));
                    }

                    @Override
                    protected int capacityIncrement() {
                        return Integer.MAX_VALUE;
                    }

                    @Override
                    protected void data(final CharBuffer data, final boolean endOfStream) throws IOException {
                    }

                    @Override
                    protected Void buildResult() throws IOException {
                        return null;
                    }

                    @Override
                    public void failed(final Exception cause) {
                        System.out.println(requestURI + "->" + cause);
                    }

                    @Override
                    public void releaseResources() {
                    }

                }, null);
        future.get();

        System.out.println("Shutting down");
        client.close(CloseMode.GRACEFUL);
    }

}