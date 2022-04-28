package com.jachs.httpcomponents.examples;


import java.io.IOException;
import java.io.InputStream;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;

/**
 * This example demonstrates the recommended way of using API to make sure
 * the underlying connection gets released back to the connection manager.
 * 这个例子演示了使用API来确保
 * 基础连接被释放回连接管理器。
 * @see http://hc.apache.org/httpcomponents-client-5.0.x/httpclient5/examples/ClientConnectionRelease.java
 */
public class ClientConnectionRelease {

    public final static void main(final String[] args) throws Exception {
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpGet httpget = new HttpGet("http://httpbin.org/get");

            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());
            try (final CloseableHttpResponse response = httpclient.execute(httpget)) {
                System.out.println("----------------------------------------");
                System.out.println(response.getCode() + " " + response.getReasonPhrase());

                // Get hold of the response entity
                final HttpEntity entity = response.getEntity();

                // If the response does not enclose an entity, there is no need
                // to bother about connection release
                if (entity != null) {
                    try (final InputStream inStream = entity.getContent()) {
                        inStream.read();
                        // do something useful with the response
                    } catch (final IOException ex) {
                        // In case of an IOException the connection will be released
                        // back to the connection manager automatically
                        throw ex;
                    }
                }
            }
        }
    }

}
