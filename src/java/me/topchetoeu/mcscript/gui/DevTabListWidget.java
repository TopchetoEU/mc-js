package me.topchetoeu.mcscript.gui;

import java.util.ArrayList;

import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;

public class DevTabListWidget extends Widget {
    public static interface TabChangeHandle {
        void tabChanged(DevTextBoxWidget tab);
    }

    @Override
    protected boolean hasBorders() { return false; }

    public final TabChangeHandle changeHandle;

    private int tabI = -1;
    private ArrayList<DevTextBoxWidget> tabs = new ArrayList<>();


    public DevTextBoxWidget getTab() {
        if (tabs.size() == 0) return null;
        return tabs.get(tabI);
    }
    public void setTab(DevTextBoxWidget tab) {
        if (tabs.contains(tab)) tabs.add(tab);
        tabI = tabs.indexOf(tab);
        changeHandle.tabChanged(getTab());
    }

    public void next() {
        if (tabs.size() < 2) return;
        tabI++;
        if (tabI >= tabs.size()) tabI = 0;
        changeHandle.tabChanged(getTab());
    }
    public void prev() {
        if (tabs.size() < 2) return;
        tabI--;
        if (tabI < 0) tabI = tabs.size() - 1;
        changeHandle.tabChanged(getTab());
    }
    public void add(DevTextBoxWidget tab) {
        tabs.add(++tabI, tab);
        changeHandle.tabChanged(getTab());
    }
    public void close() {
        if (tabs.size() == 0) return;
        tabs.remove(tabI);
        if (tabI == tabs.size()) {
            tabI--;
            changeHandle.tabChanged(getTab());
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isMouseOver(mx, my)) return false;
        mx -= x; my -= y;

        int i = (int)((my - theme.get("padding-l")) / (theme.fontSize() + 4));
        if (i >= 0 && i < tabs.size()) {
            if (button == 2) {
                tabs.remove(tabI);
                if (tabI == tabs.size()) {
                    tabI--;
                    changeHandle.tabChanged(getTab());
                }
            }
            else {
                tabI = i;
                changeHandle.tabChanged(getTab());
            }
        }

        return true;
    }
    @Override
    public void render(MatrixStack mat, int mx, int my, float delta) {
        int h = theme.fontSize() + 4;

        mat.push();
        mat.translate(x, y, 0);

        fill(mat, 0, 0, w, theme.get("padding-l"), theme.get("bg-3"));

        mat.push();
        mat.translate(0, theme.get("padding-l"), 0);

        for (int i = 0; i < tabs.size(); i++) {
            int col = theme.get("bg-3");
            if (tabI == i) col = theme.get("bg-4");
            fill(mat, 0, i * h, w, (i + 1) * h, col);

            var name = tabs.get(i).getFile().getName();
            if (!tabs.get(i).fileIsSaved()) name = "*" + name;
            theme.font.draw(mat, name, 5, i * h + 2, theme.get("text"));
        }

        mat.pop();

        fill(mat, 0, tabs.size() * h + theme.get("padding-l"), w, this.h, theme.get("bg-3"));

        mat.pop();
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder var1) {
        
    }
    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    public DevTabListWidget(Theme theme, TabChangeHandle changeHandle) {
        super(theme);
        this.changeHandle = changeHandle;
    }
}
