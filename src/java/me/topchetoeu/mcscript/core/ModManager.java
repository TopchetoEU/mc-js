package me.topchetoeu.mcscript.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import me.topchetoeu.jscript.common.Filename;
import me.topchetoeu.jscript.common.json.JSON;
import me.topchetoeu.jscript.runtime.Compiler;
import me.topchetoeu.jscript.runtime.Engine;
import me.topchetoeu.jscript.runtime.Environment;
import me.topchetoeu.jscript.runtime.EventLoop;
import me.topchetoeu.jscript.runtime.debug.DebugContext;
import me.topchetoeu.jscript.runtime.exceptions.EngineException;
import me.topchetoeu.jscript.runtime.values.Values;
import me.topchetoeu.jscript.lib.Internals;
import me.topchetoeu.jscript.utils.JSCompiler;
import me.topchetoeu.jscript.utils.debug.DebugServer;
import me.topchetoeu.jscript.utils.debug.SimpleDebugger;
import me.topchetoeu.jscript.utils.filesystem.ActionType;
import me.topchetoeu.jscript.utils.filesystem.EntryType;
import me.topchetoeu.jscript.utils.filesystem.ErrorReason;
import me.topchetoeu.jscript.utils.filesystem.File;
import me.topchetoeu.jscript.utils.filesystem.FileStat;
import me.topchetoeu.jscript.utils.filesystem.Filesystem;
import me.topchetoeu.jscript.utils.filesystem.FilesystemException;
import me.topchetoeu.jscript.utils.filesystem.Mode;
import me.topchetoeu.jscript.utils.filesystem.PhysicalFilesystem;
import me.topchetoeu.jscript.utils.filesystem.RootFilesystem;
import me.topchetoeu.jscript.utils.filesystem.STDFilesystem;
import me.topchetoeu.jscript.utils.modules.ModuleRepo;
import me.topchetoeu.jscript.utils.modules.RootModuleRepo;
import me.topchetoeu.jscript.utils.permissions.PermissionsManager;
import me.topchetoeu.jscript.utils.permissions.PermissionsProvider;
import me.topchetoeu.mcscript.lib.MCInternals;

public class ModManager {
    public final String codeFolder, dataFolder;
    public final List<Mod> mods = new ArrayList<>();
    public final DebugServer debugServer;

    private Filesystem zipFs(Path path) {
        var zipPath = new Path[] { path };

        return new Filesystem() {
            @Override public void close() {
                zipPath[0] = null;
            }
            @Override public File open(String path, Mode mode) {
                path = normalize(path).substring(1);

                try {
                    if (zipPath[0] == null) throw new FilesystemException(ErrorReason.CLOSED, "Filesystem closed.");

                    var file = new ZipFile(zipPath[0].toFile());
                    var entry = file.getEntry(path);

                    if (entry == null) {
                        file.close();
                        throw new FilesystemException(ErrorReason.DOESNT_EXIST);
                    }
                    if (mode.writable) {
                        file.close();
                        throw new FilesystemException(ErrorReason.NO_PERMISSION, "Zip filesystem is read-only.")
                            .setEntry(entry.isDirectory() ? EntryType.FOLDER : EntryType.FILE);
                    }

                    var res = File.ofStream(file.getInputStream(entry));

                    return new File() {
                        @Override public int read(byte[] buff) { return res.read(buff); }
                        @Override public long seek(long offset, int pos) { return res.seek(offset, pos); }
                        @Override public void write(byte[] buff) { res.write(buff); }
                        @Override public boolean close() {
                            if (res.close()) {
                                try { file.close(); }
                                catch (IOException e) { }
                                return true;
                            }

                            return false;
                        }
                    };
                }
                catch (IOException e) {
                    throw new FilesystemException(ErrorReason.UNKNOWN, e.getMessage()).setPath(path).setAction(ActionType.OPEN);
                }
                catch (FilesystemException e) {
                    throw e.setPath(path).setAction(ActionType.OPEN);
                }
            }
            @Override public FileStat stat(String path) {
                path = normalize(path);

                try {
                    if (zipPath[0] == null) throw new FilesystemException(ErrorReason.CLOSED, "Filesystem closed.");

                    var file = new ZipFile(zipPath[0].toFile());
                    var entry = file.getEntry(path);
                    file.close();

                    if (entry == null) return new FileStat(Mode.NONE, EntryType.NONE);
                    else if (entry.isDirectory()) return new FileStat(Mode.READ, EntryType.FOLDER);
                    else return new FileStat(Mode.READ, EntryType.FILE);
                }
                catch (IOException e) {
                    throw new FilesystemException(ErrorReason.UNKNOWN, e.getMessage()).setPath(path).setAction(ActionType.STAT);
                }
                catch (FilesystemException e) {
                    throw e.setPath(path).setAction(ActionType.STAT);
                }
            }
        };
    }

    private void loadFileMod(Filesystem codeFs) throws IOException {
        var file = codeFs.open("manifest.json", Mode.READ);
        var manifest = JSON.parse(new Filename("code", "manifest.json"), file.readToString()).map();
        file.close();

        var name = manifest.string("name");
        var version = manifest.string("version");
        var author = manifest.string("author");
        var main = manifest.string("main");

        file = codeFs.open(main, Mode.READ);
        var mainSrc = file.readToString();
        file.close();

        var env = new Environment();
        var loop = new Engine();

        Files.createDirectories(Path.of(dataFolder.toString(), name));

        var fs = new RootFilesystem(PermissionsProvider.get(env));
        fs.protocols.put("file", new PhysicalFilesystem(Path.of(dataFolder.toString(), name).toString()));
        fs.protocols.put("code", codeFs);

        var perms = new PermissionsManager();
        perms.add("jscript.file.read:**");
        perms.add("jscript.file.read:std://in");
        perms.add("jscript.file.write:std://out");
        perms.add("jscript.file.write:std://err");
        perms.add("jscript.file.write:file://**");

        var modules = new RootModuleRepo();
        modules.repos.put("file", ModuleRepo.ofFilesystem(fs.protocols.get("code")));

        var debug = new DebugContext();
        debugServer.targets.put(name, (socket, req) -> new SimpleDebugger(socket).attach(debug));

        Internals.apply(env);
        MCInternals.apply(env);
        env.add(EventLoop.KEY, loop);
        env.add(Filesystem.KEY, fs);
        env.add(PermissionsProvider.KEY, perms);
        env.add(Compiler.KEY, new JSCompiler(env));
        env.add(DebugContext.KEY, debug);

        new Thread(() -> {
            try {
                loop.pushMsg(false, env, new Filename("code", main), mainSrc, null).await();
            }
            catch (EngineException e) { Values.printError(e, "in mod initializer"); }

        }, "Awaiter").start();

        var mod = new Mod(name, author, version, loop, env, fs);
        mods.add(mod);
    }

    public void load() throws IOException {
        for (var file : Files.list(Path.of(codeFolder)).collect(Collectors.toList())) {
            if (Files.isDirectory(file)) {
                loadFileMod(new PhysicalFilesystem(file.toString()));
            }
            else {
                loadFileMod(zipFs(file));
            }
        }
    }
    public void setSTD(File in, File out, File err) {
        for (var mod : mods) {
            mod.fs.protocols.put("std", new STDFilesystem(in, out, err));
        }
    }
    public void start() {
        for (var mod : mods) {
            mod.loop.start();
        }
    }

    public ModManager(String folder, String dataFolder, DebugServer server) {
        this.codeFolder = folder;
        this.dataFolder = dataFolder;
        this.debugServer = server;
    }
}