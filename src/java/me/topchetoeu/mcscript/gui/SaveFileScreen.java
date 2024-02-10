package me.topchetoeu.mcscript.gui;

import java.io.File;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class SaveFileScreen extends FileDialogScreen {
    public interface SaveHandler {
        void fileSaved(File file);
    }

    public final SaveHandler handler;

    @Override
    protected String okButtonText() { return "Save"; }

    @Override
    protected void okPressed(File file) {
        handler.fileSaved(file);
        close();
    }
    @Override
    protected void selected(File file) {
        handler.fileSaved(file);
        close();
    }

    protected SaveFileScreen(Screen parent, Theme theme, File root, Text title, SaveHandler handler) {
        super(parent, theme, root, title);
        this.handler = handler;
    }
}
