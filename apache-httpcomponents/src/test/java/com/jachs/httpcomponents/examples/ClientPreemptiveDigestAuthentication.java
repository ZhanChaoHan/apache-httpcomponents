package com.jachs.httpcomponents.examples;

import org.apache.hc.client5.http.auth.AuthExchange;
import org.apache.hc.client5.http.auth.AuthScheme;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.DigestScheme;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;

/**
 * An example of how HttpClient can authenticate multiple requests
 * using the same Digest scheme. After the initial request / response exchange
 * all subsequent requests sharing the same execution context can re-use
 * the last Digest nonce value to authenticate with the server.
HttpClient如何对多个请求进行身份验证的示例
使用相同的摘要方案。在初始请求/响应交换后
共享同一执行上下文的所有后续请求都可以重用
要向服务器进行身份验证的最后一个摘要nonce值。
 */
public class ClientPreemptiveDigestAuthentication {

    public static void main(final String[] args) throws Exception {
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {

            final HttpHost target = new HttpHost("http", "httpbin.org", 80);

            final HttpClientContext localContext = HttpClientContext.create();
            final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(target),
                    new UsernamePasswordCredentials("user", "passwd".toCharArray()));
            localContext.setCredentialsProvider(credentialsProvider);

            final HttpGet httpget = new HttpGet("http://httpbin.org/digest-auth/auth/user/passwd");

            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());
            for (int i = 0; i < 3; i++) {
                try (final CloseableHttpResponse response = httpclient.execute(target, httpget, localContext)) {
                    System.out.println("----------------------------------------");
                    System.out.println(response.getCode() + " " + response.getReasonPhrase());
                    EntityUtils.consume(response.getEntity());

                    final AuthExchange authExchange = localContext.getAuthExchange(target);
                    if (authExchange != null) {
                        final AuthScheme authScheme = authExchange.getAuthScheme();
                        if (authScheme instanceof DigestScheme) {
                            final DigestScheme digestScheme = (DigestScheme) authScheme;
                            System.out.println("Nonce: " + digestScheme.getNonce() +
                                    "; count: " + digestScheme.getNounceCount());
                        }
                    }
                }
            }
        }
    }

}