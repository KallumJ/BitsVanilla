package team.bits.vanilla.fabric.mixin.anniversary;

import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.apache.commons.io.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.*;
import team.bits.vanilla.fabric.challenges.*;
import team.bits.vanilla.fabric.util.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

@Mixin(BlockItem.class)
public class BobPlaceMixin {
    private static List<String> sentences;

    static {
        try {
            ClassLoader classLoader = BitsVanilla.class.getClassLoader();
            String sentencesFileContent = IOUtils.resourceToString("sentences.txt", Charset.defaultCharset(), classLoader);
            sentences = Arrays.stream(sentencesFileContent.split("\n")).collect(Collectors.toList());
        } catch (IOException | NullPointerException e) {
            BitsVanilla.LOGGER.error("Error while loading sentences.txt", e);
            sentences = List.of("Oops! My brain is malfunctioning... Please report this to my masters!");
        }
    }


    @Inject(
            method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;emitGameEvent(Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/event/GameEvent$Emitter;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void placeBlock(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir,
                           ItemPlacementContext _context, BlockState blockState,
                           BlockPos blockPos, World world, PlayerEntity player, ItemStack itemStack) {
        ItemStack stack = itemStack.copy();

        if (ChallengeRewardItems.isPortableBob(stack)) {
            // Randomly select and send sentence
            int index = ThreadLocalRandom.current().nextInt(sentences.size());
            player.sendMessage(Text.literal("<Bob> " + sentences.get(index).trim()));

            // Remove bob from world
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState());

            // Give bob back after a moment to prevent client not realising they have the item
            Scheduler.schedule(() -> {
                // Set count to 1 to prevent duplicating in the event someone has multiple in their hand when they place it
                stack.setCount(1);
                ((ExtendedPlayerEntity) player).giveItem(stack);
            }, 1);

        }
    }
}
