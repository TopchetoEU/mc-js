package me.topchetoeu.mcscript.core;

import me.topchetoeu.jscript.runtime.Engine;
import me.topchetoeu.jscript.runtime.Environment;
import me.topchetoeu.jscript.runtime.EventLoop;
import me.topchetoeu.jscript.utils.filesystem.RootFilesystem;

public class Mod {
    public final String name;
    public final String author;
    public final String version;

    public final Engine loop;
    public final Environment environment;
    public final RootFilesystem fs;

    public Mod(String name, String author, String version, Engine loop, Environment environment, RootFilesystem fs) {
        this.name = name;
        this.author = author;
        this.version = version;
        this.loop = loop;
        this.fs = fs;
        this.environment = environment;
        this.environment.add(EventLoop.KEY, loop);
    }
}