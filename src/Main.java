import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class Main {
    public static void main(String[] args) throws Exception {
        // Parse command line parameters
        // HttpVersion, addr, port, cert?
        String httpVersion = "";
        String httpAddr = "";
        String httpPort = "";
        String httpCert = "";

        URI url = new URI("https://" + httpAddr + ":" + 443);

        SSLContext context = SSLContext.getInstance("SSL");
        context.init(null, new TrustManager[] { new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // client certification check
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // Server certification check
                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    Certificate ca = cf.generateCertificate(new FileInputStream("src/localhost.crt"));

                    // Get trust store
                    KeyStore trustStore = KeyStore.getInstance("PKCS12");
                    trustStore.load(null, null);
                    trustStore.setCertificateEntry("ca", ca);

                    // Get Trust Manager
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init(trustStore);
                    TrustManager[] tms = tmf.getTrustManagers();
                    ((X509TrustManager)tms[0]).checkServerTrusted(chain, authType);

                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        } }, null);

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .sslContext(context)
                .version(HttpClient.Version.HTTP_2)
                .build();

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(new URI("https://localhost:443"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.version());
        System.out.println("response = " + response.headers());
        System.out.println("response = " + response.body());


        HttpClient client2 = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .sslContext(context)
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpRequest request2 = HttpRequest
                .newBuilder()
                .uri(new URI("http://localhost:8080"))
                .GET()
                .build();

        HttpResponse<String> response2 = client2.send(request2, HttpResponse.BodyHandlers.ofString());
        System.out.println(response2.version());
        System.out.println("response = " + response2.headers());
        System.out.println("response = " + response2.body());
    }

    public enum HttpArgs {
        HTTP_VERSION("version", "2", "HTTP version"),
        HTTP_ADDR("addr", "localhost", "HTTP server address"),
        HTTP_PORT("port", "8080", "HTTP server port"),
        HTTP_CERT("cert", "server.crt", "HTTP server certificate file name");

        private String name;
        private String argument;
        private String explaination;

        HttpArgs(String name, String argument, String explaination) {
            this.name = name;
            this.argument = argument;
            this.explaination = explaination;
        }


    }
}