package tu_store.demo.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class TuApiService {
    private static final String ACCESS_TOKEN = "TU86ef004ae9c4dc077b0f5ee076f23388452133650814c706551da3b9bdbdde09a4fb72e5142d25ee2069187888c64b4d";

    public boolean checkStudentExists(String studentID){
        try {
            HttpClient client = HttpClient.newHttpClient();
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
