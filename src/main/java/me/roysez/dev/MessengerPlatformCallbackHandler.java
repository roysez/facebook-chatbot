package me.roysez.dev;

import static com.github.messenger4j.MessengerPlatform.CHALLENGE_REQUEST_PARAM_NAME;
import static com.github.messenger4j.MessengerPlatform.MODE_REQUEST_PARAM_NAME;
import static com.github.messenger4j.MessengerPlatform.SIGNATURE_HEADER_NAME;
import static com.github.messenger4j.MessengerPlatform.VERIFY_TOKEN_REQUEST_PARAM_NAME;

import com.github.messenger4j.MessengerPlatform;
import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.exceptions.MessengerVerificationException;
import com.github.messenger4j.receive.MessengerReceiveClient;
import com.github.messenger4j.receive.events.AccountLinkingEvent.AccountLinkingStatus;
import com.github.messenger4j.receive.events.AttachmentMessageEvent.Attachment;
import com.github.messenger4j.receive.events.AttachmentMessageEvent.AttachmentType;
import com.github.messenger4j.receive.events.AttachmentMessageEvent.Payload;
import com.github.messenger4j.receive.handlers.AccountLinkingEventHandler;
import com.github.messenger4j.receive.handlers.AttachmentMessageEventHandler;
import com.github.messenger4j.receive.handlers.EchoMessageEventHandler;
import com.github.messenger4j.receive.handlers.FallbackEventHandler;
import com.github.messenger4j.receive.handlers.MessageDeliveredEventHandler;
import com.github.messenger4j.receive.handlers.MessageReadEventHandler;
import com.github.messenger4j.receive.handlers.OptInEventHandler;
import com.github.messenger4j.receive.handlers.PostbackEventHandler;
import com.github.messenger4j.receive.handlers.QuickReplyMessageEventHandler;
import com.github.messenger4j.receive.handlers.TextMessageEventHandler;
import com.github.messenger4j.send.MessengerSendClient;
import com.github.messenger4j.send.buttons.Button;
import com.github.messenger4j.send.templates.ButtonTemplate;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import me.roysez.dev.service.Sender;
import me.roysez.dev.service.TrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is the main class for inbound and outbound communication with the Facebook Messenger Platform.
 * The callback handler is responsible for the webhook verification and processing of the inbound messages and events.
 * It showcases the features of the Messenger Platform.
  */
@RestController
@RequestMapping("/callback")
public class MessengerPlatformCallbackHandler {

    private static final String RESOURCE_URL =
            "https://raw.githubusercontent.com/fbsamples/messenger-platform-samples/master/node/public";

    private static final Logger logger = LoggerFactory.getLogger(MessengerPlatformCallbackHandler.class);

    private final MessengerReceiveClient receiveClient;
    private final MessengerSendClient sendClient;
    private final Sender sender;
    private final TrackingService trackingService;
    /**
     * Constructs the {@code MessengerPlatformCallbackHandler} and initializes the {@code MessengerReceiveClient}.
     *
     * @param appSecret   the {@code Application Secret}
     * @param verifyToken the {@code Verification Token} that has been provided by you during the setup of the {@code
     *                    Webhook}
     * @param sendClient  the initialized {@code MessengerSendClient}
     */
    @Autowired
    public MessengerPlatformCallbackHandler(@Value("${messenger4j.appSecret}") final String appSecret,
                                            @Value("${messenger4j.verifyToken}") final String verifyToken,
                                            final MessengerSendClient sendClient,Sender sender,final TrackingService trackingService) {

        logger.debug("Initializing MessengerReceiveClient - appSecret: {} | verifyToken: {}", appSecret, verifyToken);
        this.receiveClient = MessengerPlatform.newReceiveClientBuilder(appSecret, verifyToken)
                .onTextMessageEvent(newTextMessageEventHandler())
                .onAttachmentMessageEvent(newAttachmentMessageEventHandler())
                .onQuickReplyMessageEvent(newQuickReplyMessageEventHandler())
                .onPostbackEvent(newPostbackEventHandler())
                .onAccountLinkingEvent(newAccountLinkingEventHandler())
                .onOptInEvent(newOptInEventHandler())
                .onEchoMessageEvent(newEchoMessageEventHandler())
                .onMessageDeliveredEvent(newMessageDeliveredEventHandler())
                .onMessageReadEvent(newMessageReadEventHandler())
                .fallbackEventHandler(newFallbackEventHandler())
                .build();
        this.sendClient = sendClient;
        this.sender = sender;
        this.trackingService = trackingService;
    }

    /**
     * Webhook verification endpoint.
     *
     * The passed verification token (as query parameter) must match the configured verification token.
     * In case this is true, the passed challenge string must be returned by this endpoint.
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> verifyWebhook(@RequestParam(MODE_REQUEST_PARAM_NAME) final String mode,
                                                @RequestParam(VERIFY_TOKEN_REQUEST_PARAM_NAME) final String verifyToken,
                                                @RequestParam(CHALLENGE_REQUEST_PARAM_NAME) final String challenge) {

        logger.debug("Received Webhook verification request - mode: {} | verifyToken: {} | challenge: {}", mode,
                verifyToken, challenge);
        try {
            return ResponseEntity.ok(this.receiveClient.verifyWebhook(mode, verifyToken, challenge));
        } catch (MessengerVerificationException e) {
            logger.warn("Webhook verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * Callback endpoint responsible for processing the inbound messages and events.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> handleCallback(@RequestBody final String payload,
                                               @RequestHeader(SIGNATURE_HEADER_NAME) final String signature) {

        logger.debug("Received Messenger Platform callback - payload: {} | signature: {}", payload, signature);
        try {
            this.receiveClient.processCallbackPayload(payload, signature);
            logger.debug("Processed callback payload successfully");
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (MessengerVerificationException e) {
            logger.warn("Processing of callback payload failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private TextMessageEventHandler newTextMessageEventHandler() {
        return event -> {
            logger.debug("Received TextMessageEvent: {}", event);

            final String messageId = event.getMid();
            final String messageText = event.getText();
            final String senderId = event.getSender().getId();
            final Date timestamp = event.getTimestamp();

            logger.info("Received message '{}' with text '{}' from user '{}' at '{}'",
                    messageId, messageText, senderId, timestamp);

            try {
                switch (messageText.toLowerCase()) {
                    case "image":
                        sender.sendImageMessage(senderId,this.sendClient);
                        break;

                    case "gif":
                        sender.sendGifMessage(senderId,this.sendClient);
                        break;

                    case "audio":
                        sender.sendAudioMessage(senderId,this.sendClient);
                        break;

                    case "video":
                        sender.sendVideoMessage(senderId,this.sendClient);
                        break;

                    case "file":
                        sender.sendFileMessage(senderId,this.sendClient);
                        break;

                    case "button":
                        sender.sendButtonMessage(senderId,this.sendClient);
                        break;

                    case "generic":
                        sender.sendGenericMessage(senderId,this.sendClient);
                        break;

                    case "receipt":
                        sender.sendReceiptMessage(senderId,this.sendClient);
                        break;

                    case "quick reply":
                        sender.sendQuickReply(senderId,this.sendClient);
                        break;

                    case "read receipt":
                        sender.sendReadReceipt(senderId,this.sendClient);
                        break;

                    case "typing on":
                        sender.sendTypingOn(senderId,this.sendClient);
                        break;

                    case "typing off":
                        sender.sendTypingOff(senderId,this.sendClient);
                        break;


                    case "account linking":
                        sender.sendAccountLinking(senderId,this.sendClient);
                        break;
                    case "testmetadata":
                        sender.sendTextMessage(senderId,"Testing",this.sendClient,"metadata");
                        break;
                    default:
                        if(messageText.toLowerCase().matches("^[0-9]{10,14}$")){
                            trackingService.track(messageText);
                        }
                        sender.sendTextMessage(senderId, messageText,this.sendClient,"");
                }
            } catch (MessengerApiException | MessengerIOException e) {
                sender.handleSendException(e);
            }
        };
    }



    private AttachmentMessageEventHandler newAttachmentMessageEventHandler() {
        return event -> {
            logger.debug("Received AttachmentMessageEvent: {}", event);

            final String messageId = event.getMid();
            final List<Attachment> attachments = event.getAttachments();
            final String senderId = event.getSender().getId();
            final Date timestamp = event.getTimestamp();

            logger.info("Received message '{}' with attachments from user '{}' at '{}':",
                    messageId, senderId, timestamp);

            attachments.forEach(attachment -> {
                final AttachmentType attachmentType = attachment.getType();
                final Payload payload = attachment.getPayload();

                String payloadAsString = null;
                if (payload.isBinaryPayload()) {
                    payloadAsString = payload.asBinaryPayload().getUrl();
                }
                if (payload.isLocationPayload()) {
                    payloadAsString = payload.asLocationPayload().getCoordinates().toString();
                }

                logger.info("Attachment of type '{}' with payload '{}'", attachmentType, payloadAsString);
            });

            sender.sendTextMessage(senderId, "Message with attachment received",this.sendClient,"");
        };
    }

    private QuickReplyMessageEventHandler newQuickReplyMessageEventHandler() {
        return event -> {
            logger.debug("Received QuickReplyMessageEvent: {}", event);


            final String senderId = event.getSender().getId();
            final String messageId = event.getMid();
            final String quickReplyPayload = event.getQuickReply().getPayload();

            logger.info("Received quick reply for message '{}' with payload '{}'", messageId, quickReplyPayload);

            if(quickReplyPayload.equals("GET_STATUS_DELIVERY_FORM_PAYLOAD")){
                
                sender.sendTextMessage(senderId,"Введіть номер накладної",this.sendClient,"");
            } else
            sender.sendTextMessage(senderId, "Quick reply tapped",this.sendClient,"");
        };
    }

    private PostbackEventHandler newPostbackEventHandler() {
        return event -> {
            logger.debug("Received PostbackEvent: {}", event);

            final String senderId = event.getSender().getId();
            final String recipientId = event.getRecipient().getId();
            final String payload = event.getPayload();
            final Date timestamp = event.getTimestamp();


            logger.info("Received postback for user '{}' and page '{}' with payload '{}' at '{}'",
                    senderId, recipientId, payload, timestamp);

            sender.sendTextMessage(senderId, "Postback called",this.sendClient,"");
        };
    }

    private AccountLinkingEventHandler newAccountLinkingEventHandler() {
        return event -> {
            logger.debug("Received AccountLinkingEvent: {}", event);

            final String senderId = event.getSender().getId();
            final AccountLinkingStatus accountLinkingStatus = event.getStatus();
            final String authorizationCode = event.getAuthorizationCode();

            logger.info("Received account linking event for user '{}' with status '{}' and auth code '{}'",
                    senderId, accountLinkingStatus, authorizationCode);
        };
    }

    private OptInEventHandler newOptInEventHandler() {
        return event -> {
            logger.debug("Received OptInEvent: {}", event);

            final String senderId = event.getSender().getId();
            final String recipientId = event.getRecipient().getId();
            final String passThroughParam = event.getRef();
            final Date timestamp = event.getTimestamp();

            logger.info("Received authentication for user '{}' and page '{}' with pass through param '{}' at '{}'",
                    senderId, recipientId, passThroughParam, timestamp);

            sender.sendTextMessage(senderId, "Authentication successful",this.sendClient,"");
        };
    }

    private EchoMessageEventHandler newEchoMessageEventHandler() {
        return event -> {
            logger.debug("Received EchoMessageEvent: {}", event);

            final String messageId = event.getMid();
            final String recipientId = event.getRecipient().getId();
            final String senderId = event.getSender().getId();
            final Date timestamp = event.getTimestamp();
            if(event.getMetadata()!=null && event.getMetadata().equals("DEVELOPER_DEFINED_METADATA")){
                sender.sendTextMessage(recipientId,"tested!",sendClient,"");
            }
            logger.info("Received echo for message '{}' that has been sent to recipient '{}' by service '{}' at '{}'",
                    messageId, recipientId, senderId, timestamp);
        };
    }

    private MessageDeliveredEventHandler newMessageDeliveredEventHandler() {
        return event -> {
            logger.debug("Received MessageDeliveredEvent: {}", event);

            final List<String> messageIds = event.getMids();
            final Date watermark = event.getWatermark();
            final String senderId = event.getSender().getId();

            if (messageIds != null) {
                messageIds.forEach(messageId -> {
                    logger.info("Received delivery confirmation for message '{}'", messageId);
                });
            }

            logger.info("All messages before '{}' were delivered to user '{}'", watermark, senderId);
        };
    }

    private MessageReadEventHandler newMessageReadEventHandler() {
        return event -> {
            logger.debug("Received MessageReadEvent: {}", event);

            final Date watermark = event.getWatermark();
            final String senderId = event.getSender().getId();

            logger.info("All messages before '{}' were read by user '{}'", watermark, senderId);
        };
    }

    /**
     * This handler is called when either the message is unsupported or when the event handler for the actual event type
     * is not registered. In this showcase all event handlers are registered. Hence only in case of an
     * unsupported message the fallback event handler is called.
     */
    private FallbackEventHandler newFallbackEventHandler() {
        return event -> {
            logger.debug("Received FallbackEvent: {}", event);

            final String senderId = event.getSender().getId();
            logger.info("Received unsupported message from user '{}'", senderId);
        };
    }




}