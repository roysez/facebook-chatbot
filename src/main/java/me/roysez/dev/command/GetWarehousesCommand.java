package me.roysez.dev.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.receive.events.AttachmentMessageEvent;
import com.github.messenger4j.receive.events.Event;
import com.github.messenger4j.receive.events.TextMessageEvent;
import com.github.messenger4j.send.MessengerSendClient;
import com.github.messenger4j.send.SenderAction;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.vdurmont.emoji.EmojiManager;
import me.roysez.dev.domain.Document;
import me.roysez.dev.domain.DocumentTracking;
import me.roysez.dev.domain.Warehouse;
import me.roysez.dev.maps.MapUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
                    AttachmentMessageEvent.Coordinates coordinates = payload.asLocationPayload().getCoordinates();
                    sendClient.sendSenderAction(recipientId, SenderAction.TYPING_ON);
                    sendClient.sendTextMessage(recipientId,"Зачекайте будь-ласка, аналізуємо вашу локацію");
                    sendClient.sendSenderAction(recipientId, SenderAction.TYPING_ON);
                    List<Warehouse> warehouses = getWarehousesByCity(
                            getCityByCoordinates(payload.asLocationPayload().getCoordinates()));

                    Map<Warehouse,Double> mapOfWarehouses = Collections.EMPTY_MAP;
                    StringBuilder response = new StringBuilder();
                    final int[] i = {0};
                    if(!warehouses.isEmpty()) {
                        mapOfWarehouses = filterWarehouses(warehouses, coordinates);
                        mapOfWarehouses = mapOfWarehouses.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue())
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                        (e1, e2) -> e1, LinkedHashMap::new));
                        response.append("Найближчі відділення до вас"+ EmojiManager.getForAlias("truck").getUnicode() + '\n'+ '\n');
                        mapOfWarehouses.forEach((key, value) -> {
                            if(i[0]<3) {
                                response.append(EmojiManager.getForAlias("house").getUnicode() +
                                        key.getDescription() + " ( "
                                        + (int) (value * 1000) + " meters)\n\n");
                            }
                            i[0]++;
                        });

                    } else  response.append("Щось пішло не так :( Повторіть спробу пізніше \n");

                    mapOfWarehouses.forEach((warehouse, aDouble) -> System.out.println(warehouse.getDescription() + " ("+aDouble+") \n"));

                    sendClient.sendTextMessage(recipientId,response.toString().replace(":",":\n"+
                            EmojiManager.getForAlias("information_source").getUnicode()));

                } catch (Exception e) {
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

    private List<Warehouse> getWarehousesByCity(String cityName){

        HttpResponse<String> response = null;
        try {
            response = Unirest.post("https://api.novaposhta.ua/v2.0/json/")
                    .header("content-type", "application/json")
                    .header("cache-control", "no-cache")
                    .header("postman-token", "274da9e9-e3d1-9236-4dd3-b72ad07ce560")
                    .body("{\"apiKey\":\""+apiKey+"\",\"modelName\":\"AddressGeneral\"," +
                            "\"calledMethod\":\"getWarehouses\"," +
                            "\"methodProperties\":{\"CityName\":\""+cityName+"\"," +
                            "\"Language\":\"ru ИЛИ ua\"}}\r\n")
                    .asString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        List<Warehouse> warehouseList = new ArrayList<>();


        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.readValue(response.getBody(), ObjectNode.class);

             warehouseList = mapper.readValue(node.get("data").toString(),
                     mapper.getTypeFactory().constructCollectionType(List.class,Warehouse.class));

            System.out.println("Data response empty -" + warehouseList.isEmpty());
        } catch (Exception e){
            e.printStackTrace();

        }

        return warehouseList;

    }

    private Map<Warehouse,Double>  filterWarehouses(List<Warehouse> warehouses, AttachmentMessageEvent.Coordinates userCoorinates){

        Map<Warehouse,Double> map = new HashMap<>();
        warehouses.forEach(warehouse ->{
            Double distance = MapUtil.distanceInKm(userCoorinates,
                    warehouse.getLongitude(),warehouse.getLatitude() );

            if(!warehouse.getDescription().contains("Поштомат") && distance < 5d)
                map.put(warehouse,distance);

        });
        return  map;


    }




}
