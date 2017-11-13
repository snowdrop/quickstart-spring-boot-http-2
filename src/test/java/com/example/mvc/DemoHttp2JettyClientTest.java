package com.example.mvc;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoHttp2Application.class)
@PropertySource("classpath:application.properties")
public class DemoHttp2JettyClientTest {

    @Test
    public void simple_get() {
        HttpClient httpClient = null;
        String response = null;
        int port = 8080;
        try {
            httpClient = new HttpClient( new HttpClientTransportOverHTTP2( new HTTP2Client() ),
                    new SslContextFactory( false ) );
            Assert.assertNotNull(httpClient);
            httpClient.start();

            Request request = httpClient.newRequest("http://localhost:" + port + "/");
            Assert.assertNotNull(request);

            response = request.send().getContentAsString();
            Assert.assertEquals("Hello", response);
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

    /*
    @Test
    @Ignore
    public void testHttp2Connect() throws Exception {
        long startTime = System.nanoTime();

        // Create and start HTTP2Client.
        HTTP2Client client = new HTTP2Client();
        SslContextFactory sslContextFactory = new SslContextFactory(true);
        client.addBean(sslContextFactory);
        client.start();

        // Connect to host.
        String host = "localhost";
        int port = 8443;

        FuturePromise<Session> sessionPromise = new FuturePromise<>();
        client.connect(sslContextFactory, new InetSocketAddress(host, port), new ServerSessionListener.Adapter(), sessionPromise);

        // Obtain the client Session object.
        Session session = sessionPromise.get(5, TimeUnit.SECONDS);

        // Prepare the HTTP request headers.
        HttpFields requestFields = new HttpFields();
        requestFields.put("User-Agent", client.getClass().getName() + "/" + Jetty.VERSION);
        // Prepare the HTTP request object.
        MetaData.Request request = new MetaData.Request("GET", new HttpURI("https://" + host + ":" + port + "/"), HttpVersion.HTTP_2, requestFields);
        // Create the HTTP/2 HEADERS frame representing the HTTP request.
        HeadersFrame headersFrame = new HeadersFrame(request, null, true);

        // Prepare the listener to receive the HTTP response frames.
        Stream.Listener responseListener = new Stream.Listener.Adapter() {
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

        Thread.sleep(TimeUnit.SECONDS.toMillis(20));

        client.stop();
    }

    @Test
    public void testHttp2WithSSLConnect() throws Exception {
        OkHttpClient client = getClientWithSSL();
        Request request = new Request.Builder().url("https://localhost:" + port + "/").build();
        Response response = client.newCall(request).execute();
        assertThat(response.protocol()).isEqualTo(Protocol.HTTP_2);
    }

    private static OkHttpClient getClientWithSSL() {
        SSLContext sslContext;
        TrustManager[] trustManagers;
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            File certFile = new File("src/main/resources/server.cer");
            InputStream certInputStream = new FileInputStream(certFile);
            BufferedInputStream bis = new BufferedInputStream(certInputStream);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            while (bis.available() > 0) {
                Certificate cert = certificateFactory.generateCertificate(bis);
                keyStore.setCertificateEntry("localhost", cert);
            }
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            trustManagers = trustManagerFactory.getTrustManagers();
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);
        } catch (Exception e) {
            e.printStackTrace(); //TODO replace with real exception handling tailored to your needs
            return null;
        }

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0])
                .addInterceptor(loggingInterceptor)
                .build();
        return client;
    }
    */

}