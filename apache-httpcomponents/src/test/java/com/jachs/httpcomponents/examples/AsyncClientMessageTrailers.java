package com.jachs.httpcomponents.examples;

import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.entity.DigestingEntityProducer;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityProducer;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

/**
 * This example demonstrates how to use a custom execution interceptor
 * to add trailers to all outgoing request enclosing an entity.
 * 此示例演示如何使用自定义执行拦截器向包含实体的所有传出请求添加尾部。
 */
public class AsyncClientMessageTrailers {

    public final static void main(final String[] args) throws Exception {

        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();

        final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .addExecInterceptorAfter(ChainElement.PROTOCOL.name(), "custom", new AsyncExecChainHandler() {

                    @Override
                    public void execute(
                            final HttpRequest request,
                            final AsyncEntityProducer entityProducer,
                            final AsyncExecChain.Scope scope,
                            final AsyncExecChain chain,
                            final AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
                        // Send MD5 hash in a trailer by decorating the original entity producer
                        chain.proceed(
                                request,
                                entityProducer != null ? new DigestingEntityProducer("MD5", entityProducer) : null,
                                scope,
                                asyncExecCallback);
                    }

                })
                .build();

        client.start();

        final String requestUri = "http://httpbin.org/post";
        final AsyncRequestProducer requestProducer = AsyncRequestBuilder.post(requestUri)
                .setEntity(new StringAsyncEntityProducer("some stuff", ContentType.TEXT_PLAIN))
                .build();
        final Future<SimpleHttpResponse> future = client.execute(
                requestProducer,
                SimpleResponseConsumer.create(),
                new FutureCallback<SimpleHttpResponse>() {

                    @Override
                    public void completed(final SimpleHttpResponse response) {
                        System.out.println(requestUri + "->" + response.getCode());
                        System.out.println(response.getBody());
                    }

                    @Override
                    public void failed(final Exception ex) {
                        System.out.println(requestUri + "->" + ex);
                    }

                    @Override
                    public void cancelled() {
                        System.out.println(requestUri + " cancelled");
                    }

                });
        future.get();

        System.out.println("Shutting down");
        client.close(CloseMode.GRACEFUL);
    }

}
