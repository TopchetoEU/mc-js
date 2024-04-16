package me.topchetoeu.mcscript.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public interface PlayerBlockPlaceEvent {
    boolean blockPlaced(ServerPlayerEntity player, BlockPos pos, BlockState state);

    public static final Event<PlayerBlockPlaceEvent> EVENT = EventFactory.createArrayBacked(PlayerBlockPlaceEvent.class, arr -> (player, pos, state) -> {
        for (var el : arr) {
            if (!el.blockPlaced(player, pos, state)) return false;
        }

        return true;
    });
}
