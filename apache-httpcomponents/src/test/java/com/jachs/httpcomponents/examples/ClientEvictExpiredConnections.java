package com.jachs.httpcomponents.examples;



import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.pool.PoolStats;
import org.apache.hc.core5.util.TimeValue;

/**
 * Example demonstrating how to evict expired and idle connections from the connection pool.
 * 演示如何从连接池中逐出过期和空闲连接的示例。
 */
public class ClientEvictExpiredConnections {

    public static void main(final String[] args) throws Exception {
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        try (final CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(5))
                .build()) {
            // create an array of URIs to perform GETs on
            final String[] urisToGet = {
                    "http://hc.apache.org/",
                    "http://hc.apache.org/httpcomponents-core-ga/",
                    "http://hc.apache.org/httpcomponents-client-ga/",
            };

            for (final String requestURI : urisToGet) {
                final HttpGet request = new HttpGet(requestURI);

                System.out.println("Executing request " + request.getMethod() + " " + request.getRequestUri());

                try (final CloseableHttpResponse response = httpclient.execute(request)) {
                    System.out.println("----------------------------------------");
                    System.out.println(response.getCode() + " " + response.getReasonPhrase());
                    EntityUtils.consume(response.getEntity());
                }
            }

            final PoolStats stats1 = cm.getTotalStats();
            System.out.println("Connections kept alive: " + stats1.getAvailable());

            // Sleep 10 sec and let the connection evictor do its job
            Thread.sleep(10000);

            final PoolStats stats2 = cm.getTotalStats();
            System.out.println("Connections kept alive: " + stats2.getAvailable());

        }
    }

}