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
        // Configure a parsed parameters and a http server url
        String httpVersion = HttpArgs.parse(HttpArgs.HTTP_VERSION, args);
        String httpAddr = HttpArgs.parse(HttpArgs.HTTP_ADDR, args);
        String httpPort = HttpArgs.parse(HttpArgs.HTTP_PORT, args);
        String httpCert = HttpArgs.parse(HttpArgs.HTTP_CERT, args);
        URI url = new URI("https://" + httpAddr + ":" + httpPort);


        // Create a pool with the server certificate since it is not signed by a known CA
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[] { new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // 클라이언트에 관한 부분으로 건드리지 않아도 됨
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // Create TLS configuration with the certificate of the server
                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    Certificate ca = cf.generateCertificate(new FileInputStream("src/" + httpCert));

                    // Trust Store
                    KeyStore trustStore = KeyStore.getInstance("PKCS12");
                    trustStore.load(null, null);
                    trustStore.setCertificateEntry("ca", ca);

                    // Trust Manager
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


        // Use the proper transport in the client
        HttpClient.Version version;
        if (httpVersion.equals("1")) {
            String message = String.format("Connect to %s over TLS using HTTP/1.1", url.toString());
            System.out.println(message);
            version = HttpClient.Version.HTTP_1_1;
        } else if (httpVersion.equals("2")){
            String message = String.format("Connect to %s over TLS using HTTP/2", url.toString());
            System.out.println(message);
            version = HttpClient.Version.HTTP_2;
        } else {
            String message = "잘못된 HTTP 버전입니다. 1 또는 2를 입력해주세요.";
            System.out.println(message);
            throw new IllegalArgumentException("잘못된 HTTP 버전입니다.");
        }


        // Create a http client
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .sslContext(context)
                .version(version)
                .build();


        // Create a http request
       HttpRequest request;
        try {
            request = HttpRequest
                    .newBuilder()
                    .uri(url)
                    .GET()
                    .setHeader("http-version", version.toString())
//                    .setHeader("port", )
                    .build();

        } catch (Exception exception) {
            System.out.println("Failed request build: " + url);
            System.out.println(exception.getMessage());
            throw new IllegalArgumentException("request 설정에 실패했습니다.");
        }

        // Perform the request
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception exception) {
            System.out.println("Failed get: " + url);
            System.out.println(exception.getMessage());
            throw new IllegalArgumentException("서버에 get 요청을 보내는데 실패했습니다.");
        }


        int responseStatusCode = response.statusCode();
        String responseProto = response.version().toString();
        String responseBody = response.body();
        String responseMessage = String.format("Got response %d: %s %s", responseStatusCode, responseProto, responseBody);
        System.out.println(responseMessage);
    }

    // 입력을 위한 enum 클래스
    public enum HttpArgs {
        HTTP_VERSION("version", "2", "HTTP version"),
        HTTP_ADDR("addr", "localhost", "HTTP server address"),
        HTTP_PORT("port", "8000", "HTTP server port"),
        HTTP_CERT("cert", "server.crt", "HTTP server certificate file name");

        private String name;
        private String defaultArgument;
        private String explaination;

        HttpArgs(String name, String defaultArgument, String explaination) {
            this.name = name;
            this.defaultArgument = defaultArgument;
            this.explaination = explaination;
        }

        public static String parse(HttpArgs httpArgs, String[] args) {
            try {
                String option = "-" + httpArgs.name;
                for (int i = 0; i < args.length; i++) {
                    if (option.equals(args[i])) {
                        return args[i + 1];
                    }
                }
            } catch (IndexOutOfBoundsException exception) {
                System.out.println(httpArgs.name + " 값을 잘못 입력하셨습니다.");
            }
            return httpArgs.defaultArgument;
        }
    }
}