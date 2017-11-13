package com.example.mvc;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
/*import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.api.server.ServerSessionListener;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.util.ssl.SslContextFactory;*/
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource( locations = "classpath:test-application.properties" )
public class DemoHttp2JettyClientTest {

    @Value( "${local.server.port}" )
    private int port;

/*    @Test
    @Ignore
    public void simple_get()
            throws Exception {
        HttpClient httpClient = new HttpClient(new HttpClientTransportOverHTTP2(new HTTP2Client()), //
                new SslContextFactory(true));
        httpClient.start();

        String json = httpClient.newRequest("https://localhost:" + port + "/") //
                .send() //
                .getContentAsString();

        System.out.println("json:" + json);

        Assert.assertEquals("Hello", json.toString());

        httpClient.stop();
    }

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
    }*/

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

}