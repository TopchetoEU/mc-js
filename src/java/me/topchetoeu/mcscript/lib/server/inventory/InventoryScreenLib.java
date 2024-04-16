package me.topchetoeu.mcscript.lib.server.inventory;

import java.util.HashSet;

import me.topchetoeu.jscript.runtime.values.ArrayValue;
import me.topchetoeu.jscript.runtime.values.NativeFunction;
import me.topchetoeu.jscript.runtime.values.ObjectValue;
import me.topchetoeu.jscript.utils.interop.Arguments;
import me.topchetoeu.jscript.utils.interop.Expose;
import me.topchetoeu.jscript.utils.interop.WrapperName;
import me.topchetoeu.mcscript.core.Data;
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
    @Expose public static ObjectValue __cursorItem(Arguments args) {
        return Data.toJS(args, Data.toNBT(args.self(ScreenHandler.class).getCursorStack()));
    }
    @Expose public static ObjectValue __getSlot(Arguments args) {
        var self = args.self(ScreenHandler.class);
        var i = args.getInt(0);

        var slot = self.getSlot(i);

        var res = new ObjectValue();
        res.defineProperty(args, "i", slot.getIndex());
        res.defineProperty(args, "x", slot.x);
        res.defineProperty(args, "y", slot.y);
        res.defineProperty(args, "inventory", slot.inventory);
        res.defineProperty(args, "item",
            new NativeFunction(_args -> {
                return Data.toJS(args, Data.toNBT(slot.getStack()));
            }),
            new NativeFunction(_args -> {
                var item = Data.toItemStack(Data.toCompound(_args, 0));
                slot.setStack(item);
                return null;
            }),
        true, true);

        return res;
    }
}
