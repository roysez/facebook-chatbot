package me.roysez.dev.command;

import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.receive.events.Event;
import com.github.messenger4j.send.MessengerSendClient;
import com.github.messenger4j.send.QuickReply;
import com.github.messenger4j.send.SenderAction;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class GetStartedCommand implements Command {

    @Override
    public void execute(Event event, MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {

        logger.info("Get Started command executed with event - {}",event);

        final String recipientId = event.getSender().getId();

        sendClient.sendSenderAction(recipientId, SenderAction.TYPING_ON);

        final List<QuickReply> quickReplies = QuickReply.newListBuilder()
                .addTextQuickReply("Статус доставки", "GET_STATUS_DELIVERY_FORM_PAYLOAD").toList()
                .addLocationQuickReply().toList()
                .build();


        sendClient.sendTextMessage(recipientId,
                "Швидко дізнавайтесь статус ваших відправлень Нової Пошти " +
                "— просто надішліть номер накладної після вибору пункту \'Статус доставки\' " +
                "і отримайте всю потрібну інформацію.  " +
                "Щоб знайти найближчі відділення Нової пошти," +
                " просто надішлість нам вашу локацію" , quickReplies);

        sendClient.sendSenderAction(recipientId, SenderAction.TYPING_OFF);
    }
}
