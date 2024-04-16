package me.topchetoeu.mcscript.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.topchetoeu.mcscript.events.PlayerBlockPlaceEvent;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private void onPlace(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cbi) {
        if (!(context.getPlayer() instanceof ServerPlayerEntity)) return;

        var cancelled = !PlayerBlockPlaceEvent.EVENT.invoker().blockPlaced((ServerPlayerEntity)context.getPlayer(), context.getBlockPos(), state);

        if (cancelled) cbi.setReturnValue(false);
    }
}
