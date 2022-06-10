package team.bits.vanilla.fabric.mixin;

import net.minecraft.item.EnderEyeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.bits.nibbles.utils.MessageTypes;
import team.bits.vanilla.fabric.commands.EndLockCommand;

@Mixin(EnderEyeItem.class)
public class EndLockMixin {
    @Inject(
            method = "useOnBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"
            ),
            cancellable = true
    )
    public void preventEyeInstertion(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (EndLockCommand.isEndLocked()) {
            if (context.getPlayer() != null) {
                ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
                player.sendMessage(Text.literal("The end is currently locked"), MessageTypes.NEGATIVE);
                cir.setReturnValue(ActionResult.PASS);
            }
        }
    }
}
