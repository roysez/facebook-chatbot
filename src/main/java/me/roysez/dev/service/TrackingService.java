package me.roysez.dev.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.roysez.dev.domain.Document;
import me.roysez.dev.domain.DocumentTracking;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;

@Service
public class TrackingService {


    private final String apiKey;

    private RestTemplate restTemplate;

    @Autowired
    public TrackingService(@Value("${novaposhta.apiKey}") final String apiKey) {
        this.apiKey = apiKey;
    }

    public String track(String documentNumber) throws IOException {

        restTemplate = new RestTemplate();

        JSONObject request = new JSONObject();

        ArrayList<Document> documents = new ArrayList<>();
        documents.add(new Document(documentNumber,""));

        request.put("apiKey", apiKey);
        request.put("modelName", "TrackingDocument");
        request.put("calledMethod", "getStatusDocuments");
        request.put("methodProperties",new JSONObject().put("Documents",documents));


        String requestString = request.toString()
                .replace("phone","Phone").replace("documentNumber","DocumentNumber");
        System.out.println(requestString);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<String>(requestString, headers);


        ResponseEntity<String> loginResponse = restTemplate
                .exchange("https://api.novaposhta.ua/v2.0/json/", HttpMethod.POST, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.readValue(loginResponse.getBody(), ObjectNode.class);

        DocumentTracking documentTracking = mapper.readValue(node.get("data").get(0).toString(),DocumentTracking.class);
        return documentTracking.getStatus();
    }
}
