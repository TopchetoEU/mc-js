package me.topchetoeu.mcscript.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public class ScreenHandlerEvents {
    public static interface SlotClickHandler {
        boolean slotClicked(ScreenHandler handler, int slotIndex, int button, SlotActionType actionType, ServerPlayerEntity player);
    }
    public static interface ScreenCloseHandler {
        boolean screenClosed(ScreenHandler handler, ServerPlayerEntity player);
    }

    public static final Event<SlotClickHandler> SLOT_CLICKED = EventFactory.createArrayBacked(SlotClickHandler.class, arr -> (handler, slotIndex, button, actionType, player) ->  {
        for (var el : arr) {
            if (!el.slotClicked(handler, slotIndex, button, actionType, player)) return false;
        }

        return true;
    });
    public static final Event<ScreenCloseHandler> SCREEN_CLOSE = EventFactory.createArrayBacked(ScreenCloseHandler.class, arr -> (handler, player) ->  {
        for (var el : arr) {
            if (!el.screenClosed(handler, player)) return false;
        }

        return true;
    });
}
