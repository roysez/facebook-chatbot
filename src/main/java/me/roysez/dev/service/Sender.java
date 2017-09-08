package me.roysez.dev.service;


import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.send.*;
import com.github.messenger4j.send.buttons.Button;
import com.github.messenger4j.send.templates.ButtonTemplate;
import com.github.messenger4j.send.templates.GenericTemplate;
import com.github.messenger4j.send.templates.ReceiptTemplate;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import me.roysez.dev.MessengerPlatformCallbackHandler;
import me.roysez.dev.domain.DocumentTracking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Sender {


    private final TrackingService trackingService;

    @Autowired
    public Sender(TrackingService trackingService){
        this.trackingService = trackingService;
    }


    private static final String RESOURCE_URL =
            "https://raw.githubusercontent.com/fbsamples/messenger-platform-samples/master/node/public";

    private static final Logger logger = LoggerFactory.getLogger(MessengerPlatformCallbackHandler.class);


    public void handleGetStarted(String recipientId,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException{

        sendClient.sendSenderAction(recipientId, SenderAction.TYPING_ON);
        final List<QuickReply> quickReplies = QuickReply.newListBuilder()
                .addTextQuickReply("Статус доставки", "GET_STATUS_DELIVERY_FORM_PAYLOAD").toList()
                .addLocationQuickReply().toList()
                .build();


        sendClient.sendTextMessage(recipientId, "Швидко дізнавайтесь статус ваших відправлень Нової Пошти " +
                "— просто надішліть номер накладної після вибору пункту \'Статус доставки\' " +
                "і отримайте всю потрібну інформацію.  " +
                "Щоб знайти найближчі відділення Нової пошти," +
                " просто надішлість нам вашу локацію" , quickReplies);

        sendClient.sendSenderAction(recipientId, SenderAction.TYPING_OFF);
    }

    public void trackingDelivery(String recipientId,MessengerSendClient sendClient,String documentNumber) throws MessengerApiException, MessengerIOException{
        try {
            DocumentTracking documentTracking = trackingService.track(documentNumber);
            sendClient.sendSenderAction(recipientId, SenderAction.TYPING_ON);
            StringBuilder response = new StringBuilder()
                    .append(EmojiManager.getForAlias("small_airplane").getUnicode()
                            + " Відправлення з міста: " + documentTracking.getCitySender() + "\n"
                            + EmojiManager.getForAlias("airplane_arriving").getUnicode()
                            + " До: " + documentTracking.getCityRecipient() + "\n"
                            + EmojiManager.getForAlias("timer_clock").getUnicode()
                            + " Дата та час доставки: " + documentTracking.getRecipientDateTime() + "\n"
                            + EmojiManager.getForAlias("envelope").getUnicode()
                            + " Статус: "  + documentTracking.getStatus() + "\n");

            sendClient.sendTextMessage(recipientId,response.toString());

        } catch (Exception e){
            e.printStackTrace();

            logger.debug("Fail to track document - {}",documentNumber);

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

    public void sendImageMessage(String recipientId,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        sendClient.sendImageAttachment(recipientId, RESOURCE_URL + "/assets/rift.png");
    }

    public void sendGifMessage(String recipientId,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        sendClient.sendImageAttachment(recipientId, "https://media.giphy.com/media/11sBLVxNs7v6WA/giphy.gif");
    }

    public void sendAudioMessage(String recipientId,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        sendClient.sendAudioAttachment(recipientId, RESOURCE_URL + "/assets/sample.mp3");
    }

    public void sendVideoMessage(String recipientId,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        sendClient.sendVideoAttachment(recipientId, RESOURCE_URL + "/assets/allofus480.mov");
    }

    public void sendFileMessage(String recipientId,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        sendClient.sendFileAttachment(recipientId, RESOURCE_URL + "/assets/test.txt");
    }

    public void sendButtonMessage(String recipientId,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        final List<Button> buttons = Button.newListBuilder()
                .addUrlButton("Open Web URL", "https://www.oculus.com/en-us/rift/").toList()
                .addPostbackButton("Trigger Postback", "DEVELOPER_DEFINED_PAYLOAD").toList()
                .addCallButton("Call Phone Number", "+16505551234").toList()
                .build();

        final ButtonTemplate buttonTemplate = ButtonTemplate.newBuilder("Tap a button", buttons).build();
        sendClient.sendTemplate(recipientId, buttonTemplate);
    }

    public void sendGenericMessage(String recipientId,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        final List<Button> riftButtons = Button.newListBuilder()
                .addUrlButton("Open Web URL", "https://www.oculus.com/en-us/rift/").toList()
                .addPostbackButton("Call Postback", "Payload for first bubble").toList()
                .build();

        final List<Button> touchButtons = Button.newListBuilder()
                .addUrlButton("Open Web URL", "https://www.oculus.com/en-us/touch/").toList()
                .addPostbackButton("Call Postback", "Payload for second bubble").toList()
                .build();


        final GenericTemplate genericTemplate = GenericTemplate.newBuilder()
                .addElements()
                .addElement("rift")
                .subtitle("Next-generation virtual reality")
                .itemUrl("https://www.oculus.com/en-us/rift/")
                .imageUrl(RESOURCE_URL + "/assets/rift.png")
                .buttons(riftButtons)
                .toList()
                .addElement("touch")
                .subtitle("Your Hands, Now in VR")
                .itemUrl("https://www.oculus.com/en-us/touch/")
                .imageUrl(RESOURCE_URL + "/assets/touch.png")
                .buttons(touchButtons)
                .toList()
                .done()
                .build();

        sendClient.sendTemplate(recipientId, genericTemplate);
    }

    public void sendReceiptMessage(String recipientId,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        final String uniqueReceiptId = "order-" + Math.floor(Math.random() * 1000);

        final ReceiptTemplate receiptTemplate = ReceiptTemplate.newBuilder("Peter Chang", uniqueReceiptId, "USD", "Visa 1234")
                .timestamp(1428444852L)
                .addElements()
                .addElement("Oculus Rift", 599.00f)
                .subtitle("Includes: headset, sensor, remote")
                .quantity(1)
                .currency("USD")
                .imageUrl(RESOURCE_URL + "/assets/riftsq.png")
                .toList()
                .addElement("Samsung Gear VR", 99.99f)
                .subtitle("Frost White")
                .quantity(1)
                .currency("USD")
                .imageUrl(RESOURCE_URL + "/assets/gearvrsq.png")
                .toList()
                .done()
                .addAddress("1 Hacker Way", "Menlo Park", "94025", "CA", "US").done()
                .addSummary(626.66f)
                .subtotal(698.99f)
                .shippingCost(20.00f)
                .totalTax(57.67f)
                .done()
                .addAdjustments()
                .addAdjustment().name("New Customer Discount").amount(-50f).toList()
                .addAdjustment().name("$100 Off Coupon").amount(-100f).toList()
                .done()
                .build();

        sendClient.sendTemplate(recipientId, receiptTemplate);
    }

    public void sendQuickReply(String recipientId,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        final List<QuickReply> quickReplies = QuickReply.newListBuilder()
                .addTextQuickReply("Action", "DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_ACTION").toList()
                .addTextQuickReply("Comedy", "DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_COMEDY").toList()
                .addTextQuickReply("Drama", "DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_DRAMA").toList()
                .addLocationQuickReply().toList()
                .build();

        sendClient.sendTextMessage(recipientId, "What's your favorite movie genre?", quickReplies);
    }

    public void sendReadReceipt(String recipientId,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        sendClient.sendSenderAction(recipientId, SenderAction.MARK_SEEN);
    }

    public void sendTypingOn(String recipientId,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        sendClient.sendSenderAction(recipientId, SenderAction.TYPING_ON);
    }

    public void sendTypingOff(String recipientId,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        sendClient.sendSenderAction(recipientId, SenderAction.TYPING_OFF);
    }

    public void sendAccountLinking(String recipientId,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        // Simulation of account linking
    }

    // Default
    public void sendTextMessage(String recipientId, String text,MessengerSendClient sendClient) {
        try {
            final Recipient recipient = Recipient.newBuilder().recipientId(recipientId).build();
            final NotificationType notificationType = NotificationType.REGULAR;
            final String metadata = "DEVELOPER_DEFINED_METADATA";

                sendClient.sendTextMessage(recipient, notificationType, text, metadata);
        } catch (MessengerApiException | MessengerIOException e) {
            handleSendException(e);
        }
    }

    public void handleSendException(Exception e) {
        logger.error("Message could not be sent. An unexpected error occurred.", e);
    }
}
