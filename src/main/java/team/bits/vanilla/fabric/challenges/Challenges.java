package team.bits.vanilla.fabric.challenges;

import net.minecraft.server.network.*;
import net.minecraft.text.*;
import team.bits.vanilla.fabric.database.*;

import java.util.*;

public final class Challenges {

    public static final Challenge CARGO_SHIP = new Challenge(
            "cargo_ship", "Cargo Ship",
            "Fill a boat with 729 stacks of Cobblestone",
            ChallengeRewardItems.INSTAMINER
    );
    public static final Challenge PHANTOM_FIREBALL = new Challenge(
            "phantom_fireball", "Your New Best Friend",
            "Use a Ghast's fireball to kill a phantom",
            ChallengeRewardItems.TREECAPITATOR
    );
    public static final Challenge WARDEN_END = new Challenge(
            "warden_end", "Home Sweet Home",
            "Bring a Warden to The End",
            ChallengeRewardItems.PROSPEROUS_PICKAXE
    );
    public static final Challenge WITHER_DRAGON = new Challenge(
            "wither_dragon", "Package Deal",
            "Kill a Wither and an Ender Dragon within 30 seconds of each other",
            ChallengeRewardItems.RUNNING_BOOTS
    );
    public static final Challenge WORLD_CORNERS = new Challenge(
            "world_corners", "Flat Earther's Dream",
            "Visit all corners of the world",
            ChallengeRewardItems.NEPTUNE
    );
    public static final Challenge REMIX = new Challenge(
            "remix", "Remix",
            "Play every single music disc at once",
            ChallengeRewardItems.EVERLASTING_WINGS
    );

    private static final Set<Challenge> CHALLENGES = Set.of(
            CARGO_SHIP, PHANTOM_FIREBALL, WARDEN_END,
            WITHER_DRAGON, WORLD_CORNERS, REMIX
    );

    private static final String CHALLENGE_ANNOUNCEMENT = "%s completed the %s challenge";

    private Challenges() {
    }

    public static Set<Challenge> getAllChallenges() {
        return CHALLENGES;
    }

    public static Text getChallengeAnnouncement(Challenge challenge, ServerPlayerEntity player) {
        final ChallengeInformation info = challenge.getInformation();
        final String displayName = info.displayName();
        final String description = info.description();
        final String playerName = PlayerApiUtils.getEffectiveName(player);
        final Text rewardText = info.reward().toHoverableText();

        return Text.literal(String.format(CHALLENGE_ANNOUNCEMENT, playerName, displayName))
                .append(Text.literal(" and received "))
                .append(rewardText)
                .append(" as a reward!")
                .styled(style ->
                        style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(description)))
                );
    }
}
