package team.bits.vanilla.fabric.mixin;

import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.freecam.Freecam;


@Mixin(ServerPlayNetworkHandler.class)
public class PreventFreecamTpMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(
            method="onSpectatorTeleport",
            at = @At("HEAD"),
            cancellable = true
    )
    public void preventTp(SpectatorTeleportC2SPacket packet, CallbackInfo ci) {
        if (Freecam.isPlayerInFreecam(player)) {
            player.sendMessage(Text.literal("You may not do that while in freecam").styled(style -> style.withColor(Colors.NEGATIVE)));
            ci.cancel();
        }
    }
}
