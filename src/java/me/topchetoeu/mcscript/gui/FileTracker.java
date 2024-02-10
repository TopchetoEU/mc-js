package me.topchetoeu.mcscript.gui;

import java.io.File;

public class FileTracker {
    public final File file;

    private long lastChange = 0;
    private boolean exists = false;
    private boolean created = false;
    private boolean deleted = false;
    private boolean changed = false;

    public void update() {
        if (file.exists()) {
            if (!exists) created = changed = true;
            exists = true;

            if (lastChange < file.lastModified()) {
                changed = true;
                lastChange = file.lastModified();
            }
        }
        else {
            if (exists) changed = deleted = true;
            exists = created = false;
            lastChange = 0;
        }
    }

    public boolean exists() {
        update();
        return exists;
    }
    public boolean wasChanged() {
        update();
        if (changed) {
            changed = false;
            return true;
        }
        else return false;
    }
    public boolean wasCreated() {
        update();
        if (created) {
            created = false;
            return true;
        }
        else return false;
    }
    public boolean wasDeleted() {
        update();
        if (deleted) {
            deleted = false;
            return true;
        }
        else return false;
    }

    public FileTracker(File file) {
        this.file = file;
        if (exists = file.exists()) lastChange = file.lastModified();
        else lastChange = 0;
    }
}
