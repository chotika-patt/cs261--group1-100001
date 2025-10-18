package tu_store.demo.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;

@Service
public class TuApiService {
    private static final String ACCESS_TOKEN = "TU86ef004ae9c4dc077b0f5ee076f23388452133650814c706551da3b9bdbdde09a4fb72e5142d25ee2069187888c64b4d";

    public HttpClient createHttpClientWithTUCert() throws Exception {
        // Load your truststore
        InputStream trustStoreStream = getClass().getResourceAsStream("/certs/tu-truststore.jks");
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(trustStoreStream, "changeit".toCharArray());

        // Init TrustManagerFactory with the loaded truststore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // Init SSLContext with the trust managers
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        // Build HttpClient using this SSLContext
        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();
    }


    public boolean checkStudentExists(String studentID){
        try {
            HttpClient client = createHttpClientWithTUCert();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://restapi.tu.ac.th/api/v2/profile/std/info/?id=" + studentID))
                    .header("Content-Type", "application/json")
                    .header("Application-Key", ACCESS_TOKEN)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            return root.path("status").asBoolean(false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
