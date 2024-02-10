package me.topchetoeu.mcscript.gui;

import net.minecraft.client.util.math.MatrixStack;

public class ButtonWidget extends Widget {
    public interface ClickHandler {
        void click();
    }

    public final ClickHandler handler;
    public final String text;

    @Override
    public void render(MatrixStack mat, int mx, int my, float delta) {
        if (isMouseOver(mx, my)) fill(mat, x, y, x + w, y + h, theme.get("bg-2"));
        else fill(mat, x, y, x + w, y + h, theme.get("bg-1"));

        theme.font.draw(mat, text, x + (w - theme.font.getWidth(text)) / 2, y + (h - theme.fontSize()) / 2, theme.get("text"));

        super.render(mat, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (isMouseOver(mx, my) && button == 0) {
            handler.click();
            return true;
        }
        return false;
    }

    @Override
    protected boolean hasBorders() { return true; }
    
    public ButtonWidget(Theme theme, String text, ClickHandler handler) {
        super(theme);
        this.handler = handler;
        this.text = text;
    }
}
