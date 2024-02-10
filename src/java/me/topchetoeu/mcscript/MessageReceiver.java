package me.topchetoeu.mcscript;

public interface MessageReceiver {
    public void sendError(String msg);
    public void sendInfo(String msg);
}