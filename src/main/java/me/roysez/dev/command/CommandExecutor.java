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

    private  Map<Operation, Command> map = new HashMap<>();

    private GetStartedCommand getStartedCommand;

    private TrackingCommand trackingCommand;

    @Autowired
    public CommandExecutor(GetStartedCommand getStartedCommand, TrackingCommand trackingCommand) {
        this.getStartedCommand = getStartedCommand;
        this.trackingCommand = trackingCommand;

        map.put(Operation.GET_STARTED, this.getStartedCommand);
        map.put(Operation.DOCUMENT_TRACKING, this.trackingCommand);
    }


    public  void execute(Operation operation, Event event,MessengerSendClient sendClient) throws MessengerApiException, MessengerIOException {
        this.map.get(operation).execute(event,sendClient);
    }

}
