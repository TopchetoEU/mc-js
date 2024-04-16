package me.topchetoeu.mcscript.lib.server.entities;

import me.topchetoeu.jscript.common.json.JSON;
import me.topchetoeu.jscript.runtime.exceptions.EngineException;
import me.topchetoeu.jscript.runtime.values.ObjectValue;
import me.topchetoeu.jscript.utils.interop.Arguments;
import me.topchetoeu.jscript.utils.interop.Expose;
import me.topchetoeu.jscript.utils.interop.ExposeType;
import me.topchetoeu.jscript.utils.interop.WrapperName;
import me.topchetoeu.mcscript.lib.server.ServerLib;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

@WrapperName("ServerPlayer")
public class ServerPlayerLib {
    @Expose(type = ExposeType.GETTER)
    public static String __gamemode(Arguments args) {
        switch (args.self(ServerPlayerEntity.class).interactionManager.getGameMode()) {
            case SURVIVAL: return "survival";
            case CREATIVE: return "creative";
            case ADVENTURE: return "adventure";
            case SPECTATOR: return "spectator";
            default: return "unknonw";
        }
    }
    @Expose(type = ExposeType.SETTER, value = "gamemode")
    public static void __setGamemode(Arguments args) {
        var self = args.self(ServerPlayerEntity.class);
        var gm = args.getString(0);

        ServerLib.runSync(self.getServer(), () -> {
            // System.out.println("Set %s's gamemode to %s".formatted(self.getName().getString(), gm.toString()));
            switch (gm) {
                case "survival": self.changeGameMode(GameMode.SURVIVAL); break;
                case "creative": self.changeGameMode(GameMode.CREATIVE); break;
                case "adventure": self.changeGameMode(GameMode.ADVENTURE); break;
                case "spectator": self.changeGameMode(GameMode.SPECTATOR); break;
                default: throw EngineException.ofError("Invalid gamemode '%s'.".formatted(gm));
            }
        });
    }

    @Expose(type = ExposeType.GETTER)
    public static double __foodLevel(Arguments args) {
        return args.self(ServerPlayerEntity.class).getHungerManager().getFoodLevel();
    }
    @Expose(type = ExposeType.SETTER, value = "foodLevel")
    public static void __setFoodLevel(Arguments args) {
        var self = args.self(ServerPlayerEntity.class);

        ServerLib.runSync(self.getServer(), () -> {
            args.self(ServerPlayerEntity.class).getHungerManager().setFoodLevel(args.getInt(0));
        });
    }

    @Expose(type = ExposeType.GETTER)
    public static double __saturation(Arguments args) {
        return args.self(ServerPlayerEntity.class).getHungerManager().getSaturationLevel();
    }
    @Expose(type = ExposeType.SETTER, value = "saturation")
    public static void __setSaturation(Arguments args) {
        var self = args.self(ServerPlayerEntity.class);

        self.getInventory();

        ServerLib.runSync(self.getServer(), () -> {
            args.self(ServerPlayerEntity.class).getHungerManager().setSaturationLevel(args.getFloat(0));
        });
    }

    @Expose public static void __sendTitle(Arguments args) {
        var self = args.self(ServerPlayerEntity.class);
        var conf = JSON.fromJs(args.ctx, args.convert(0, ObjectValue.class)).map();

        var title = conf.string("title", "");
        var subtitle = conf.string("subtitle", "");
        var fadeIn = conf.number("fadeIn", 0);
        var fadeOut = conf.number("fadeOut", 0);
        var duration = conf.number("duration", 1);

        self.networkHandler.sendPacket(new TitleFadeS2CPacket((int)(fadeIn * 20), (int)(duration * 20), (int)(fadeOut * 20)));
        self.networkHandler.sendPacket(new SubtitleS2CPacket(Text.of(subtitle)));
        self.networkHandler.sendPacket(new TitleS2CPacket(Text.of(title)));
    }

    @Expose(type = ExposeType.GETTER)
    public static Inventory __inventory(Arguments args) {
        var self = args.self(ServerPlayerEntity.class);
        return self.getInventory();
    }
    @Expose(type = ExposeType.GETTER)
    public static ScreenHandler __screen(Arguments args) {
        var self = args.self(ServerPlayerEntity.class);

        return self.currentScreenHandler;
    }

    @Expose public static ScreenHandler __openInventory(Arguments args) {
        var self = args.self(ServerPlayerEntity.class);
        var name = args.getString(0);
        var type = args.getString(1);
        var inv = args.convert(2, Inventory.class);
        ScreenHandlerFactory factory;

        switch (type) {
            case "3x3":
                factory = (i, pinv, player) -> new Generic3x3ContainerScreenHandler(i, pinv, inv);
                break;
            case "9x1":
                factory = (i, pinv, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X1, i, pinv, inv, 1);
                break;
            case "9x2":
                factory = (i, pinv, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X2, i, pinv, inv, 2);
                break;
            case "9x3":
                factory = (i, pinv, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, i, pinv, inv, 3);
                break;
            case "9x4":
                factory = (i, pinv, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X4, i, pinv, inv, 4);
                break;
            case "9x5":
                factory = (i, pinv, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, i, pinv, inv, 5);
                break;
            case "9x6":
            default:
                factory = (i, pinv, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, i, pinv, inv, 6);
                break;
        }

        self.openHandledScreen(new SimpleNamedScreenHandlerFactory(factory, Text.of(name)));
        return self.currentScreenHandler;
    }
    @Expose public static void __closeInventory(Arguments args) {
        var self = args.self(ServerPlayerEntity.class);

        ServerLib.runSync(self.getServer(), () -> {
            self.closeHandledScreen();
        });
    }
}
