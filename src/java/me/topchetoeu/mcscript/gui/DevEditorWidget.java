package me.topchetoeu.mcscript.gui;

import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;

public class DevEditorWidget extends Widget {
    private DevTextBoxWidget tab;

    private void updateTab() {
        if (tab == null) return;
        tab.setMultiline(true).setRect(x, y + theme.fontSize() + theme.get("padding-s"), w, h - theme.fontSize() - theme.get("padding-s"));
        tab.theme.setTheme(theme);
    }

    @Override
    protected boolean hasBorders() { return false; }
    @Override
    public Widget setRect(int x, int y, int w, int h) {
        super.setRect(x, y, w, h);
        updateTab();
        return this;
    }

    public DevTextBoxWidget get() {
        return tab;
    }
    public void set(DevTextBoxWidget tab) {
        if (this.tab == tab) return;
        this.tab = tab;
        updateTab();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (tab == null) return false;
        return tab.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (tab == null) return false;
        return tab.charTyped(chr, modifiers);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tab == null) return false;
        return tab.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (tab == null) return false;
        return tab.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder nmb) {
    }
    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void render(MatrixStack mat, int mx, int my, float delta) {
        if (tab == null) {
            String text = "No file opened :(";
            int cx = w - theme.font.getWidth(text);
            int cy = h - theme.fontSize();

            fill(mat, x, y, x + w, y + h, theme.get("bg-1"));
            theme.font.draw(mat, text, x + cx / 2, x + cy / 2, theme.get("text"));
        }
        else {
            tab.render(mat, mx, my, delta);
            fill(mat, x, y, x + w, y + theme.fontSize() + 2, theme.get("bg-2"));
            theme.font.draw(mat, tab.getFile().getAbsoluteFile().toString(), x + 5, y + 2, theme.get("text"));
        }
    }

    public DevEditorWidget(Theme theme) {
        super(theme);
    }
}
