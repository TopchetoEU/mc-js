package me.topchetoeu.mcscript.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ChatMessageCallback {
    public static class ChatArgs {
        public String message;
        public boolean cancelled;
    }

    void execute(ChatArgs args);

    public static final Event<ChatMessageCallback> EVENT = EventFactory.createArrayBacked(ChatMessageCallback.class, arr -> args -> {
        for (var el : arr) el.execute(args);
    });
}
