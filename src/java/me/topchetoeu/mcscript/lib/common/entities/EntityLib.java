package me.topchetoeu.mcscript.lib.common.entities;

import me.topchetoeu.jscript.runtime.values.ObjectValue;
import me.topchetoeu.jscript.utils.interop.Arguments;
import me.topchetoeu.jscript.utils.interop.Expose;
import me.topchetoeu.jscript.utils.interop.ExposeType;
import me.topchetoeu.jscript.utils.interop.WrapperName;
import me.topchetoeu.mcscript.core.Data;
import me.topchetoeu.mcscript.lib.server.ServerLib;
import me.topchetoeu.mcscript.lib.utils.LocationLib;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

@WrapperName("Entity")
public class EntityLib {
    @Expose(type = ExposeType.GETTER)
    public static World __world(Arguments args) {
        return args.self(Entity.class).getWorld();
    }

    @Expose(type = ExposeType.GETTER)
    public static String __name(Arguments args) {
        var self = args.self(Entity.class);
        return self.getDisplayName().getString();
    }
    @Expose(type = ExposeType.SETTER, value = "name")
    public static void __setName(Arguments args) {
        var self = args.self(Entity.class);
        ServerLib.runSync(self.getServer(), () -> {
            self.setCustomName(Text.of(args.getString(0)));
        });
    }

    @Expose(type = ExposeType.GETTER)
    public static LocationLib __location(Arguments args) {
        var self = args.self(Entity.class);
        return new LocationLib(self.getX(), self.getY(), self.getZ());
    }
    @Expose(type = ExposeType.SETTER, value = "location")
    public static void __setLocation(Arguments args) {
        var self = args.self(Entity.class);
        ServerLib.runSync(self.getServer(), () -> {
            var pos = new LocationLib(args);
            self.refreshPositionAfterTeleport(pos.x, pos.y, pos.z);
        });
    }

    @Expose(type = ExposeType.GETTER)
    public static String __uuid(Arguments args) {
        return args.self(ServerPlayerEntity.class).getUuidAsString();
    }

    @Expose public static void __clearName(Arguments args) {
        var self = args.self(Entity.class);
        ServerLib.runSync(self.getServer(), () -> {
            self.setCustomName(null);
        });
    }

    @Expose(type = ExposeType.GETTER)
    public static ObjectValue __nbt(Arguments args) {
        var self = args.self(Entity.class);

        return ServerLib.runSync(self.getServer(), () -> {
            var res = new NbtCompound();
            self.writeNbt(res);
            return Data.toJS(args, res);
        });
    }
    @Expose(type = ExposeType.SETTER, value = "nbt")
    public static void __setNbt(Arguments args) {
        var self = args.self(Entity.class);

        ServerLib.runSync(self.getServer(), () -> {
            var nbt = Data.toNBT(args, 0);
            self.readNbt((NbtCompound)nbt);
        });
    }

    @Expose public static void __discard(Arguments args) {
        var self = args.self(Entity.class);
        ServerLib.runSync(self.getServer(), () -> {
            self.discard();
        });
    }
}
