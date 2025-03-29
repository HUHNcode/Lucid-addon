package huhncode.lucid.lucidaddon.events;

import meteordevelopment.meteorclient.events.Cancellable;

public class SendCommandEvent extends Cancellable {
    public String message;

    public SendCommandEvent(String message) {
        this.message = message;
    }

    public static SendCommandEvent get(String message) {
        System.out.println("Command intercepted: " + message);
        return new SendCommandEvent(message);
    }
}
