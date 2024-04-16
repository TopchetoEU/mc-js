package me.topchetoeu.mcscript.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.topchetoeu.mcscript.events.ChatMessageCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Inject(method = "sendMessage", at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"), cancellable = true)
    private void onSendMessage(String chatText, boolean addToHistory, CallbackInfoReturnable<Boolean> cbi) {
        var client = MinecraftClient.getInstance();

        var args = new ChatMessageCallback.ChatArgs();
        args.message = chatText;
        args.cancelled = false;
        ChatMessageCallback.EVENT.invoker().execute(args);

        if (!args.cancelled) {
            chatText = args.message;

            if (chatText.startsWith("/")) {
                client.player.networkHandler.sendChatCommand(chatText.substring(1));
            } else {
                client.player.networkHandler.sendChatMessage(chatText);
            }
        }

        cbi.setReturnValue(true);
        cbi.cancel();
    }
}
