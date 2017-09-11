package me.roysez.dev.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.receive.events.AttachmentMessageEvent;
import com.github.messenger4j.receive.events.Event;
import com.github.messenger4j.receive.events.TextMessageEvent;
import com.github.messenger4j.send.MessengerSendClient;
import me.roysez.dev.domain.Document;
import me.roysez.dev.domain.DocumentTracking;
import me.roysez.dev.domain.Warehouse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class GetWarehousesCommand implements Command {

    private final String urlCityCollection = "http://nominatim.openstreetmap.org/reverse";

    private final String apiKey;

    @Autowired
    public GetWarehousesCommand(@Value("${novaposhta.apiKey}") final String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void execute(Event event, MessengerSendClient sendClient)
            throws MessengerApiException, MessengerIOException {

        logger.info("Get warehouses command executed with event - {}",event);

        AttachmentMessageEvent messageEvent;

        if(event instanceof AttachmentMessageEvent)
            messageEvent = (AttachmentMessageEvent) event;
        else throw new IllegalArgumentException();

        final String recipientId = messageEvent.getSender().getId();
        final List<AttachmentMessageEvent.Attachment> attachments = messageEvent.getAttachments();

        attachments.forEach(attachment -> {

            final AttachmentMessageEvent.AttachmentType attachmentType = attachment.getType();
            final AttachmentMessageEvent.Payload payload = attachment.getPayload();

            String payloadAsString = null;

            if (payload.isLocationPayload()) {
                payloadAsString = payload.asLocationPayload().getCoordinates().toString();
                try {
                    sendClient.sendTextMessage(recipientId,
                            getNearWarehouses(
                            getCityByCoordinates(payload.asLocationPayload().getCoordinates()))
                    );
                } catch (MessengerApiException | MessengerIOException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getCityByCoordinates (AttachmentMessageEvent.Coordinates coordinates) throws IOException {
        RestTemplate restTemplate = new RestTemplate();


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlCityCollection)
                .queryParam("format","json")
                .queryParam("lat", coordinates.getLatitude())
                .queryParam("lon", coordinates.getLongitude());


        HttpEntity<?> entity = new HttpEntity<>(headers);

        HttpEntity<String> response = restTemplate.exchange(
                builder.build().encode().toUri(),
                HttpMethod.GET,
                entity,
                String.class);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.readValue(response.getBody(), ObjectNode.class);
        String responseBody = response.getBody(), city = "Невідомі координати.Повторіть спробу";
        logger.info(response.getBody());
        try {
            if(responseBody.contains("city"))
                city =  mapper.readValue(node.get("address").get("city").toString(), String.class);
            else if(responseBody.contains("town"))
                city =   mapper.readValue(node.get("address").get("town").toString(), String.class);
            else if(responseBody.contains("village"))
                city =  mapper.readValue(node.get("address").get("village").toString(), String.class);


        } catch (NullPointerException e) {
            city = "Невідомі координати.Повторіть спробу";
        }
        return city;

    }


    public String getNearWarehouses(String cityName){
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject request = new JSONObject();

        Warehouse.WarehouseTracking warehouseTracking =
                            new Warehouse.WarehouseTracking(cityName,"ru ИЛИ ua");

        request.put("apiKey", apiKey);
        request.put("modelName", "AddressGeneral");
        request.put("calledMethod", "getWarehouses");
        request.put("methodProperties",warehouseTracking);

        HttpEntity<String> entity = new HttpEntity<String>(request.toString(), httpHeaders);

        // send request and parse result
        ResponseEntity<String> response = restTemplate
                .exchange("https://api.novaposhta.ua/v2.0/json/", HttpMethod.POST, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();

        System.out.println(response.getBody());
        return "Test";


    }
}
