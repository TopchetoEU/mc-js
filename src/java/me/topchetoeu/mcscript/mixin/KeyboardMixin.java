package me.topchetoeu.mcscript.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onChar", at = @At("HEAD"))
    private void onChar(long window, int codePoint, int modifiers, CallbackInfo cbi) {
        var client = MinecraftClient.getInstance();
        for (var ch : Character.toChars(codePoint)) {
            if (client.currentScreen == null && client.getOverlay() == null && ch == '#') {
                ((MinecraftClientMixin)client).invokeOpenChatScreen("");
            }
        }
    }
}
