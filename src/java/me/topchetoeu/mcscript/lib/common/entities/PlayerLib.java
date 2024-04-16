package me.topchetoeu.mcscript.lib.common.entities;

import me.topchetoeu.jscript.utils.interop.Arguments;
import me.topchetoeu.jscript.utils.interop.Expose;
import me.topchetoeu.jscript.utils.interop.WrapperName;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

@WrapperName("Player")
public class PlayerLib {
    @Expose public static void __sendMessage(Arguments args) {
        args.self(PlayerEntity.class).sendMessage(Text.of(args.getString(0)));
    }
}
