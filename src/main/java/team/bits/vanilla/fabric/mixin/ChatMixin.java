package team.bits.vanilla.fabric.mixin;

import net.minecraft.*;
import net.minecraft.network.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(ServerPlayNetworkHandler.class)
public class ChatMixin {

    @Inject(
            method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/class_7648;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onPacketSend(Packet<?> packet, class_7648 arg, CallbackInfo ci) {
        if (packet instanceof ChatMessageS2CPacket) {
            ci.cancel();
        }
    }
}
