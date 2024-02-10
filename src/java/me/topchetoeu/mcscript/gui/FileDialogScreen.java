package me.topchetoeu.mcscript.gui;

import java.io.File;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public abstract class FileDialogScreen extends Screen {
    public final Screen parent;
    public final Theme theme;
    protected final FileListWidget list;
    protected final ButtonWidget ok;
    protected final ButtonWidget cancel;
    protected final TextBoxWidget name;

    protected abstract String okButtonText();
    protected abstract void okPressed(File file);
    protected abstract void selected(File file);

    private void submit() {
        okPressed(new File(list.cd(), name.getValue()).toPath().toAbsolutePath().toFile());
    }

    private int headerHeight() {
        return theme.get("padding-m") * 2 + theme.fontSize();
    }
    private int footerHeight() {
        return (theme.get("padding-m") + theme.get("padding-s")) * 2 + theme.fontSize();
    }

    @Override
    protected void init() {
        int padl = theme.get("padding-l");
        int padm = theme.get("padding-m");
        int pads = theme.get("padding-s");

        int btnw = 50;
        int btnh = pads * 2 + theme.fontSize();
        int btny = height - padm - btnh;
        int inputw = width - 100 - padl * 4;

        list.setRect(padl, headerHeight(), width - padl * 2, height - headerHeight() - footerHeight());
        ok.setRect(padl, btny, btnw, btnh);
        cancel.setRect(padl + 50 + padl, btny, btnw, btnh);
        name.setPos(padl + 100 + padl * 2, btny).setWidth(inputw);
        addDrawableChild(list);
        addDrawableChild(ok);
        addDrawableChild(cancel);
        addDrawableChild(name);
    }

    @Override
    public void render(MatrixStack mat, int mx, int my, float delta) {
        fill(mat, 0, 0, width, height, theme.get("bg-1"));
        list.render(mat, mx, my, delta);
        fill(mat, list.x, 0, list.w + list.x, list.y - 1, theme.get("bg-1"));
        fill(mat, list.x, list.y + list.h + 1, list.w + list.x, height, theme.get("bg-1"));
        theme.font.draw(mat, title, theme.get("padding-l"), theme.get("padding-m"), theme.get("text"));
        ok.render(mat, mx, my, delta);
        cancel.render(mat, mx, my, delta);
        name.render(mat, mx, my, delta);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            submit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected FileDialogScreen(Screen parent, Theme theme, File root, Text title) {
        super(title);
        this.parent = parent;
        this.theme = theme;
        list = new FileListWidget(theme, root, this::selected);
        name = new TextBoxWidget(theme);
        ok = new ButtonWidget(theme, okButtonText(), this::submit);
        cancel = new ButtonWidget(theme, "Cancel", this::close);
    }
}
