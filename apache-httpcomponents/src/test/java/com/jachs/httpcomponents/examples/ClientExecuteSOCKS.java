package com.jachs.httpcomponents.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

/**
 * How to send a request via SOCKS proxy.
 *如何通过SOCKS代理发送请求。
 * @since 4.1
 */
public class ClientExecuteSOCKS {

    public static void main(final String[] args)throws Exception {
        final Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new MyConnectionSocketFactory())
                .build();
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
        try (final CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .build()) {
            final InetSocketAddress socksaddr = new InetSocketAddress("mysockshost", 1234);
            final HttpClientContext context = HttpClientContext.create();
            context.setAttribute("socks.address", socksaddr);

            final HttpHost target = new HttpHost("http", "httpbin.org", 80);
            final HttpGet request = new HttpGet("/get");

            System.out.println("Executing request " + request.getMethod() + " " + request.getUri() +
                    " via SOCKS proxy " + socksaddr);
            try (final CloseableHttpResponse response = httpclient.execute(target, request, context)) {
                System.out.println("----------------------------------------");
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                System.out.println(EntityUtils.toString(response.getEntity()));
            }
        }
    }

    static class MyConnectionSocketFactory implements ConnectionSocketFactory {

        @Override
        public Socket createSocket(final HttpContext context) throws IOException {
            final InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
            final Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
            return new Socket(proxy);
        }

        @Override
        public Socket connectSocket(
                final TimeValue connectTimeout,
                final Socket socket,
                final HttpHost host,
                final InetSocketAddress remoteAddress,
                final InetSocketAddress localAddress,
                final HttpContext context) throws IOException {
            final Socket sock;
            if (socket != null) {
                sock = socket;
            } else {
                sock = createSocket(context);
            }
            if (localAddress != null) {
                sock.bind(localAddress);
            }
            sock.connect(remoteAddress, connectTimeout != null ? connectTimeout.toMillisecondsIntBound() : 0);
            return sock;
        }

    }

}