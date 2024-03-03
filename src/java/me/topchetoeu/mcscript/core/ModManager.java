package me.topchetoeu.mcscript.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.topchetoeu.jscript.common.Filename;
import me.topchetoeu.jscript.common.json.JSON;
import me.topchetoeu.jscript.core.Compiler;
import me.topchetoeu.jscript.core.Engine;
import me.topchetoeu.jscript.core.Environment;
import me.topchetoeu.jscript.core.EventLoop;
import me.topchetoeu.jscript.lib.Internals;
import me.topchetoeu.jscript.utils.JSCompiler;
import me.topchetoeu.jscript.utils.filesystem.File;
import me.topchetoeu.jscript.utils.filesystem.Filesystem;
import me.topchetoeu.jscript.utils.filesystem.PhysicalFilesystem;
import me.topchetoeu.jscript.utils.filesystem.RootFilesystem;
import me.topchetoeu.jscript.utils.filesystem.STDFilesystem;
import me.topchetoeu.jscript.utils.modules.ModuleRepo;
import me.topchetoeu.jscript.utils.modules.RootModuleRepo;
import me.topchetoeu.jscript.utils.permissions.PermissionsManager;
import me.topchetoeu.jscript.utils.permissions.PermissionsProvider;

public class ModManager {
    public final String codeFolder, dataFolder;
    public final List<Mod> mods = new ArrayList<>();

    private void loadMod(String folder) throws IOException {
        var filename = Path.of(folder, "manifest.json");
        if (!filename.toFile().exists() || !filename.toFile().isFile()) return;

        var rawManifest = new String(Files.readAllBytes(filename));
        var manifest = JSON.parse(Filename.fromFile(filename.toFile()), rawManifest).map();

        var name = manifest.string("name");
        var version = manifest.string("version");
        var author = manifest.string("author");
        var main = manifest.string("main");
        var mainSrc = new String(Files.readAllBytes(Path.of(codeFolder.toString(), name, main)));

        var env = new Environment();
        var loop = new Engine();

        var fs = new RootFilesystem(PermissionsProvider.get(env));
        fs.protocols.put("file", new PhysicalFilesystem(Path.of(dataFolder.toString(), name).toString()));
        fs.protocols.put("code", new PhysicalFilesystem(Path.of(codeFolder.toString(), name).toString()));

        Files.createDirectories(Path.of(codeFolder.toString(), name));

        var perms = new PermissionsManager();
        perms.add("jscript.file.read:**");
        perms.add("jscript.file.read:std://in");
        perms.add("jscript.file.write:std://out");
        perms.add("jscript.file.write:std://err");
        perms.add("jscript.file.write:file://**");

        var modules = new RootModuleRepo();
        modules.repos.put("file", ModuleRepo.ofFilesystem(fs.protocols.get("code")));

        Internals.apply(env);
        env.add(EventLoop.KEY, loop);
        env.add(Filesystem.KEY, fs);
        env.add(PermissionsProvider.KEY, perms);
        env.add(Compiler.KEY, new JSCompiler(env));

        loop.pushMsg(false, env, new Filename("code", main), mainSrc, null);

        var mod = new Mod(name, author, version, loop, env, fs);
        mods.add(mod);
    }

    public void load() throws IOException {
        for (var file : Files.list(Path.of(codeFolder)).collect(Collectors.toList())) {
            if (!Files.isDirectory(file)) continue;
            loadMod(file.toString());
        }
    }
    public void setSTD(File in, File out, File err) {
        var std = new STDFilesystem();

        std.add("in", in);
        std.add("out", out);
        std.add("err", err);

        for (var mod : mods) {
            mod.fs.protocols.put("std", std);
        }
    }
    public void start() {
        for (var mod : mods) {
            mod.loop.start();
        }
    }

    public ModManager(String folder, String dataFolder) {
        this.codeFolder = folder;
        this.dataFolder = dataFolder;
    }
}