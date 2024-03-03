package me.topchetoeu.mcscript;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.topchetoeu.jscript.utils.filesystem.File;
import me.topchetoeu.mcscript.core.ModManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class McScript implements ModInitializer, ClientModInitializer {
    // private boolean openDev = false;
    public static final Logger LOGGER = LoggerFactory.getLogger("jscript");

    // private void execute(int i, Environment env, Engine engine, String raw, MessageReceiver msg) {
    //     try {
    //         var res = engine.pushMsg(false, env, new Filename("repl", i + ".js"), raw, null).await();
    //         msg.sendInfo(Values.toReadable(new Context(env), res));
    //     }
    //     catch (RuntimeException e) {
    //         msg.sendError(Values.errorToReadable(e, ""));
    //     }
    // }

    // private Environment createEnv(String location, LineReader in, LineWriter out) {
    //     var env = new Environment();
    //     var inFile = File.ofLineReader(in);
    //     var outFile = File.ofLineWriter(out);
    //     Internals.apply(env);

    //     var fs = new RootFilesystem(PermissionsProvider.get(env));
    //     fs.protocols.put("std", new STDFilesystem().add("in", inFile).add("out", outFile));

    //     var perms = new PermissionsManager();

    //     env.add(PermissionsProvider.KEY, perms);
    //     env.add(Filesystem.KEY, fs);

    //     env.global.define(null, "env", true, location);
    //     return env;
    // }

    @Override public void onInitialize() {
        var mods = new ModManager("mods", "mod-data");
        try {
            mods.load();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // ServerLifecycleEvents.CLIENT_STARTED.register((client) -> {
            // var receiver = new ClientMessageReceiver();
            // mods.setSTD(null, File.ofLineWriter(receiver::sendInfo), File.ofLineWriter(receiver::sendError));
        mods.setSTD(null, File.ofLineWriter(LOGGER::info), File.ofLineWriter(LOGGER::info));
        mods.start();
        // });

        // var engine = new Engine();

        // ServerLifecycleEvents.SERVER_STARTING.register(server -> {
        //     engine.start();

        //     var env = createEnv("SERVER", () -> null, value -> {
        //         for (var line : value.split("\n", -1)) LOGGER.info(line);
        //     });

        //     var i = new int[1]; 
    
        //     server.getCommandManager().getDispatcher().register(CommandManager.literal("msc")
        //         .then(CommandManager.argument("code", greedyString()).executes(c -> {
        //             String str = getString(c, "code");
        //             var receiver = new ServerCommandMessageReceiver(c.getSource());
        //             execute(i[0]++, env, engine, str, receiver);
        //             return 1;
        //         }))
        //     );
        // });
        // ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
        //     engine.stop();
        // });
    }

    @net.fabricmc.api.Environment(EnvType.CLIENT)
    @Override public void onInitializeClient() {
        // var dev = new DevScreen();
        // var engine = new Engine();

        ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
            // var i = new int[1];
            // ChatMessageCallback.EVENT.register(args -> {
            //     if (args.message.startsWith("#")) {
            //         execute(i[0]++, env, engine, args.message.substring(1), receiver);
            //         args.cancelled = true;
            //     }
            // });
            // ClientTickEvents.END_CLIENT_TICK.register((_1) -> {
            //     if (openDev) {
            //         client.setScreenAndRender(dev);
            //         openDev = false;
            //     }
            // });
            // ClientCommandRegistrationCallback.EVENT.register((disp, _1) -> {
            //     disp.register(ClientCommandManager.literal("dev")
            //         .executes(c -> {
            //             openDev = true;
            //             return 1;
            //         })
            //     );
            // });
        });

        // ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> {
        //     engine.stop();
        // });
    }
}
