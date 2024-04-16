package me.topchetoeu.mcscript.lib.server.inventory;

import java.util.Iterator;

import me.topchetoeu.jscript.runtime.values.ObjectValue;
import me.topchetoeu.jscript.runtime.values.Values;
import me.topchetoeu.jscript.utils.interop.Arguments;
import me.topchetoeu.jscript.utils.interop.Expose;
import me.topchetoeu.jscript.utils.interop.ExposeType;
import me.topchetoeu.jscript.utils.interop.WrapperName;
import me.topchetoeu.mcscript.core.Data;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

@WrapperName("Inventory")
public class InventoryLib {
    @Expose(type = ExposeType.GETTER)
    public static int __size(Arguments args) {
        var self = args.self(Inventory.class);
        return self.size();
    }

    @Expose public static ObjectValue __get(Arguments args) {
        var self = args.self(Inventory.class);
        var i = args.getInt(0);
        return Data.toJS(args, Data.toNBT(self.getStack(i)));
    }
    @Expose public static void __set(Arguments args) {
        var self = args.self(Inventory.class);

        var i = args.getInt(0);

        var item = Data.toItemStack((NbtCompound)Data.toNBT(args, 1));
        if (item != null && item.getItem() != Items.AIR) {
            self.setStack(i, item);
            return;
        }

        self.removeStack(i);
    }
    @Expose public static void __clear(Arguments args) {
        var self = args.self(Inventory.class);
        self.clear();
    }

    @Expose public static Inventory __clone(Arguments args) {
        var self = args.self(Inventory.class);
        var res = new SimpleInventory(self.size());

        for (var i = 0; i < res.size(); i++) {
            res.setStack(i, self.getStack(i));
        }

        return res;
    }

    @Expose public static void __copyFrom(Arguments args) {
        var self = args.self(Inventory.class);
        var from = args.convert(0, Inventory.class);

        for (var i = 0; i < self.size() && i < from.size(); i++) {
            self.setStack(i, from.getStack(i));
        }
    }

    @Expose("@@Symbol.iterator")
    public static ObjectValue __iterator(Arguments args) {
        var self = args.self(Inventory.class);

        return Values.toJSIterator(args, new Iterator<>() {
            private int i = 0;

            @Override public boolean hasNext() {
                while (i < self.size()) {
                    var item = self.getStack(i);
                    if (item != null && item.getItem() != Items.AIR) return true;
                    i++;
                }

                return false;
            }
            @Override public Object next() {
                if (i >= self.size()) return null;

                while (i < self.size()) {
                    var item = self.getStack(i++);
                    if (item != null && item.getItem() != Items.AIR) return Data.toJS(args, Data.toNBT(item));
                }

                return null;
            }
        });
    }
}
