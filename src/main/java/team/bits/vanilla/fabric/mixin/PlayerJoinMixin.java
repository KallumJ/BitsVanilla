package team.bits.vanilla.fabric.mixin;

import net.minecraft.network.message.*;
import net.minecraft.server.*;
import net.minecraft.text.*;
import net.minecraft.util.registry.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(PlayerManager.class)
public class PlayerJoinMixin {

    @Redirect(
            method = "onPlayerConnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Lnet/minecraft/util/registry/RegistryKey;)V"
            )
    )
    public void customBroadcastChatMessage(PlayerManager instance, Text message, RegistryKey<MessageType> registryKey) {
        // we handle join messages in Velocity, so we want to drop this broadcast
    }
}
