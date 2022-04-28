package com.jachs.httpcomponents.examples;


import java.util.concurrent.Future;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.io.CloseMode;

/**
 * A simple example that uses HttpClient to execute an HTTP request against
 * a target site that requires user authentication.
 * 使用HttpClient对执行HTTP请求的简单示例,需要用户身份验证的目标站点。
 */
public class AsyncClientAuthentication {

    public static void main(final String[] args) throws Exception {
        final BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope("httpbin.org", 80),
                new UsernamePasswordCredentials("user", "passwd".toCharArray()));
        final CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        httpclient.start();

        final String requestUri = "http://httpbin.org/basic-auth/user/passwd";
        final SimpleHttpRequest httpget = SimpleHttpRequests.get(requestUri);

        System.out.println("Executing request " + requestUri);
        final Future<SimpleHttpResponse> future = httpclient.execute(
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

        System.out.println("Shutting down");
        httpclient.close(CloseMode.GRACEFUL);

    }
}