package com.jachs.httpcomponents.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.impl.BasicEntityDetails;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

/**
 * This example demonstrates how to insert custom request interceptor and an execution interceptor
 * to the request execution chain.
 * 此示例演示如何插入自定义请求拦截器和执行拦截器到请求执行链。
 */
public class AsyncClientInterceptors {

    public static void main(final String[] args) throws Exception {

        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();

        final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)

                // Add a simple request ID to each outgoing request

                .addRequestInterceptorFirst(new HttpRequestInterceptor() {

                    private final AtomicLong count = new AtomicLong(0);

                    public void process(
                            final HttpRequest request,
                            final EntityDetails entity,
                            final HttpContext context) throws HttpException, IOException {
                        request.setHeader("request-id", Long.toString(count.incrementAndGet()));
                    }
                })

                // Simulate a 404 response for some requests without passing the message down to the backend

                .addExecInterceptorAfter(ChainElement.PROTOCOL.name(), "custom", new AsyncExecChainHandler() {

                    public void execute(
                            final HttpRequest request,
                            final AsyncEntityProducer requestEntityProducer,
                            final AsyncExecChain.Scope scope,
                            final AsyncExecChain chain,
                            final AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
                        final Header idHeader = request.getFirstHeader("request-id");
                        if (idHeader != null && "13".equalsIgnoreCase(idHeader.getValue())) {
                            final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_NOT_FOUND, "Oppsie");
                            final ByteBuffer content = ByteBuffer.wrap("bad luck".getBytes(StandardCharsets.US_ASCII));
                            final AsyncDataConsumer asyncDataConsumer = asyncExecCallback.handleResponse(
                                    response,
                                    new BasicEntityDetails(content.remaining(), ContentType.TEXT_PLAIN));
                            asyncDataConsumer.consume(content);
                            asyncDataConsumer.streamEnd(null);
                        } else {
                            chain.proceed(request, requestEntityProducer, scope, asyncExecCallback);
                        }
                    }

                })

                .build();

        client.start();

        final String requestUri = "http://httpbin.org/get";
        for (int i = 0; i < 20; i++) {
            final SimpleHttpRequest httpget = SimpleHttpRequests.get(requestUri);
            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());
            final Future<SimpleHttpResponse> future = client.execute(
                    httpget,
                    new FutureCallback<SimpleHttpResponse>() {

                        public void completed(final SimpleHttpResponse response) {
                            System.out.println(requestUri + "->" + response.getCode());
                            System.out.println(response.getBody());
                        }

                        public void failed(final Exception ex) {
                            System.out.println(requestUri + "->" + ex);
                        }

                        public void cancelled() {
                            System.out.println(requestUri + " cancelled");
                        }

                    });
            future.get();
        }

        System.out.println("Shutting down");
        client.close(CloseMode.GRACEFUL);
    }

}
