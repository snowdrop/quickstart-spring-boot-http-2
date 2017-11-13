package com.example.mvc;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoHttp2Application.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@PropertySource("classpath:application.properties")
public class DemoHttp2JettyClientTest {

    @Test
    public void testHttp2clientUsingPort8080() {
        HttpClient httpClient = null;
        String response;
        int port = 8080;
        try {
            httpClient = new HttpClient( new HttpClientTransportOverHTTP2( new HTTP2Client() ),
                    new SslContextFactory( false ) );
            Assert.assertNotNull(httpClient);
            httpClient.start();

            Request request = httpClient.newRequest("http://localhost:" + port + "/");
            Assert.assertNotNull(request);

            response = request.send().getContentAsString();
            Assert.assertEquals("hello!", response);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                httpClient.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testHttp2clientUsingSSL() {
        HttpClient httpClient = null;
        String response;
        int port = 8443;
        try {
            httpClient = new HttpClient( new HttpClientTransportOverHTTP2( new HTTP2Client() ),
                    new SslContextFactory( true ) );
            Assert.assertNotNull(httpClient);
            httpClient.start();

            Request request = httpClient.newRequest("https://localhost:" + port + "/");
            Assert.assertNotNull(request);

            response = request.send().getContentAsString();
            Assert.assertEquals("hello!", response);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                httpClient.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}