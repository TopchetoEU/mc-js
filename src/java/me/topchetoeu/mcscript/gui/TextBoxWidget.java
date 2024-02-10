package me.topchetoeu.mcscript.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper.Argb;

public class TextBoxWidget extends Widget {
    private boolean multiline = false;
    private ArrayList<ArrayList<Character>> lines = new ArrayList<>();
    private String value;

    private int xPos = 0, yPos = 0;
    private float xScroll = 0, yScroll = 0;
    private int maxLineLen = 0;

    private int fontWidth() {
        return 6;
    }
    private int fontHeight() {
        return theme.fontSize();
    }

    private void updateValue() {
        var res = new StringBuilder();

        for (int i = 0; i < lines.size(); i++) {
            if (i != 0) res.append('\n');
            for (var c : lines.get(i)) {
                res.append(c);
            }
            if (maxLineLen < lines.get(i).size()) maxLineLen = lines.get(i).size();
        }

        value = res.toString();
    }
    private void updatePos() {
        int realX = xPos;
        if (yPos >= lines.size()) yPos = lines.size() - 1;
        if (yPos < 0) yPos = 0;
        
        if (realX > lines.get(yPos).size()) realX = lines.get(yPos).size();
        if (xPos < 0) xPos = 0;

        xScroll = Math.min(xScroll, (maxLineLen) * fontWidth() - fontWidth());
        yScroll = Math.min(yScroll, (lines.size()) * fontHeight() - fontHeight());

        if (xScroll < 0) xScroll = 0;
        if (yScroll < 0) yScroll = 0;
    }
    private void updateScroll() {
        updatePos();

        int realX = getCursorX();

        yScroll = Math.min(yScroll, yPos * fontHeight());
        yScroll = Math.max(yScroll, (yPos + 1) * fontHeight() - h + theme.get("padding-s") * 2);

        xScroll = Math.min(xScroll, realX * fontWidth());
        xScroll = Math.max(xScroll, realX * fontWidth() - w + theme.get("padding-s") * 2);
    }

    private List<Character> getLine() {
        updatePos();
        return lines.get(yPos);
    }

    public String getValue() { return value; }
    public TextBoxWidget setValue(String val) {
        lines.clear();
        for (var line : val.split("\n")) {
            var chars = new ArrayList<Character>();

            for (var c : line.toCharArray()) {
                if (c == '\r') continue;
                chars.add(c);
            }

            lines.add(chars);
        }

        value = val;

        updatePos();
        textChanged();

        return this;
    }

    public int getCursorX() {
        updatePos();
        if (xPos > getLine().size()) return getLine().size();
        else return xPos;
    }
    public void setCursorX(int val) {
        xPos = val;

        if (xPos < 0) xPos = 0;
        if (xPos > getLine().size()) xPos = getLine().size();

        updateScroll();
    }
    public void changeCursorX(int delta) {
        setCursorX(getCursorX() + delta);
    }

    public int getCursorY() {
        updatePos();
        return yPos;
    }
    public void setCursorY(int val) {
        yPos = val;

        if (yPos < 0) yPos = 0;
        if (yPos >= lines.size()) yPos = lines.size() - 1;

        updateScroll();
    }
    public void changeCursorY(int delta) {
        setCursorY(yPos + delta);
    }

    public float getScrollX() {
        updatePos();
        
        return xScroll;
    }
    public void setScrollX(float val) {
        xScroll = val;
        updatePos();
    }
    public void changeScrollX(float delta) {
        setScrollX(getScrollX() + delta);
    }

    public float getScrollY() {
        updatePos();
        return yScroll;
    }
    public void setScrollY(float val) {
        yScroll = val;
        updatePos();
    }
    public void changeScrollY(float delta) {
        setScrollY(getScrollY() + delta);
    }

    public boolean isMultiline() { return multiline; }
    public TextBoxWidget setMultiline(boolean value) {
        if (value == multiline) return this;

        if (value) {
            setHeight(theme.get("padding-s") * 2 + theme.fontSize());
            var first = lines.get(0);
            lines.clear();
            lines.add(first);
            textChanged();
        }

        multiline = value;
        return this;
    }

    @Override
    protected boolean hasBorders() { return true; }

    public void input(char c) {
        updatePos();
        xPos = getCursorX();
        getLine().add(xPos, c);
        updateValue();
        xPos++;
        updateScroll();
        textChanged();
    }
    public void newLine() {
        if (!multiline) return;
        updatePos();

        var newLine = new ArrayList<Character>();
        var currLine = getLine();

        while (currLine.size() > xPos) {
            newLine.add(currLine.remove(xPos));
        }

        lines.add(yPos + 1, newLine);

        changeCursorY(1);
        setCursorX(0);

        updateScroll();
        updateValue();
        textChanged();
    }
    public void backspace() {
        updatePos();

        if (xPos == 0) {
            if (yPos == 0) return;
            var line = lines.remove(yPos);
            changeCursorY(-1);
            setCursorX(getLine().size());
            getLine().addAll(line);
        }
        else {
            changeCursorX(-1);
            getLine().remove(xPos);
        }

        updateScroll();
        updateValue();
        textChanged();
    }
    public void delete() {
        updatePos();

        if (xPos == getLine().size()) {
            if (yPos == lines.size() - 1) return;
            getLine().addAll(lines.remove(yPos + 1));
        }
        else {
            getLine().remove(xPos);
        }

        updateScroll();
        updateValue();
        textChanged();
    }
    public void home() {
        setCursorX(0);
        updateScroll();
    }
    public void end() {
        setCursorX(getLine().size());
        updateScroll();
    }
    public void tab() {
        for (int i = getCursorX() % 4; i < 4; i++) {
            input(' ');
        }
    }
    public void right(boolean words) {
        if (words) {
            var line = getLine();
    
            if (getCursorX() >= getLine().size()) {
                if (getCursorY() == lines.size() - 1) return;
                changeCursorY(1);
                setCursorX(0);
            }
            else {
                if (line.get(getCursorX()) == ' ') {
                    for (; getCursorX() < getLine().size(); changeCursorX(1)) {
                        if (line.get(getCursorX()) != ' ') break;
                    }
                }
                else {
                    for (; getCursorX() < getLine().size(); changeCursorX(1)) {
                        if (line.get(getCursorX()) == ' ') break;
                    }
                    for (; getCursorX() < getLine().size(); changeCursorX(1)) {
                        if (line.get(getCursorX()) != ' ') break;
                    }
                }
            }
        }
        else if (getCursorX() >= getLine().size()) {
            if (getCursorY() == lines.size() - 1) return;
            changeCursorY(1);
            setCursorX(0);
        }
        else changeCursorX(1);
        updateScroll();
    }
    public void left(boolean words) {
        if (getCursorX() == 0) {
            if (getCursorY() == 0) return;
            changeCursorY(-1);
            setCursorX(getLine().size());
        }
        else if (words) {
            var line = getLine();
    
            if (line.get(getCursorX() - 1) == ' ') {
                for (; getCursorX() > 0; changeCursorX(-1)) {
                    if (line.get(getCursorX() - 1) != ' ') break;
                }
            }
            else {
                for (; getCursorX() > 0; changeCursorX(-1)) {
                    if (line.get(getCursorX() - 1) == ' ') break;
                }
                for (; getCursorX() > 0; changeCursorX(-1)) {
                    if (line.get(getCursorX() - 1) != ' ') break;
                }
            }
        }
        else changeCursorX(-1);
        updateScroll();
    }

    @Override
    public Widget setRect(int x, int y, int w, int h) {
        super.setRect(x, y, w, h);
        updateScroll();
        return this;
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        try {
            boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;

            switch (keyCode) {
                case GLFW.GLFW_KEY_BACKSPACE: backspace(); return true;
                case GLFW.GLFW_KEY_DELETE: delete(); return true;
                case GLFW.GLFW_KEY_ENTER: newLine(); return true;
                case GLFW.GLFW_KEY_TAB: tab(); return true;

                case GLFW.GLFW_KEY_UP: changeCursorY(-1); return true;
                case GLFW.GLFW_KEY_DOWN: changeCursorY(1); return true;
                case GLFW.GLFW_KEY_LEFT: left(ctrl); return true;
                case GLFW.GLFW_KEY_RIGHT: right(ctrl); return true;

                case GLFW.GLFW_KEY_END: end(); return true;
                case GLFW.GLFW_KEY_HOME: home(); return true;

                case GLFW.GLFW_KEY_PAGE_DOWN: {
                    if (ctrl) changeScrollX(w / 2);
                    else changeScrollY(h / 2);
                    return true;
                }
                case GLFW.GLFW_KEY_PAGE_UP: {
                    if (ctrl) changeScrollX(-w / 2);
                    else changeScrollY(-h / 2);
                    return true;
                }
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }

        return false;
    }
    @Override
    public boolean charTyped(char c, int modifiers) {
        if (c == '\n') newLine();
        input(c);
        return true;
    }
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isMouseOver(mx, my)) return false;
        mx -= x - xScroll + theme.get("padding-s");
        my -= y - yScroll + theme.get("padding-s");

        setCursorY((int)Math.floor(my / fontHeight()));
        setCursorX((int)Math.round(mx / fontWidth()));
        return true;
    }
    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        if (!isMouseOver(mx, my)) return false;
        changeScrollY(-(float)amount * 10);
        return true;
    }
    @Override
    public void render(MatrixStack mat, int mouseX, int mouseY, float delta) {
        renderSetup();

        mat.push();
        mat.translate(x, y, 0);

        var width = 6;
        var height = theme.fontSize();

        fill(mat, 0, 0, this.w, this.h, theme.get("bg-1"));

        mat.push();
        mat.translate(theme.get("padding-s") - xScroll, theme.get("padding-s") - yScroll, 0);

        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);

            for (int j = 0; j < line.size(); j++) {
                var c = line.get(j);
                float offX = (width - theme.font.getWidth(c + "")) / 2f;
                theme.font.draw(mat, c + "", j * width + offX, i * height, theme.get("text"));
            }
        }

        drawVerticalLine(mat, xPos * width, yPos * height - 1, (yPos + 1) * height, Argb.getArgb(255, 255, 0, 0));

        mat.pop();
        mat.pop();

        renderFinalize();

        super.render(mat, mouseX, mouseY, delta);
    }

    protected void textChanged() { }

    @Override
    public void appendNarrations(NarrationMessageBuilder nmb) {
        nmb.put(NarrationPart.USAGE, "Text box");
    }
    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    public TextBoxWidget(Theme theme) {
        super(theme);
        this.h = theme.fontSize() + theme.get("padding-s") * 2;
        this.setValue("");
        updateScroll();
    }
}
