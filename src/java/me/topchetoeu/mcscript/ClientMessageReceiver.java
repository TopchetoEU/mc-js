package me.topchetoeu.mcscript;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class ClientMessageReceiver implements MessageReceiver {
    public final MinecraftClient client = MinecraftClient.getInstance();

    @Override
    public void sendError(String msg) {
        client.player.sendMessage(Text.literal("").append(msg).formatted(Formatting.RED));
    }
    @Override
    public void sendInfo(String msg) {
        client.player.sendMessage(Text.of(msg));
    }
}