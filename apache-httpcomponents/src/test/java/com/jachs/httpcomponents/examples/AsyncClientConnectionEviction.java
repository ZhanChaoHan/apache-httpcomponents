package com.jachs.httpcomponents.examples;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

/**
 * Example demonstrating how to evict expired and idle connections
 * from the connection pool.
 * 演示如何从连接池中逐出过期和空闲连接的示例。
 */
public class AsyncClientConnectionEviction {

    public static void main(final String[] args) throws Exception {

        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();

        final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(10))
                .build();

        client.start();

        final HttpHost target = new HttpHost("httpbin.org");

        final SimpleHttpRequest request = SimpleHttpRequests.get(target, "/");
        final Future<SimpleHttpResponse> future1 = client.execute(
                SimpleRequestProducer.create(request),
                SimpleResponseConsumer.create(),
                new FutureCallback<SimpleHttpResponse>() {

                    public void completed(final SimpleHttpResponse response) {
                        System.out.println(request.getRequestUri() + "->" + response.getCode());
                        System.out.println(response.getBody());
                    }

                    public void failed(final Exception ex) {
                        System.out.println(request.getRequestUri() + "->" + ex);
                    }

                    public void cancelled() {
                        System.out.println(request.getRequestUri() + " cancelled");
                    }

                });

        future1.get();

        Thread.sleep(TimeUnit.SECONDS.toMillis(30));

        // Previous connection should get evicted from the pool by now

        final Future<SimpleHttpResponse> future2 = client.execute(
                SimpleRequestProducer.create(request),
                SimpleResponseConsumer.create(),
                new FutureCallback<SimpleHttpResponse>() {

                    public void completed(final SimpleHttpResponse response) {
                        System.out.println(request.getRequestUri() + "->" + response.getCode());
                        System.out.println(response.getBody());
                    }

                    public void failed(final Exception ex) {
                        System.out.println(request.getRequestUri() + "->" + ex);
                    }

                    public void cancelled() {
                        System.out.println(request.getRequestUri() + " cancelled");
                    }

                });

        future2.get();

        System.out.println("Shutting down");
        client.close(CloseMode.GRACEFUL);
    }

}