package me.topchetoeu.mcscript.lib.server;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;

import me.topchetoeu.jscript.common.ResultRunnable;
import me.topchetoeu.jscript.common.events.DataNotifier;
import me.topchetoeu.jscript.runtime.Context;
import me.topchetoeu.jscript.runtime.EventLoop;
import me.topchetoeu.jscript.runtime.exceptions.EngineException;
import me.topchetoeu.jscript.runtime.values.ArrayValue;
import me.topchetoeu.jscript.runtime.values.NativeFunction;
import me.topchetoeu.jscript.runtime.values.ObjectValue;
import me.topchetoeu.jscript.runtime.values.Values;
import me.topchetoeu.jscript.utils.interop.Arguments;
import me.topchetoeu.jscript.utils.interop.Expose;
import me.topchetoeu.jscript.utils.interop.ExposeField;
import me.topchetoeu.jscript.utils.interop.ExposeTarget;
import me.topchetoeu.jscript.utils.interop.ExposeType;
import me.topchetoeu.jscript.utils.interop.WrapperName;
import me.topchetoeu.mcscript.MessageQueue;
import me.topchetoeu.mcscript.core.Data;
import me.topchetoeu.mcscript.events.PlayerBlockPlaceEvent;
import me.topchetoeu.mcscript.events.ScreenHandlerEvents;
import me.topchetoeu.mcscript.lib.utils.EventLib;
import me.topchetoeu.mcscript.lib.utils.LocationLib;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

@WrapperName("Server")
@SuppressWarnings("resource") // for crying outloud
public class ServerLib {
    public static class EventCtx {
        @ExposeField public final EventLib blockPlace;
        @ExposeField public final EventLib blockBreak;

        @ExposeField public final EventLib tickStart;
        @ExposeField public final EventLib tickEnd;

        @ExposeField public final EventLib playerJoin;
        @ExposeField public final EventLib playerLeave;
        @ExposeField public final EventLib playerChangeWorld;

        @ExposeField public final EventLib entityDamage;
        @ExposeField public final EventLib entityUse;

        @ExposeField public final EventLib itemUse;

        @ExposeField public final EventLib inventoryScreenClicked;
        @ExposeField public final EventLib inventoryScreenClosed;

        public EventCtx(Thread thread) {
            blockPlace = new EventLib(thread);
            blockBreak = new EventLib(thread);
            tickStart = new EventLib(thread);
            tickEnd = new EventLib(thread);
            playerJoin = new EventLib(thread);
            playerLeave = new EventLib(thread);
            playerChangeWorld = new EventLib(thread);
            entityDamage = new EventLib(thread);
            entityUse = new EventLib(thread);
            itemUse = new EventLib(thread);
            inventoryScreenClicked = new EventLib(thread);
            inventoryScreenClosed = new EventLib(thread);
        }
    }

    private static final WeakHashMap<MinecraftServer, EventCtx> ctxs = new WeakHashMap<>();

    public static EventCtx events(MinecraftServer server) {
        ctxs.putIfAbsent(server, new EventCtx(server.getThread()));
        return ctxs.get(server);
    }
    public static MessageQueue queue(MinecraftServer server) {
        return MessageQueue.get(server.getThread());
    }
    public static MinecraftServer get(Entity obj) {
        return obj.getServer();
    }

    public static <T> T runSync(MinecraftServer server, ResultRunnable<T> runnable) {
        var res = new DataNotifier<T>();

        queue(server).enqueueSync(() -> {
            try { res.next(runnable.run()); }
            catch (RuntimeException e) { res.error(e); }
        });

        return res.await();
    }
    public static void runSync(MinecraftServer server, Runnable runnable) {
        runSync(server, () -> {
            runnable.run();
            return null;
        });
    }

    @Expose(type = ExposeType.GETTER)
    public static EventLib __blockBreak(Arguments args) {
        return events(args.self(MinecraftServer.class)).blockBreak;
    }
    @Expose(type = ExposeType.GETTER)
    public static EventLib __blockPlace(Arguments args) {
        return events(args.self(MinecraftServer.class)).blockPlace;
    }
    @Expose(type = ExposeType.GETTER)
    public static EventLib __tickStart(Arguments args) {
        return events(args.self(MinecraftServer.class)).tickStart;
    }
    @Expose(type = ExposeType.GETTER)
    public static EventLib __tickEnd(Arguments args) {
        return events(args.self(MinecraftServer.class)).tickEnd;
    }
    @Expose(type = ExposeType.GETTER)
    public static EventLib __playerJoin(Arguments args) {
        return events(args.self(MinecraftServer.class)).playerJoin;
    }
    @Expose(type = ExposeType.GETTER)
    public static EventLib __playerLeave(Arguments args) {
        return events(args.self(MinecraftServer.class)).playerLeave;
    }
    @Expose(type = ExposeType.GETTER)
    public static EventLib __playerChangeWorld(Arguments args) {
        return events(args.self(MinecraftServer.class)).playerChangeWorld;
    }

    @Expose(type = ExposeType.GETTER)
    public static EventLib __entityDamage(Arguments args) {
        return events(args.self(MinecraftServer.class)).entityDamage;
    }
    @Expose(type = ExposeType.GETTER)
    public static EventLib __entityUse(Arguments args) {
        return events(args.self(MinecraftServer.class)).entityUse;
    }

    @Expose(type = ExposeType.GETTER)
    public static EventLib __itemUse(Arguments args) {
        return events(args.self(MinecraftServer.class)).itemUse;
    }

    @Expose(type = ExposeType.GETTER)
    public static EventLib __inventoryScreenClicked(Arguments args) {
        return events(args.self(MinecraftServer.class)).inventoryScreenClicked;
    }
    @Expose(type = ExposeType.GETTER)
    public static EventLib __inventoryScreenClosed(Arguments args) {
        return events(args.self(MinecraftServer.class)).inventoryScreenClosed;
    }

    @Expose(type = ExposeType.GETTER)
    public static ArrayValue __worlds(Arguments args) {
        var server = args.self(MinecraftServer.class);
        var res = new ArrayValue();

        for (var world : server.getWorlds()) {
            res.set(args.ctx, res.size(), world);
        }

        return res;
    }
    @Expose(type = ExposeType.GETTER)
    public static ArrayValue __players(Arguments args) {
        var server = args.self(MinecraftServer.class);
        var res = new ArrayValue();

        for (var player : server.getPlayerManager().getPlayerList()) {
            res.set(args.ctx, res.size(), player);
        }

        return res;
    }

    @Expose public static void __registerCommand(Arguments args) {
        var server = args.self(MinecraftServer.class);
        var name = args.convert(0, String.class);
        var obj = args.convert(1, ObjectValue.class);

        var exec = (Command<ServerCommandSource>)(context) -> {
            return queue(server).await(EventLoop.get(args.ctx).pushMsg(() -> {
                try {
                    String cmdArgs;
                    var ctx = Context.clean(args.ctx);

                    try {
                        cmdArgs = context.getArgument("args", String.class);
                    }
                    catch (IllegalArgumentException e) {
                        cmdArgs = "";
                    }

                    var res = Values.call(
                        ctx, Values.getMember(ctx, obj, "execute"), obj,
                        cmdArgs,
                        context.getSource().getEntity(),
                        new NativeFunction(_args -> {
                            context.getSource().sendMessage(Text.of(_args.convert(0, String.class)));
                            return null;
                        }),
                        new NativeFunction(_args -> {
                            context.getSource().sendError(Text.of(_args.convert(0, String.class)));
                            return null;
                        })
                    );
                    return (int)Values.toNumber(ctx, res);
                }
                catch (EngineException e) {
                    context.getSource().sendError(Text.of(Values.errorToReadable(e, "")));
                    return 0;
                }
            }, false));
        };

        server.getCommandManager().getDispatcher().register(literal(name).executes(exec).then(
            argument("args", StringArgumentType.greedyString()).executes(exec)
        ));
    }
    @Expose public static void __sendMessage(Arguments args) {
        var server = args.self(MinecraftServer.class);
        var text = args.convert(0, String.class);

        server.sendMessage(Text.of(text));
    }

    @Expose public static Entity __getByUUID(Arguments args) {
        var server = args.self(MinecraftServer.class);
        var uuid = args.getString(0);

        for (var world : server.getWorlds()) {
            var entity = world.getEntity(UUID.fromString(uuid));
            if (entity != null) return entity;
        }

        return null;
    }

    @Expose public static ObjectValue __cmd(Arguments args) {
        var server = args.self(MinecraftServer.class);
        var cmd = args.getString(0);
        var opts = (ObjectValue)args.getOrDefault(1, new ObjectValue());

        var loc = Values.wrapper(Values.getMember(args, opts, "at"), LocationLib.class);
        var pitch = Values.toNumber(args.ctx, Values.getMember(args, opts, "pitch"));
        var yaw = Values.toNumber(args.ctx, Values.getMember(args, opts, "yaw"));
        var entity = Values.wrapper(Values.getMember(args, opts, "as"), Entity.class);
        var world = Values.wrapper(Values.getMember(args, opts, "world"), ServerWorld.class);

        if (loc == null) loc = new LocationLib(0, 0, 0);
        if (world == null) world = server.getWorlds().iterator().next();

        var output = new ArrayValue();

        var sender = new ServerCommandSource(
            new CommandOutput() {
                @Override public void sendMessage(Text text) {
                    output.set(args, output.size(), text.getString());
                }
                @Override public boolean shouldBroadcastConsoleToOps() { return false; }
                @Override public boolean shouldReceiveFeedback() { return true; }
                @Override public boolean shouldTrackOutput() { return false; }
            },
            new Vec3d(loc.x, loc.y, loc.z), new Vec2f((float)pitch, (float)yaw),
            world, 4, "js", Text.of("js"),
            server, entity
        );

        var code = server.getCommandManager().executeWithPrefix(sender, cmd);

        return new ObjectValue(args, Map.of(
            "output", output,
            "code", code
        ));
    }

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            events(server).tickStart.invoke();
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            events(server).tickEnd.invoke();
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            events(server).playerJoin.invoke(handler.player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            events(server).playerLeave.invoke(handler.player);
        });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            events(player.server).playerChangeWorld.invoke(player, origin, destination);
        });
        PlayerBlockPlaceEvent.EVENT.register((player, pos, state) -> {
            return events(player.getServer()).blockPlace.invokeCancellable(
                LocationLib.of(pos), player, player.getWorld()
            );
        });
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld)) return true;

            return events(world.getServer()).blockBreak.invokeCancellable(
                LocationLib.of(pos), player, world
            );
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, damageSource, damageAmount) -> {
            var dmgSrc = new ObjectValue();

            dmgSrc.defineProperty(null, "damager", damageSource.getAttacker());
            dmgSrc.defineProperty(null, "location", LocationLib.of(damageSource.getPosition()));
            dmgSrc.defineProperty(null, "type", damageSource.getType().msgId());

            return events(entity.getServer()).entityDamage.invokeCancellable(entity, damageAmount, dmgSrc);
        });
        ScreenHandlerEvents.SCREEN_CLOSE.register((handler, player) -> {
            return events(player.getServer()).inventoryScreenClosed.invokeCancellable(
                handler, player
            );
        });
        ScreenHandlerEvents.SLOT_CLICKED.register((handler, slotIndex, rawButton, rawActionType, player) -> {
            var actType = "";

            switch (rawActionType) {
                case CLONE: actType = "clone"; break;
                case PICKUP: actType = "pickup"; break;
                case PICKUP_ALL: actType = "pickupAll"; break;
                case QUICK_CRAFT: actType = "quickCraft"; break;
                case QUICK_MOVE: actType = "quickMove"; break;
                case SWAP: actType = "swap"; break;
                case THROW: actType = "throw"; break;
            }

            return events(player.getServer()).inventoryScreenClicked.invokeCancellable(
                handler, player, slotIndex < 0 ? null : slotIndex, rawButton, actType
            );
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!events(player.getServer()).entityUse.invokeCancellable(
                player, entity
            )) return ActionResult.FAIL;

            else return ActionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!events(player.getServer()).itemUse.invokeCancellable(
                Data.toJS(null, Data.toNBT(player.getStackInHand(hand))),
                player, hand == Hand.MAIN_HAND ? "main" : "off"
            )) return TypedActionResult.fail(player.getStackInHand(hand));
            else return TypedActionResult.pass(player.getStackInHand(hand));
        });
    }

    @Expose(target = ExposeTarget.STATIC)
    public static int __maxStack(Arguments args) {
        var item = Registries.ITEM.get(new Identifier(args.getString(0)));

        if (item == null) return 0;
        else return item.getMaxCount();
    }
}
