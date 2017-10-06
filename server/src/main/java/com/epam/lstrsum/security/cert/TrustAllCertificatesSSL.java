package com.epam.lstrsum.security.cert;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@Slf4j
public class TrustAllCertificatesSSL {

    /**
     * Note:
     * not a best way for the server security to trust all ssl certificates.
     * It will be better to create a method trustCertificatesByKeyStore() to store
     * and trust only the public security keys of the ADFS.
     * But these keys may expire in the future and need for time to time manual updating.
     * See more details https://kb.epam.com/pages/viewpage.action?pageId=366131678
     */
    public TrustAllCertificatesSSL() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            log.error("Could not apply SSL settings!", e);
        }
    }
}
