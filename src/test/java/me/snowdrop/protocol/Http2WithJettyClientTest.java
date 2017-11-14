package me.snowdrop.protocol;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Http2Application.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@PropertySource("classpath:application.properties")
public class Http2WithJettyClientTest {

    @Test
    public void testHttp2WithHttp2ClientAndHttp() throws Exception {
        long startTime = System.nanoTime();
        // create a Jetty HTTP/2 client
        HTTP2Client client = new HTTP2Client();
        client.start();

        // create a new session the represents a (multiplexed) connection to the server
        FuturePromise<Session> sessionFuture = new FuturePromise<>();
        client.connect(
                new InetSocketAddress("localhost", 8080),
                new Session.Listener.Adapter(), sessionFuture);

        // Obtain the client Session object.
        Session session = sessionFuture.get(5, TimeUnit.SECONDS);

        // Prepare the HTTP request headers.
        HttpFields requestFields = new HttpFields();
        requestFields.put("User-Agent", client.getClass().getName() + "/" + Jetty.VERSION);
        // Prepare the HTTP request object.
        MetaData.Request request = new MetaData.Request("GET", new HttpURI("https://localhost:8080/"), HttpVersion.HTTP_2, requestFields);
        // Create the HTTP/2 HEADERS frame representing the HTTP request.
        HeadersFrame headersFrame = new HeadersFrame(request, null, true);

        // Prepare the listener to receive the HTTP response frames.
        Stream.Listener responseListener = new Stream.Listener.Adapter() {
            // processes HEADER frames
            @Override
            public void onHeaders(Stream stream, HeadersFrame frame) {
                System.out.println("[" + stream.getId() + "] HEADERS " + frame.getMetaData().toString());
            }

            @Override
            public void onData(Stream stream, DataFrame frame, Callback callback) {
                byte[] bytes = new byte[frame.getData().remaining()];
                frame.getData().get(bytes);
                int duration = (int) TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
                System.out.println("After " + duration + " seconds: " + new String(bytes));
                callback.succeeded();
            }
        };

        session.newStream(headersFrame, new FuturePromise<>(), responseListener);
        session.newStream(headersFrame, new FuturePromise<>(), responseListener);
        session.newStream(headersFrame, new FuturePromise<>(), responseListener);

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        client.stop();
    }

    @Test
    public void testHttp2WithHttpClientAndHttp() {
        HttpClient httpClient = null;
        String response;
        int port = 8080;
        try {
            httpClient = new HttpClient(new HttpClientTransportOverHTTP2(new HTTP2Client()),
                    new SslContextFactory(false));
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

}