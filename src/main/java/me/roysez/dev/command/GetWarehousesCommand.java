package me.roysez.dev.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.receive.events.AttachmentMessageEvent;
import com.github.messenger4j.receive.events.Event;
import com.github.messenger4j.receive.events.TextMessageEvent;
import com.github.messenger4j.send.MessengerSendClient;
import me.roysez.dev.domain.DocumentTracking;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Component
public class GetWarehousesCommand implements Command {

    final String urlCityCollection = "http://nominatim.openstreetmap.org/reverse";

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
                            getCityByCoordinates(payload.asLocationPayload().getCoordinates()));
                } catch (MessengerApiException | MessengerIOException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getCityByCoordinates (AttachmentMessageEvent.Coordinates coordinates) throws IOException {
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
        logger.info(response.getBody());
        return mapper.readValue(node.get("address").get("city").toString(), String.class);

    }

}
