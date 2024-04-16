package me.topchetoeu.mcscript.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.topchetoeu.mcscript.events.ScreenHandlerEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo cbi) {
        if (!(player instanceof ServerPlayerEntity sp)) return;

        var handle = (ScreenHandler)(Object)this;
        var cancelled = !ScreenHandlerEvents.SLOT_CLICKED.invoker().slotClicked(handle, slotIndex, button, actionType, sp);

        if (cancelled) cbi.cancel();
    }
    @Inject(method = "onClosed", at = @At("HEAD"), cancellable = true)
    private void onClosed(PlayerEntity player, CallbackInfo cbi) {
        if (!(player instanceof ServerPlayerEntity sp)) return;

        var handle = (ScreenHandler)(Object)this;
        var cancelled = !ScreenHandlerEvents.SCREEN_CLOSE.invoker().screenClosed(handle, sp);

        if (cancelled) cbi.cancel();
    }
}
