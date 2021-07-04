package team.bits.vanilla.fabric.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.event.misc.PlayerConnectEvent;

import java.util.UUID;

@Mixin(PlayerManager.class)
public class PlayerJoinMixin {

    @Inject(
            method = "onPlayerConnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;onPlayerConnected(Lnet/minecraft/server/network/ServerPlayerEntity;)V"
            )
    )
    public void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        PlayerConnectEvent.EVENT.invoker().onPlayerConnect(player, connection);
    }

    @Redirect(
            method = "onPlayerConnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"
            )
    )
    public void customBroadcastChatMessage(PlayerManager playerManager, Text message, MessageType type, UUID sender) {
        // we handle join messages in Velocity, so we want to drop this broadcast
    }
}
