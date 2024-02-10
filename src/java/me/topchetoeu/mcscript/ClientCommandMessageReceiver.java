package me.topchetoeu.mcscript;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ClientCommandMessageReceiver implements MessageReceiver {
    public final FabricClientCommandSource receiver;

    @Override
    public void sendError(String msg) {
        receiver.sendError(Text.of(msg));
    }
    @Override
    public void sendInfo(String msg) {
        receiver.sendFeedback(Text.of(msg));
    }

    public ClientCommandMessageReceiver(FabricClientCommandSource receiver) {
        this.receiver = receiver;
    }
}