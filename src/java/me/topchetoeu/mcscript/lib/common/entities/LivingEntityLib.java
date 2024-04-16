package me.topchetoeu.mcscript.lib.common.entities;

import me.topchetoeu.jscript.utils.interop.Arguments;
import me.topchetoeu.jscript.utils.interop.Expose;
import me.topchetoeu.jscript.utils.interop.ExposeType;
import me.topchetoeu.jscript.utils.interop.WrapperName;
import me.topchetoeu.mcscript.lib.server.ServerLib;
import net.minecraft.entity.LivingEntity;

@WrapperName("LivingEntity")
public class LivingEntityLib {
    @Expose(type = ExposeType.GETTER)
    public static double __health(Arguments args) {
        return args.self(LivingEntity.class).getHealth();
    }
    @Expose(type = ExposeType.SETTER, value = "health")
    public static void __setHealth(Arguments args) {
        var self = args.self(LivingEntity.class);
        ServerLib.runSync(self.getServer(), () -> {
            self.setHealth(args.getFloat(0));
        });
    }
    @Expose(type = ExposeType.GETTER)
    public static double __maxHealth(Arguments args) {
        return args.self(LivingEntity.class).getMaxHealth();
    }
}
