package me.topchetoeu.mcscript;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;

public class Main implements ModInitializer {
    private void smite(PlayerEntity player) {
        var world = player.getWorld();
        var bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        bolt.setPosition(player.getPos());
        world.spawnEntity(bolt);
    }
    private void feed(PlayerEntity player) {
        player.getHungerManager().setSaturationLevel(20);
        player.getHungerManager().setFoodLevel(20);
    }

    @Override public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                literal("smite").then(argument("player", EntityArgumentType.players())).executes(context -> {
                    var selector = context.getArgument("player", EntitySelector.class);

                    for (var player : selector.getPlayers(context.getSource())) {
                        smite(player);
                    }

                    return 1;
                })
            );
            dispatcher.register(
                literal("feed").then(argument("player", EntityArgumentType.players())).executes(context -> {
                    var selector = context.getArgument("player", EntitySelector.class);

                    for (var player : selector.getPlayers(context.getSource())) {
                        feed(player);
                    }

                    return 1;
                })
            );
        });
    }
}



