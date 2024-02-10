package me.topchetoeu.mcscript.gui;

import java.nio.file.Files;

import net.minecraft.client.util.math.MatrixStack;

import java.io.File;
import java.io.IOException;

public class DevTextBoxWidget extends TextBoxWidget {
    private File file;
    private FileTracker tracker;

    private boolean saved;
    private String conflict = null;

    public File getFile() {
        return file;
    }

    public boolean fileExists() {
        return tracker.exists();
    }
    public boolean fileIsSaved() {
        return saved;
    }
    public boolean fileHasConflict() {
        return conflict != null;
    }
    public String getFileConflictReason() {
        return conflict;
    }
    public void saveFile() throws IOException {
        Files.writeString(file.toPath(), getValue());
        tracker.wasChanged();
        saved = true;
        conflict = null;
    }
    public void saveFileAs(File file) throws IOException {
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), getValue());
        tracker.wasChanged();
        this.file = file;
        this.tracker = new FileTracker(file);
        saved = true;
        conflict = null;
    }


    @Override
    protected boolean hasBorders() { return false; }
    @Override
    protected void textChanged() {
        saved = false;
    }

    public void update() {
        if (tracker.wasCreated()) {
            conflict = "File was created";
            saved = false;
        }

        try {
            if (tracker.wasChanged()) {
                if (saved) this.setValue(Files.readString(file.toPath()));
                else conflict = "File was changed";
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (tracker.wasDeleted()) {
            saved = false;
            conflict = null;
        }
    }
    @Override
    public void render(MatrixStack mat, int x, int y, float delta) {
        update();
        super.render(mat, x, y, delta);
    }

    public DevTextBoxWidget(File file, Theme theme) throws IOException {
        super(theme);
        this.file = file;
        this.tracker = new FileTracker(file);
        if (tracker.exists()) {
            saved = true;
            this.setMultiline(true);
            this.setValue(Files.readString(file.toPath()));
        }
        else saved = false;
    }
}
