package me.topchetoeu.mcscript.gui;

import java.io.File;
import java.io.IOException;

import org.lwjgl.glfw.GLFW;

import me.topchetoeu.mcscript.gui.OpenFileScreen.OpenHandler;
import me.topchetoeu.mcscript.gui.SaveFileScreen.SaveHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class DevScreen extends Screen implements OpenHandler, SaveHandler {
    private enum Focus {
        EDITOR,
        MENU,
    }

    private Theme theme;
    private DevEditorWidget editor;
    private DevTabListWidget tabs;

    private Focus focus = Focus.EDITOR;
    private int untitledI = 1;

    // private int altI = 0;

    // private void drawFileMenu(MatrixStack matrices) {
    //     int h = theme.fontSize() + 2;

    //     int x = 1;
    //     int y = height - 15;
    //     int i = 0;

    //     fill(matrices, 0, this.height - h, this.width, this.height, Argb.getArgb(255, 40, 40, 40));

    //     for (var el : ListExt.of("Save", "Open", "Close", "Quit")) {
    //         int w = font.getWidth(el) + 10;
            
    //         if (focus == Focus.MENU && altI == i++) {
    //             fill(matrices, x, y, x + w, y + h, Argb.getArgb(255, 80, 80, 80));
    //         }
    //         font.draw(matrices, el, x + 5, y + 1, Argb.getArgb(255, 255, 255, 255));
    //         x += w;
    //     }
    // }

    public void fileOpened(File file) {
        try {
            tabs.add(new DevTextBoxWidget(file, theme));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void fileSaved(File file) {
        var tab = tabs.getTab();
        if (tab != null) {
            try {
                tab.saveFileAs(file);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getUntitled() {
        while (true) {
            int i = untitledI++;
            if (!new File("untitled" + i + ".ms").exists()) return i;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        try {
            boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
            boolean alt = (modifiers & GLFW.GLFW_MOD_ALT) != 0;
            boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
            if (ctrl) {
                if (keyCode == GLFW.GLFW_KEY_N) {
                    tabs.add(new DevTextBoxWidget(new File("untitled" + getUntitled() + ".ms"), theme));
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_W) {
                    tabs.close();
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_S) {
                    if (!alt) tabs.getTab().saveFile();
                    else client.setScreen(new SaveFileScreen(this, theme, new File(""), Text.of("Save file:"), this));
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_O) {
                    client.setScreen(new OpenFileScreen(this, theme, new File(""), Text.of("Open file:"), this));
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_TAB) {
                    if (shift) tabs.prev();
                    else tabs.next();
                    return true;
                }
            }
            else {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                client.setScreen(null);
                return true;
            }
                if (keyCode == GLFW.GLFW_KEY_LEFT_ALT) {
                    if (focus == Focus.MENU) focus = Focus.EDITOR;
                    else focus = Focus.MENU;
                }
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }

        return this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers);
    }

    @SuppressWarnings("all")
    protected void init() {
        if (theme == null) {
            theme = Theme.dark(MinecraftClient.getInstance().textRenderer);
            editor = new DevEditorWidget(theme);
            tabs = new DevTabListWidget(theme, tab -> editor.set(tab));
        }

        addDrawableChild(editor);
        addDrawableChild(tabs);
        setFocused(editor);

        editor.setRect(0, 0, width - 100, height);
        tabs.setRect(width - 100, 0, 100, height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var res = super.mouseClicked(mouseX, mouseY, button);
        return res;
    }

    // @Override
    // public boolean changeFocus(boolean lookForwards) {
    //     return true;
    // }

    public DevScreen() {
        super(Text.of("McScript IDE"));

        // try {
            // this.tabs.add(new DevTab(new File("test1.ms"), font));
            // this.tabs.add(new DevTab(new File("test1.ms"), font));
            // this.tabs.add(new DevTab(new File("test3.ms"), font));
        // }
        // catch (IOException e) {
        //     e.printStackTrace();
        // }
        // this.tabs.add(new DevTab(0, "", "test2.ms"));
        // this.tabs.add(new DevTab(0, "", "test3.ms"));
    }
}
