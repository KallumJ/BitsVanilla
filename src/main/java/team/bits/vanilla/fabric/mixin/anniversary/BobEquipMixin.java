package team.bits.vanilla.fabric.mixin.anniversary;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.challenges.ChallengeRewardItems;
import team.bits.vanilla.fabric.database.PlayerApiUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(LivingEntity.class)
public abstract class BobEquipMixin {
    private static final List<String> EXCLAMATIONS = List.of(
            "HEY! TAKE ME OFF!",
            "GET OUT OF HERE!",
            "THIS IS MY HEAD IT'S PRIVATE!",
            "I'M GONNA TELL MY MASTER ON YOU! TAKE ME OFF!"
    );

    @Inject(
            method = "onEquipStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;playEquipSound(Lnet/minecraft/item/ItemStack;)V"
            )
    )
    public void equip(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (ChallengeRewardItems.isPortableBob(newStack) && entity instanceof ServerPlayerEntity player) {

            Text message = Text.empty()
                    .append(
                            Text.literal("<Bob> ")
                    )
                    .append(
                            chooseRandomExclamation(player)
                    );
            player.sendMessage(message);
        }
    }

    private Text chooseRandomExclamation(ServerPlayerEntity player) {
        int selection = ThreadLocalRandom.current().nextInt(EXCLAMATIONS.size() + 1);

        MutableText chosenText;
        try {
            chosenText = Text.literal(EXCLAMATIONS.get(selection)).styled(style -> style.withColor(Formatting.RED));
        } catch (IndexOutOfBoundsException ex) {
            chosenText = Text.empty()
                    .append(
                            Text.literal("fhdbghj").styled(style -> style.withObfuscated(true))
                    )
                    .append(
                            Text.literal(PlayerApiUtils.getEffectiveName(player))
                    )
                    .append(
                            Text.literal("bhdfghj").styled(style -> style.withObfuscated(true))
                    )
                    .append(
                            Text.literal("warning you")
                    )
                    .append(
                            Text.literal("jbdfsj").styled(style -> style.withObfuscated(true))
                    );
        }

        return chosenText.styled(style -> style.withColor(Formatting.RED));
    }
}
