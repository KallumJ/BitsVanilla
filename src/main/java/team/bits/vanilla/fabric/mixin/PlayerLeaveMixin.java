package team.bits.vanilla.fabric.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.event.misc.PlayerDisconnectEvent;

import java.util.UUID;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class PlayerLeaveMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract ServerPlayerEntity getPlayer();

    @Shadow
    public abstract ClientConnection getConnection();

    @Inject(at = @At("HEAD"), method = "onDisconnected")
    public void onDisconnect(Text reason, CallbackInfo ci) {
        PlayerDisconnectEvent.EVENT.invoker().onPlayerDisconnect(this.getPlayer(), this.getConnection());
    }

    @Redirect(
            method = "onDisconnected",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"
            )
    )
    public void customBroadcastChatMessage(PlayerManager playerManager, Text message, MessageType type, UUID sender) {
        // we handle leave messages in Velocity, so we want to drop this broadcast
    }
}
