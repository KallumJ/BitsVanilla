package team.bits.vanilla.fabric.mixin;

import net.minecraft.server.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ServerPlayNetworkHandler.class)
public class PlayerLeaveMixin {

    @Redirect(
            method = "onDisconnected",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"
            )
    )
    public void customBroadcastChatMessage(PlayerManager instance, Text message, boolean bl) {
        // we handle leave messages in Velocity, so we want to drop this broadcast
    }
}
