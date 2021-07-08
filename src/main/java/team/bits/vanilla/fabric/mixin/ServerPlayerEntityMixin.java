package team.bits.vanilla.fabric.mixin;

import net.kyori.adventure.text.Component;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.commands.EndLockCommand;
import team.bits.vanilla.fabric.util.Colors;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(
            method = "copyFrom",
            at = @At("TAIL")
    )
    public void onPlayerCopy(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        ExtendedPlayerEntity eOldPlayer = (ExtendedPlayerEntity) oldPlayer;
        ExtendedPlayerEntity eNewPlayer = (ExtendedPlayerEntity) this;
        eNewPlayer.copyFromOldPlayer(eOldPlayer);
    }

    @Inject(at = @At("HEAD"), method="moveToWorld", cancellable = true)
    private void endLockCheck(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        if (EndLockCommand.isEndLocked() && destination.getRegistryKey().equals(World.END)) {
            ServerPlayerEntity player = ServerPlayerEntity.class.cast(this);

            BitsVanilla.audience(player).sendMessage(Component.text("The end is currently locked", Colors.NEGATIVE));

            player.teleport(player.getX() + 2, player.getY() + 1, player.getZ() + 2);
            cir.cancel();
        }
    }

    @Overwrite
    public @Nullable Text getPlayerListName() {
        ServerPlayerEntity self = ServerPlayerEntity.class.cast(this);
        return self.getCustomName();
    }
}
