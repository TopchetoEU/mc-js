package me.topchetoeu.mcscript.gui;

import java.io.File;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class OpenFileScreen extends FileDialogScreen {
    public interface OpenHandler {
        void fileOpened(File file);
    }

    public final OpenHandler handler;

    @Override
    protected String okButtonText() { return "Open"; }

    @Override
    protected void okPressed(File file) {
        handler.fileOpened(file);
        close();
    }
    @Override
    protected void selected(File file) {
        handler.fileOpened(file);
        close();
    }

    protected OpenFileScreen(Screen parent, Theme theme, File root, Text title, OpenHandler handler) {
        super(parent, theme, root, title);
        this.handler = handler;
    }
}
