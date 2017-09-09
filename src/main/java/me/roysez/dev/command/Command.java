package me.roysez.dev.command;

import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.receive.events.Event;
import com.github.messenger4j.send.MessengerSendClient;
import me.roysez.dev.MessengerPlatformCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Command {

    static final Logger logger = LoggerFactory.getLogger(MessengerPlatformCallbackHandler.class);

    void execute(Event event, MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException;

}
