package me.roysez.dev.command;

import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.receive.events.Event;
import com.github.messenger4j.send.MessengerSendClient;
import me.roysez.dev.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CommandExecutor {

    private static Map<Operation, Command> map = new HashMap<>();

    @Autowired
    private  static GetStartedCommand getStartedCommand;

    @Autowired
    private static  TrackingCommand trackingCommand;

    static {
        map.put(Operation.GET_STARTED, getStartedCommand);
        map.put(Operation.DOCUMENT_TRACKING, trackingCommand);

    }

    public static void execute(Operation operation, Event event,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        map.get(operation).execute(event,sendClient);
    }

}
