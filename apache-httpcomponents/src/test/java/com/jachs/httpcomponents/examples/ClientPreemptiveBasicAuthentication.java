package com.jachs.httpcomponents.examples;

import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;

/**
 * An example of how HttpClient can be customized to authenticate
 * preemptively using Basic scheme.
 * <b/>
 * Generally, preemptive authentication can be considered less
 * secure than a response to an authentication challenge
 * and therefore discouraged.
 * 如何定制HttpClient进行身份验证的示例抢先使用基本方案。一般来说，抢占式身份验证可以考虑较少比响应身份验证质询更安全因此气馁。
 */
public class ClientPreemptiveBasicAuthentication {

    public static void main(final String[] args) throws Exception {
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {

            // Generate Basic scheme object and add it to the local auth cache
            final BasicScheme basicAuth = new BasicScheme();
            basicAuth.initPreemptive(new UsernamePasswordCredentials("user", "passwd".toCharArray()));

            final HttpHost target = new HttpHost("http", "httpbin.org", 80);

            // Add AuthCache to the execution context
            final HttpClientContext localContext = HttpClientContext.create();
            localContext.resetAuthExchange(target, basicAuth);

            final HttpGet httpget = new HttpGet("http://httpbin.org/hidden-basic-auth/user/passwd");

            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());
            for (int i = 0; i < 3; i++) {
                try (final CloseableHttpResponse response = httpclient.execute(httpget, localContext)) {
                    System.out.println("----------------------------------------");
                    System.out.println(response.getCode() + " " + response.getReasonPhrase());
                    System.out.println(EntityUtils.toString(response.getEntity()));
                }
            }
        }
    }

}