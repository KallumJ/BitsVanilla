package team.bits.vanilla.fabric.mixin;

import net.minecraft.network.message.*;
import net.minecraft.server.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import team.bits.vanilla.fabric.chat.*;

import java.io.*;

@Mixin(MinecraftServer.class)
public class ChatDecorationMixin {

    private final BitsChatDecorator bitsChatDecorator = new BitsChatDecorator();

    @Inject(
            method = "getMessageDecorator",
            at = @At("RETURN"),
            cancellable = true
    )
    public void returnCustomChatDecorator(CallbackInfoReturnable<MessageDecorator> cir) {
        cir.setReturnValue(bitsChatDecorator);
    }

    @Inject(
            method = "shutdown",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;save(ZZZ)Z"
            )
    )
    public void shutdownCustomChatDecorator(CallbackInfo ci) {
        try {
            bitsChatDecorator.close();
        } catch (IOException ex) {
            throw new RuntimeException("Error while shutting down custom chat decorator", ex);
        }
    }
}
