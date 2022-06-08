package team.bits.vanilla.fabric.mixin.challenges;

import net.minecraft.item.*;
import net.minecraft.server.network.*;
import net.minecraft.sound.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import team.bits.vanilla.fabric.challenges.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;
import java.util.stream.*;

@Mixin(MusicDiscItem.class)
public class RemixMixin {

    private static final int MAX_DISC_RECORDS = 64;
    private static final int DISK_DURATION = (90 * 20);

    @Final
    @Shadow
    private static Map<SoundEvent, MusicDiscItem> MUSIC_DISCS;

    private static final LinkedList<DiscPlayRecord> playingDiscs = new LinkedList<>();

    @Inject(
            method = "useOnBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/JukeboxBlock;setRecord(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/item/ItemStack;)V"
            )
    )
    public void onDiscPlay(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        MusicDiscItem self = (MusicDiscItem) (Object) this;
        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player;
        long time = context.getWorld().getTime();

        if (player != null && !ePlayer.hasCompletedChallenge(Challenges.REMIX)) {
            handleDiscPlay(new DiscPlayRecord(player, self, time));
            checkForChallengeComplete(player);
        }
    }

    private static void handleDiscPlay(@NotNull DiscPlayRecord playRecord) {
        playingDiscs.addLast(playRecord);
        if (playingDiscs.size() > MAX_DISC_RECORDS) {
            playingDiscs.removeFirst();
        }
    }

    private static void checkForChallengeComplete(@NotNull ServerPlayerEntity player) {
        long currentTime = player.getWorld().getTime();
        Set<MusicDiscItem> records = playingDiscs.stream()
                .filter(playRecord -> playRecord.player().equals(player))
                .filter(playRecord -> currentTime < playRecord.startTime() + DISK_DURATION)
                .map(DiscPlayRecord::disc)
                .collect(Collectors.toSet());

        if (records.size() == MUSIC_DISCS.size()) {
            ((ExtendedPlayerEntity) player).markChallengeCompleted(Challenges.REMIX);
        }
    }
}
