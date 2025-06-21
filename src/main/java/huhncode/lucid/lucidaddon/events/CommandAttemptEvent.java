package huhncode.lucid.lucidaddon.events;

import meteordevelopment.meteorclient.events.Cancellable;

public class CommandAttemptEvent extends Cancellable {
    private static final CommandAttemptEvent INSTANCE = new CommandAttemptEvent();

    public String message;

    public static CommandAttemptEvent get(String message) {
        INSTANCE.setCancelled(false);
        INSTANCE.message = message;
        return INSTANCE;
    }
}