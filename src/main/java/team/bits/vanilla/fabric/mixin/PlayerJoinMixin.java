package team.bits.vanilla.fabric.mixin;

import net.minecraft.server.*;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(PlayerManager.class)
public class PlayerJoinMixin {

    @Redirect(
            method = "onPlayerConnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"
            )
    )
    public void customBroadcastChatMessage(PlayerManager instance, Text message, boolean bl) {
        // we handle join messages in Velocity, so we want to drop this broadcast
    }
}
