package me.topchetoeu.mcscript;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ServerCommandMessageReceiver implements MessageReceiver {
    public final ServerCommandSource receiver;

    @Override
    public void sendInfo(String msg) {
        receiver.sendFeedback(Text.of(msg), false);
    }
    @Override
    public void sendError(String msg) {
        receiver.sendError(Text.of(msg));
    }

    public ServerCommandMessageReceiver(ServerCommandSource receiver) {
        this.receiver = receiver;
    }
}