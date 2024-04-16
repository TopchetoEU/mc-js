package me.topchetoeu.mcscript.core;

import me.topchetoeu.jscript.runtime.Extensions;
import me.topchetoeu.jscript.runtime.values.ArrayValue;
import me.topchetoeu.jscript.runtime.values.ConvertHint;
import me.topchetoeu.jscript.runtime.values.ObjectValue;
import me.topchetoeu.jscript.runtime.values.Values;
import me.topchetoeu.jscript.utils.interop.Arguments;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

@SuppressWarnings("all")
public class Data {
    public static NbtCompound toNBT(Extensions ext, ObjectValue obj) {
        var res = new NbtCompound();

        for (var key : Values.getMembers(ext, obj, true, false)) {
            if (!(key instanceof String)) continue;
            var skey = (String)key;
            String propType = null;
            var i = skey.indexOf("$");

            if (i >= 0) {
                propType = skey.substring(i + 1);
                skey = skey.substring(0, i);
            }

            var val = toNBT(ext, Values.getMember(ext, obj, skey), propType);

            if (val != null) res.put(skey, val);
        }

        return res;
    }

    public static NbtList toNBT(Extensions ext, ArrayValue arr, String type) {
        var res = new NbtList();
        for (var el : arr) {
            var val = toNBT(ext, el, type);
            if (val != null) res.add(val);
        }
        return res;
    }
    public static NbtList toNBT(Extensions ext, ArrayValue arr) {
        return toNBT(ext, arr, null);
    }

    public static NbtLongArray toNBTLongArr(Extensions ext, ArrayValue arr) {
        var res = new long[arr.size()];
        for (var i = 0; i < arr.size(); i++) res[i] = (long)Values.toNumber(ext, arr.get(i));
        return new NbtLongArray(res);
    }
    public static NbtIntArray toNBTIntArr(Extensions ext, ArrayValue arr) {
        var res = new int[arr.size()];
        for (var i = 0; i < arr.size(); i++) res[i] = (int)Values.toNumber(ext, arr.get(i));
        return new NbtIntArray(res);
    }
    public static NbtByteArray toNBTByteArr(Extensions ext, ArrayValue arr) {
        var res = new byte[arr.size()];
        for (var i = 0; i < arr.size(); i++) res[i] = (byte)Values.toNumber(ext, arr.get(i));
        return new NbtByteArray(res);
    }

    public static NbtElement toNBT(Extensions ext, Object obj, String type) {
        if (type == null) type = "";

        if (obj instanceof ArrayValue) {
            var arr = (ArrayValue)obj;

            switch (type) {
                case "l": return toNBTLongArr(ext, arr);
                case "i": return toNBTIntArr(ext, arr);
                case "b": return toNBTByteArr(ext, arr);
                default: return toNBT(ext, arr);
            }
        }

        switch (type) {
            case "l": return NbtLong.of((long)Values.toNumber(ext, obj));
            case "i": return NbtInt.of((int)Values.toNumber(ext, obj));
            case "s": return NbtShort.of((short)Values.toNumber(ext, obj));
            case "b": return NbtByte.of((byte)Values.toNumber(ext, obj));
            case "f": return NbtFloat.of((float)Values.toNumber(ext, obj));
            case "d": return NbtDouble.of((double)Values.toNumber(ext, obj));
        }

        if (obj instanceof ObjectValue) return toNBT(ext, (ObjectValue)obj);

        var prim = Values.toPrimitive(ext, obj, ConvertHint.VALUEOF);

        if (prim instanceof Number) return NbtDouble.of(((Number)prim).doubleValue());
        if (prim instanceof Boolean) return NbtByte.of((boolean)prim);
        if (prim instanceof String) return NbtString.of((String)prim);

        return null;
    }
    public static NbtElement toNBT(Extensions ext, Object obj) {
        return toNBT(ext, obj, null);
    }

    public static NbtElement toNBT(Arguments args, int i) {
        return toNBT(args, args.get(i));
    }
    public static NbtCompound toCompound(Arguments args, int i) {
        var val = args.get(i);

        if (val instanceof ObjectValue) {
            return toNBT(args, (ObjectValue)val);
        }

        return new NbtCompound();
    }

    public static ObjectValue toJS(Extensions ext, NbtCompound cmp) {
        var res = new ObjectValue();

        for (var key : cmp.getKeys()) {
            var val = toJS(ext, cmp.get(key));
            if (val != null) res.defineProperty(ext, key, val);
        }

        return res;
    }

    public static Object toJS(Extensions ext, NbtElement obj) {
        if (obj instanceof AbstractNbtNumber) return ((AbstractNbtNumber)obj).doubleValue();
        if (obj instanceof NbtString) return ((NbtString)obj).asString();
        if (obj instanceof AbstractNbtList) {
            var arr = (AbstractNbtList<?>)obj;
            var res = new ArrayValue(arr.size());

            for (var el : arr) {
                var val = toJS(ext, el);
                if (val != null) res.set(ext, res.size(), val);
            }

            return res;
        }
        if (obj instanceof NbtCompound) return toJS(ext, (NbtCompound)obj);

        return null;
    }

    public static NbtCompound toNBT(BlockState state) {
        var res = new NbtCompound();

        res.put("id", NbtString.of(Registries.BLOCK.getId(state.getBlock()).toString()));

        for (var prop : state.getProperties()) {
            if (prop instanceof EnumProperty) res.put(prop.getName(), NbtString.of(state.get(prop).toString()));
            if (prop instanceof IntProperty) res.put(prop.getName(), NbtInt.of((int)state.get((IntProperty)prop)));
            if (prop instanceof BooleanProperty) res.put(prop.getName(), NbtByte.of((boolean)state.get((BooleanProperty)prop)));
        }

        return res;
    }
    public static BlockState toBlockState(NbtCompound cmp) {
        var block = Registries.BLOCK.get(new Identifier(cmp.getString("id")));
        var mgr = block.getStateManager();
        var state = mgr.getDefaultState();

        for (var key : cmp.getKeys()) {
            var prop = (Property<?>)mgr.getProperty(key);
            if (prop == null) continue;

            if (prop instanceof IntProperty) state = state.with((IntProperty)prop, cmp.getInt(key));
            if (prop instanceof EnumProperty<?>) state = state.with((Property)prop, (Comparable)prop.parse(cmp.getString(key)).get());
            if (prop instanceof BooleanProperty) state = state.with((BooleanProperty)prop, cmp.getBoolean(key));
        }

        return state;
    }

    public static NbtCompound toNBT(ItemStack stack) {
        var res = new NbtCompound();

        res.put("id", NbtString.of(Registries.ITEM.getId(stack.getItem()).toString()));
        res.put("count", NbtInt.of(stack.getCount()));

        if (stack.hasNbt()) res.copyFrom(stack.getNbt());

        return res;
    }
    public static ItemStack toItemStack(NbtCompound cmp) {
        if (cmp == null) return null;
        var item = Registries.ITEM.get(new Identifier(cmp.getString("id")));
        var stack = new ItemStack(item, cmp.getInt("count"));

        var nbt = cmp.copy();
        nbt.remove("id");
        nbt.remove("count");

        stack.setNbt(nbt);

        return stack;
    }
}
