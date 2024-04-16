package me.topchetoeu.mcscript.lib;

import me.topchetoeu.jscript.runtime.Environment;
import me.topchetoeu.jscript.runtime.scope.GlobalScope;
import me.topchetoeu.jscript.runtime.values.NativeFunction;
import me.topchetoeu.jscript.utils.interop.ExposeField;
import me.topchetoeu.jscript.utils.interop.ExposeTarget;
import me.topchetoeu.jscript.utils.interop.NativeWrapperProvider;
import me.topchetoeu.mcscript.lib.common.entities.EntityLib;
import me.topchetoeu.mcscript.lib.common.entities.LivingEntityLib;
import me.topchetoeu.mcscript.lib.common.entities.PlayerLib;
import me.topchetoeu.mcscript.lib.server.ServerLib;
import me.topchetoeu.mcscript.lib.server.entities.ServerPlayerLib;
import me.topchetoeu.mcscript.lib.server.inventory.InventoryLib;
import me.topchetoeu.mcscript.lib.server.inventory.InventoryScreenLib;
import me.topchetoeu.mcscript.lib.server.world.ServerWorldLib;
import me.topchetoeu.mcscript.lib.utils.EventLib;
import me.topchetoeu.mcscript.lib.utils.LocationLib;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class MCInternals {
    @ExposeField(target = ExposeTarget.STATIC)
    public static final EventLib __serverLoad = new EventLib();

    static {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            __serverLoad.invoke(server);
        });
    }

    public static void apply(Environment env) {
        var wp = NativeWrapperProvider.get(env);
        var glob = new GlobalScope(wp.getNamespace(MCInternals.class));
        glob.obj.setPrototype(env, GlobalScope.get(env).obj);
        env.remove(GlobalScope.KEY);
        env.add(GlobalScope.KEY, glob);

        wp.set(Entity.class, EntityLib.class);
        wp.set(LivingEntity.class, LivingEntityLib.class);
        wp.set(PlayerEntity.class, PlayerLib.class); 
        wp.set(ServerPlayerEntity.class, ServerPlayerLib.class);
        wp.set(MinecraftServer.class, ServerLib.class);
        wp.set(ServerWorld.class, ServerWorldLib.class);
        wp.set(Inventory.class, InventoryLib.class);
        wp.set(ScreenHandler.class, InventoryScreenLib.class);

        glob.define(env, false, wp.getConstr(MinecraftServer.class));
        glob.define(env, false, wp.getConstr(LocationLib.class));
        glob.define(env, false, wp.getConstr(Entity.class));
        glob.define(env, false, wp.getConstr(LivingEntity.class));
        glob.define(env, false, wp.getConstr(PlayerEntity.class));
        glob.define(env, false, wp.getConstr(ServerPlayerEntity.class));
        glob.define(env, false, new NativeFunction("Inventory", args -> {
            return new SimpleInventory(args.getInt(0));
        }));
    }
}
