package me.topchetoeu.mcscript.lib.utils;

import me.topchetoeu.jscript.utils.interop.Arguments;
import me.topchetoeu.jscript.utils.interop.Expose;
import me.topchetoeu.jscript.utils.interop.ExposeConstructor;
import me.topchetoeu.jscript.utils.interop.ExposeType;
import me.topchetoeu.jscript.utils.interop.WrapperName;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3i;

@WrapperName("Location")
public class LocationLib {
    public final double x, y, z;

    @Expose(type = ExposeType.GETTER)
    public double __x() { return x; }
    @Expose(type = ExposeType.GETTER)
    public double __y() { return y; }
    @Expose(type = ExposeType.GETTER)
    public double __z() { return z; }

    @Expose public LocationLib __add(Arguments args) {
        var resX = x;
        var resY = y;
        var resZ = z;

        int i = 0;

        while (i < args.n()) {
            if (args.get(i) instanceof Number) {
                resX += args.convert(i, Double.class);
                resY += args.convert(i + 1, Double.class);
                resZ += args.convert(i + 2, Double.class);
                i += 3;
            }
            else {
                var val = args.convert(i, LocationLib.class);
                resX += val.x;
                resY += val.y;
                resZ += val.z;
                i++;
            }
        }

        return new LocationLib(resX, resY, resZ);
    }
    @Expose public LocationLib __subtract(Arguments args) {
        var resX = x;
        var resY = y;
        var resZ = z;

        int i = 0;

        while (i < args.n()) {
            if (args.get(i) instanceof Number) {
                resX -= args.convert(i, Double.class);
                resY -= args.convert(i + 1, Double.class);
                resZ -= args.convert(i + 2, Double.class);
                i += 3;
            }
            else {
                var val = args.convert(i, LocationLib.class);
                resX -= val.x;
                resY -= val.y;
                resZ -= val.z;
                i++;
            }
        }

        return new LocationLib(resX, resY, resZ);
    }
    @Expose public double __dot(Arguments args) {
        var resX = x;
        var resY = y;
        var resZ = z;

        if (args.get(0) instanceof Number) {
            resX *= args.convert(0, Double.class);
            resY *= args.convert(1, Double.class);
            resZ *= args.convert(2, Double.class);
        }
        else {
            var val = args.convert(0, LocationLib.class);
            resX *= val.x;
            resY *= val.y;
            resZ *= val.z;
        }

        return resX + resY + resZ;
    }
    @Expose public double __distance(Arguments args) {
        var resX = x;
        var resY = y;
        var resZ = z;

        if (args.get(0) instanceof Number) {
            resX -= args.convert(0, Double.class);
            resY -= args.convert(1, Double.class);
            resZ -= args.convert(2, Double.class);
        }
        else {
            var val = args.convert(0, LocationLib.class);
            resX -= val.x;
            resY -= val.y;
            resZ -= val.z;
        }

        return Math.sqrt(resX * resX + resY * resY + resZ * resZ);
    }
    @Expose public double __length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    @Expose public LocationLib __setX(Arguments args) {
        if (args.get(0) instanceof Number) return new LocationLib(args.convert(0, Double.class), y, z);
        else return new LocationLib(args.convert(0, LocationLib.class).x, y, z);
    }
    @Expose public LocationLib __setY(Arguments args) {
        if (args.get(0) instanceof Number) return new LocationLib(x, args.convert(0, Double.class), z);
        else return new LocationLib(x, args.convert(0, LocationLib.class).y, z);
    }
    @Expose public LocationLib __setZ(Arguments args) {
        if (args.get(0) instanceof Number) return new LocationLib(x, y, args.convert(0, Double.class));
        else return new LocationLib(x, y, args.convert(0, LocationLib.class).z);
    }

    @Expose public String __toString(Arguments args) {
        return "[%s %s %s]".formatted(x, y, z);
    }

    public LocationLib(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public LocationLib(Arguments args) {
        if (args.get(0) instanceof Number) {
            x = args.convert(0, Double.class);
            y = args.convert(1, Double.class);
            z = args.convert(2, Double.class);
        }
        else {
            var val = args.convert(0, LocationLib.class);
            x = val.x;
            y = val.y;
            z = val.z;
        }
    }

    @ExposeConstructor
    public static LocationLib __constructor(Arguments args) {
        return new LocationLib(args);
    }

    public static LocationLib of(Position vec) {
        if (vec == null) return null;
        return new LocationLib(vec.getX(), vec.getY(), vec.getZ());
    }
    public static LocationLib of(Vec3i vec) {
        if (vec == null) return null;
        return new LocationLib(vec.getX(), vec.getY(), vec.getZ());
    }
}
