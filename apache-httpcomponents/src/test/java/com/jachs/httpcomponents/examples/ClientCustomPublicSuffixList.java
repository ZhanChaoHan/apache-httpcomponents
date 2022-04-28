package com.jachs.httpcomponents.examples;


import java.net.URL;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.cookie.CookieSpecFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.cookie.RFC6265CookieSpecFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.psl.PublicSuffixMatcher;
import org.apache.hc.client5.http.psl.PublicSuffixMatcherLoader;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContexts;

/**
 * This example demonstrates how to use a custom public suffix list.
 * 此示例演示如何使用自定义公共后缀列表。
 */
public class ClientCustomPublicSuffixList {

    public static void main(final String[] args) throws Exception {

        // Use PublicSuffixMatcherLoader to load public suffix list from a file,
        // resource or from an arbitrary URL
        final PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.load(
                new URL("https://publicsuffix.org/list/effective_tld_names.dat"));

        // Please use the publicsuffix.org URL to download the list no more than once per day !!!
        // Please consider making a local copy !!!

        final RFC6265CookieSpecFactory cookieSpecFactory = new RFC6265CookieSpecFactory(publicSuffixMatcher);
        final Lookup<CookieSpecFactory> cookieSpecRegistry = RegistryBuilder.<CookieSpecFactory>create()
                .register(StandardCookieSpec.RELAXED, cookieSpecFactory)
                .register(StandardCookieSpec.STRICT, cookieSpecFactory)
                .build();
        final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                SSLContexts.createDefault(),
                new DefaultHostnameVerifier(publicSuffixMatcher));
        final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslsf)
                .build();
        try (final CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build()) {

            final HttpGet httpget = new HttpGet("https://httpbin.org/get");

            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());

            try (final CloseableHttpResponse response = httpclient.execute(httpget)) {
                System.out.println("----------------------------------------");
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                System.out.println(EntityUtils.toString(response.getEntity()));
            }
        }
    }

}