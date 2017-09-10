package me.roysez.dev.command;

import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.receive.events.AttachmentMessageEvent;
import com.github.messenger4j.receive.events.Event;
import com.github.messenger4j.receive.events.TextMessageEvent;
import com.github.messenger4j.send.MessengerSendClient;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class GetWarehousesCommand implements Command {
    @Override
    public void execute(Event event, MessengerSendClient sendClient)
            throws MessengerApiException, MessengerIOException {

        logger.info("Get warehouses command executed with event - {}",event);

        AttachmentMessageEvent attachmentMessageEvent;

        if(event instanceof AttachmentMessageEvent)
            attachmentMessageEvent = (AttachmentMessageEvent) event;
        else throw new IllegalArgumentException();

        final String recipientId = attachmentMessageEvent.getSender().getId();
        final List<AttachmentMessageEvent.Attachment> attachments = attachmentMessageEvent.getAttachments();
        final String senderId = attachmentMessageEvent.getSender().getId();
        final Date timestamp = attachmentMessageEvent.getTimestamp();

        attachments.forEach(attachment -> {
            final AttachmentMessageEvent.AttachmentType attachmentType = attachment.getType();
            final AttachmentMessageEvent.Payload payload = attachment.getPayload();
            String payloadAsString = null;
            if (payload.isLocationPayload()) {
                payloadAsString = payload.asLocationPayload().getCoordinates().toString();
                try {
                    sendClient.sendTextMessage(recipientId, payloadAsString);
                } catch (MessengerApiException e) {
                    e.printStackTrace();
                } catch (MessengerIOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
