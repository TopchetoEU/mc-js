package me.topchetoeu.mcscript;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.topchetoeu.jscript.utils.debug.DebugServer;
import me.topchetoeu.jscript.utils.filesystem.File;
import me.topchetoeu.mcscript.core.ModManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;

public class McScript implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("jscript");

    @Override public void onInitialize() {
        var server = new DebugServer();
        server.start(new InetSocketAddress(9229), true);
        var mods = new ModManager("mods", "mod-data", server);
        try {
            mods.load();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        mods.setSTD(null, File.ofLineWriter(LOGGER::info), File.ofLineWriter(LOGGER::info));
        mods.start();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                literal("smite").then(argument("player", EntityArgumentType.players()).executes(context -> {
                    var player = context.getArgument("player", EntitySelector.class);

                    for (var entity : player.getEntities(context.getSource())) {
                        var world = entity.getWorld();
                        var bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                        bolt.setPosition(entity.getPos());
                        world.spawnEntity(bolt);
                    }

                    return 1;
                }))
            );
        });
    }
}
