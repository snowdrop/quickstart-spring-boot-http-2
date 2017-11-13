package com.example.mvc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoHttp2Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemoHttp2ApplicationTest {

    @LocalServerPort
    private int port;

    @Test
    @Ignore
    public void testHttp2Connect() throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://localhost:" + port + "/").build();
        Response response = client.newCall(request).execute();
        assertThat(response.protocol()).isEqualTo(Protocol.HTTP_2);
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
                keyStore.setCertificateEntry("www.example.com", cert);
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

        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0])
                .build();
        return client;
    }

}