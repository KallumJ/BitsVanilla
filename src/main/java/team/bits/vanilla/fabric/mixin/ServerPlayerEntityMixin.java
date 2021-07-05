package team.bits.vanilla.fabric.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.database.player.PlayerDataHandle;
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

    @Overwrite
    public @Nullable Text getPlayerListName() {
        ServerPlayerEntity self = ServerPlayerEntity.class.cast(this);
        return PlayerDataHandle.get(self).getFormattedNickname();
    }
}
