package team.bits.vanilla.fabric.mixin;

import net.minecraft.network.message.*;
import net.minecraft.server.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import net.minecraft.util.registry.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ServerPlayNetworkHandler.class)
public class PlayerLeaveMixin {

    @Redirect(
            method = "onDisconnected",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Lnet/minecraft/util/registry/RegistryKey;)V"
            )
    )
    public void customBroadcastChatMessage(PlayerManager instance, Text message, RegistryKey<MessageType> registryKey) {
        // we handle leave messages in Velocity, so we want to drop this broadcast
    }
}
