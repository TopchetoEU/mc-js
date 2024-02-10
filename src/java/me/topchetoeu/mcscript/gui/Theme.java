package me.topchetoeu.mcscript.gui;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.math.ColorHelper.Argb;

public class Theme {
    public final Map<String, Integer> colors = new HashMap<>();
    public TextRenderer font;
    public int fontSize() { return font.fontHeight; }

    public Theme add(String name, int col) {
        colors.put(name, col);
        return this;
    }
    public Theme add(String name, int r, int g, int b, int a) {
        colors.put(name, Argb.getArgb(a, r, g, b));
        return this;
    }
    public int get(String name) {
        var res = colors.get(name);
        if (res == null) return 0;
        else return res;
    }

    public Theme setTheme(Theme t) {
        if (t == this) return this;
        colors.clear();
        colors.putAll(colors);
        return this;
    }
    public Theme setFont(TextRenderer font) {
        this.font = font;
        return this;
    }


    public static Theme dark(TextRenderer font) {
        return new Theme()
            .setFont(font)
            .add("padding-s", 2)
            .add("padding-m", 3)
            .add("padding-l", 5)
            .add("bg-1", 0, 0, 0, 255)
            .add("bg-2", 40, 40, 40, 255)
            .add("bg-3", 80, 80, 80, 255)
            .add("bg-4", 100, 100, 100, 255)
            .add("border", 255, 255, 255, 255)
            .add("text", 255, 255, 255, 255);
    }
}
