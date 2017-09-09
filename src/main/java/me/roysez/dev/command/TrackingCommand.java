package me.roysez.dev.command;

import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.receive.events.Event;
import com.github.messenger4j.receive.events.TextMessageEvent;
import com.github.messenger4j.send.MessengerSendClient;
import com.github.messenger4j.send.QuickReply;
import com.github.messenger4j.send.SenderAction;
import com.vdurmont.emoji.EmojiManager;
import me.roysez.dev.domain.DocumentTracking;
import me.roysez.dev.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TrackingCommand implements Command {


    private TrackingService trackingService;

    @Autowired
    public TrackingCommand(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @Override
    public void execute(Event event, MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {

        logger.info("Tracking command executed with event - {}",event);

        TextMessageEvent textMessageEvent;

        if(event instanceof TextMessageEvent)
             textMessageEvent = (TextMessageEvent) event;
        else throw new IllegalArgumentException();

        final String documentNumber = textMessageEvent.getText();
        final String recipientId = event.getSender().getId();

        try {

            DocumentTracking documentTracking = trackingService.track(documentNumber);
            sendClient.sendSenderAction(recipientId, SenderAction.TYPING_ON);
            StringBuilder response = new StringBuilder()
                    .append(EmojiManager.getForAlias("small_airplane").getUnicode()
                            + " Відправлення з міста: " + documentTracking.getCitySender() + "\n "
                            + EmojiManager.getForAlias("airplane_arriving").getUnicode()
                            + " До: " + documentTracking.getCityRecipient() + "\n"
                            + EmojiManager.getForAlias("timer_clock").getUnicode()
                            + " Дата та час доставки: " + documentTracking.getRecipientDateTime() + "\n"
                            + EmojiManager.getForAlias("envelope").getUnicode()
                            + " Статус: "  + documentTracking.getStatus() + "\n");

            sendClient.sendTextMessage(recipientId,response.toString());

        } catch (Exception e){
            e.printStackTrace();
            logger.debug("Fail to track document -  {}",documentNumber);

            sendClient.sendSenderAction(recipientId, SenderAction.TYPING_ON);

            final List<QuickReply> quickReplies = QuickReply.newListBuilder()
                    .addTextQuickReply("Повторити спробу", "GET_STATUS_DELIVERY_FORM_PAYLOAD").toList()
                    .build();

            sendClient.sendTextMessage(recipientId,
                    "Перевірте правильність введення номера накладної та повторіть спробу" +
                            EmojiManager.getForAlias("slightly_frowning").getUnicode(), quickReplies);

            sendClient.sendSenderAction(recipientId, SenderAction.TYPING_OFF);
        }

    }
}
