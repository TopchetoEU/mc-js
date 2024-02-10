package me.topchetoeu.mcscript.gui;

import org.joml.Vector4f;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;

public abstract class Widget extends DrawableHelper implements Element, Drawable, Selectable {
    public final Theme theme;
    protected int x, y, w, h;
    
    protected abstract boolean hasBorders();

    public final int getX() { return x; }
    public final Widget setX(int val) { return setPos(val, y); }

    public final int getY() { return y; }
    public final Widget setY(int val) { return setPos(x, val); }

    public final int getWidth() { return w; }
    public final Widget setWidth(int value) { return setSize(value, h); }

    public final int getHeight() { return h; }
    public final Widget setHeight(int value) { return setSize(w, value); }

    public final Widget setPos(int x, int y) {
        return setRect(x, y, w, h);
    }
    public final Widget setSize(int w, int h) {
        return setRect(x, y, w, h);
    }
    public Widget setRect(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        return this;
    }

    @Override
    public void setFocused(boolean focused) {
    }
    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public boolean isMouseOver(double mx, double my) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder var1) {
    }
    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    protected void renderSetup() {
        var m = RenderSystem.getProjectionMatrix();
        var pos = m.transform(new Vector4f(x, y, 0, 1));
        var scale = m.transform(new Vector4f(w, h, 0, 0));

        int wndw = MinecraftClient.getInstance().getWindow().getWidth();
        int wndh = MinecraftClient.getInstance().getWindow().getHeight();

        int x = (int)Math.round((pos.x + 1) / 2 * wndw);
        int y = (int)Math.round((-pos.y + 1) / 2 * wndh);
        int w = (int)Math.round(scale.x / 2 * wndw);
        int h = (int)Math.round(-scale.y / 2 * wndh);

        y = wndh - y - h;

        RenderSystem.enableScissor(x, y, w, h);
    }
    protected void renderFinalize() {
        RenderSystem.disableScissor();
    }

    @Override
    public void render(MatrixStack mat, int mx, int my, float delta) {
        int col = theme.get("border");
        drawHorizontalLine(mat, x - 1, x + w, y - 1, col);
        drawHorizontalLine(mat, x - 1, x + w, y + h, col);
        drawVerticalLine(mat, x - 1, y - 1, y + h, col);
        drawVerticalLine(mat, x + w, y - 1, y + h, col);
    }

    public Widget(Theme theme) {
        this.theme = theme;
    }
}
