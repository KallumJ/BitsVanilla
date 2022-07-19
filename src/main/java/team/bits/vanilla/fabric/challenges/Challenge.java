package team.bits.vanilla.fabric.challenges;

import net.minecraft.item.*;
import net.minecraft.server.network.*;
import net.minecraft.sound.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

public class Challenge {

    private final ChallengeInformation information;

    public Challenge(String tag, String displayName, String description, ItemStack reward) {
        this.information = new ChallengeInformation(tag, displayName, description, reward);
    }

    public void complete(@NotNull ServerPlayerEntity player) {
        ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player;
        ePlayer.markChallengeCompleted(this);
        ePlayer.giveItem(this.information.reward().copy()); // use copy to prevent accidental back-references
        ePlayer.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
        ServerInstance.broadcast(Challenges.getChallengeAnnouncement(this, player), MessageTypes.POSITIVE);
    }

    public @NotNull ChallengeInformation getInformation() {
        return this.information;
    }

    @Override
    public String toString() {
        return this.information.tag();
    }

    public void awardToNearby(World world, Vec3d center, int range) {
        Box box = new Box(
                center.getX() - range, center.getY() - range, center.getZ() - range,
                center.getX() + range, center.getY() + range, center.getZ() + range
        );
        Collection<ServerPlayerEntity> nearbyPlayers = world.getEntitiesByClass(
                ServerPlayerEntity.class, box, player -> true
        );

        for (ServerPlayerEntity player : nearbyPlayers) {
            ((ExtendedPlayerEntity) player).markChallengeCompleted(this);
        }
    }
}
