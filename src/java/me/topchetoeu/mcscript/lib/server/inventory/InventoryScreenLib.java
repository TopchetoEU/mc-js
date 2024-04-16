package me.topchetoeu.mcscript.lib.server.inventory;

import java.util.HashSet;

import me.topchetoeu.jscript.runtime.values.ArrayValue;
import me.topchetoeu.jscript.utils.interop.Arguments;
import me.topchetoeu.jscript.utils.interop.Expose;
import me.topchetoeu.jscript.utils.interop.WrapperName;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;

// I couldn't have come up with a shittier way to do inventories if my life depended on it
// Bravo, Mojang, Bravo

@WrapperName("InventoryScreen")
public class InventoryScreenLib {
    @Expose public static int __id(Arguments args) {
        return args.self(ScreenHandler.class).syncId;
    }
    @Expose public static ArrayValue __inventories(Arguments args) {
        var tmp = new HashSet<Inventory>();
        var res = new ArrayValue();

        for (var slot : args.self(ScreenHandler.class).slots) {
            var inv = slot.inventory;
            if (tmp.add(inv)) res.set(args, res.size(), inv);
        }

        return res;
    }
}
