package me.topchetoeu.mcscript.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.util.math.MatrixStack;

public class FileListWidget extends Widget {
    public interface SelectHandler {
        void selected(File f);
    }

    public final SelectHandler handler;

    private File res = null;
    private File currFile;
    private List<String> entries = new ArrayList<>();

    private float scroll = 0;


    private void setEntries() {
        if (currFile == null) {
            entries.clear();
            for (var root : File.listRoots()) {
                entries.add(root.toString());
            }
        }
        else if (currFile.isFile() || currFile.listFiles() == null) {
            res = currFile;
            handler.selected(currFile);
            currFile = currFile.getParentFile();
            return;
        }
        else {
            this.entries.clear();
            this.entries.add("(drives)");
            this.entries.add("..");
            for (var el : currFile.listFiles()) {
                if (el.isDirectory()) this.entries.add(el.getName() + "/");
            }
            for (var el : currFile.listFiles()) {
                if (el.isFile()) this.entries.add(el.getName());
            }
        }

        updateScroll();
    }
    private void updateScroll() {
        float _h = entries.size() * (theme.fontSize() + 4) - h;
        if (scroll > _h) scroll = _h;
        if (scroll < 0) scroll = 0;
    }
    private int entryHeight() {
        return theme.fontSize() + 4;
    }

    @Override
    protected boolean hasBorders() { return true; }

    public File result() {
        return res.toPath().toAbsolutePath().toFile();
    }
    public File cd() {
        return currFile.toPath().toAbsolutePath().toFile();
    }

    @Override
    public void render(MatrixStack mat, int mx, int my, float delta) {
        renderSetup();

        mat.push();
        mat.translate(x, y, 0);
        mx -= x; my -= y;

        fill(mat, 0, 0, w, h, theme.get("bg-1"));

        mat.push();
        mat.translate(0, -scroll, 0);
        my += scroll;

        for (int i = 0; i < entries.size(); i++) {
            int x1 = 0, y1 = i * entryHeight(), x2 = w, y2 = (i + 1) * entryHeight();
            if (mx >= x1 && mx < x2 && my >= y1 && my < y2) {
                fill(mat, x1, y1, x2, y2, theme.get("bg-3"));
            }
            else if (i % 2 == 0) {
                fill(mat, x1, y1, x2, y2, theme.get("bg-2"));
            }
            theme.font.draw(mat, entries.get(i), theme.get("padding-l"), y1 + 3, theme.get("text"));
        }

        mat.pop();
        mat.pop();

        renderFinalize();

        super.render(mat, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || !isMouseOver(mx, my)) return false;

        mx -= x; my -= y - scroll;

        int i = (int)(my / entryHeight());

        if (i < 0 || i >= entries.size()) return false;

        if (currFile == null) {
            currFile = new File(entries.get(i));
        }
        else {
            if (i == 0) currFile = null;
            else if (i == 1) currFile = currFile.getParentFile();
            else currFile = new File(currFile, entries.get(i));
        }

        setEntries();

        return true;
    }
    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        if (isMouseOver(mx, my)) {
            scroll -= amount * 10;
            updateScroll();
            return true;
        }
        return false;
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public FileListWidget(Theme theme, File root, SelectHandler handler) {
        super(theme);
        this.handler = handler;
        this.currFile = root.toPath().toAbsolutePath().toFile();
        if (this.currFile.isFile()) throw new RuntimeException("wtf");
        setEntries();
    }
}
