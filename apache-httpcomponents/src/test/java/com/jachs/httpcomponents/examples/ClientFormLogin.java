package com.jachs.httpcomponents.examples;


import java.net.URI;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

/**
 * A example that demonstrates how HttpClient APIs can be used to perform
 * form-based logon.
 * 一个示例，演示如何使用HttpClient api执行基于表单的登录。
 */
public class ClientFormLogin {

    public static void main(final String[] args) throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (final CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            final HttpGet httpget = new HttpGet("https://someportal/");
            try (final CloseableHttpResponse response1 = httpclient.execute(httpget)) {
                final HttpEntity entity = response1.getEntity();

                System.out.println("Login form get: " + response1.getCode() + " " + response1.getReasonPhrase());
                EntityUtils.consume(entity);

                System.out.println("Initial set of cookies:");
                final List<Cookie> cookies = cookieStore.getCookies();
                if (cookies.isEmpty()) {
                    System.out.println("None");
                } else {
                    for (int i = 0; i < cookies.size(); i++) {
                        System.out.println("- " + cookies.get(i));
                    }
                }
            }

            final ClassicHttpRequest login = ClassicRequestBuilder.post()
                    .setUri(new URI("https://someportal/"))
                    .addParameter("IDToken1", "username")
                    .addParameter("IDToken2", "password")
                    .build();
            try (final CloseableHttpResponse response2 = httpclient.execute(login)) {
                final HttpEntity entity = response2.getEntity();

                System.out.println("Login form get: " + response2.getCode() + " " + response2.getReasonPhrase());
                EntityUtils.consume(entity);

                System.out.println("Post logon cookies:");
                final List<Cookie> cookies = cookieStore.getCookies();
                if (cookies.isEmpty()) {
                    System.out.println("None");
                } else {
                    for (int i = 0; i < cookies.size(); i++) {
                        System.out.println("- " + cookies.get(i));
                    }
                }
            }
        }
    }
}