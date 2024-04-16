package me.topchetoeu.mcscript.lib.server.world;

import me.topchetoeu.jscript.common.json.JSON;
import me.topchetoeu.jscript.runtime.exceptions.EngineException;
import me.topchetoeu.jscript.runtime.values.ArrayValue;
import me.topchetoeu.jscript.runtime.values.ObjectValue;
import me.topchetoeu.jscript.utils.interop.Arguments;
import me.topchetoeu.jscript.utils.interop.Expose;
import me.topchetoeu.jscript.utils.interop.ExposeType;
import me.topchetoeu.jscript.utils.interop.WrapperName;
import me.topchetoeu.mcscript.core.Data;
import me.topchetoeu.mcscript.lib.server.ServerLib;
import me.topchetoeu.mcscript.lib.utils.LocationLib;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@WrapperName("ServerWorld")
public class ServerWorldLib {
    @Expose(type = ExposeType.GETTER) public static ArrayValue __players(Arguments args) {
        var self = args.self(ServerWorld.class);
        var res = new ArrayValue();

        for (var player : self.getPlayers()) {
            res.set(args.ctx, res.size(), player);
        }

        return res;
    }

    @Expose public static ObjectValue __getBlock(Arguments args) {
        var self = args.self(ServerWorld.class);
        var loc = args.convert(0, LocationLib.class);
        var desc = new ObjectValue();

        ServerLib.runSync(self.getServer(), () -> {
            var state = self.getBlockState(new BlockPos((int)loc.x, (int)loc.y, (int)loc.z));

            desc.defineProperty(args, "id", Registries.BLOCK.getId(state.getBlock()).toString());

            for (var prop : state.getProperties()) {
                var raw = state.get(prop);
                if (raw instanceof Number || raw instanceof Boolean) desc.defineProperty(args, prop.getName(), raw);
                else desc.defineProperty(args, prop.getName(), raw.toString());
            }
        });

        return desc;
    }
    @SuppressWarnings("all")
    @Expose public static void __setBlock(Arguments args) {
        var self = args.self(ServerWorld.class);
        var loc = args.convert(0, LocationLib.class);
        var nbt = JSON.fromJs(args, (ObjectValue)args.getOrDefault(1, new ObjectValue())).map();
        var update = args.has(2) ? args.getBoolean(2) : true;
        var id = new Identifier(nbt.string("id"));

        ServerLib.runSync(self.getServer(), () -> {
            var block = Registries.BLOCK.get(id);
            if (block == null) throw EngineException.ofError("No block %s.".formatted(id));

            var stateMgr = block.getStateManager();
            var state = block.getDefaultState();

            for (var entry : nbt.entrySet()) {
                var prop = stateMgr.getProperty(entry.getKey());
                if (prop == null) continue;

                var val = prop.parse(entry.getValue().toString());
                if (val.isEmpty()) throw EngineException.ofError("Illegal value '%s' provided for property %s.".formatted(entry.getValue(), entry.getKey()));

                // I'm so done with java generics
                state = state.with((Property)prop, (Comparable)val.get());
            }

            var pos = new BlockPos((int)loc.x, (int)loc.y, (int)loc.z);

            self.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);

            if (update) {
                self.updateNeighbors(pos, state.getBlock());
            }
        });
    }

    @Expose public static Entity __summon(Arguments args) {
        var self = args.self(ServerWorld.class);
        var loc = args.convert(0, LocationLib.class);
        var nbt = Data.toCompound(args, 1);

        return ServerLib.runSync(self.getServer(), () -> {
            var entity = EntityType.loadEntityWithPassengers(nbt, self, val -> {
                // val.readNbt(nbt);
                val.refreshPositionAndAngles(loc.x, loc.y, loc.z, val.getYaw(), val.getPitch());
                return val;
            });

            if (entity == null) return null;
            if (!self.spawnNewEntityAndPassengers(entity)) return null;

            return entity;
        });
    }
}
