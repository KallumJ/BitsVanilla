package team.bits.vanilla.fabric.listeners;

import net.minecraft.entity.player.*;
import net.minecraft.server.network.*;
import net.minecraft.sound.*;
import net.minecraft.text.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.player.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

public class DuelHandler {

    private static final String DUEL_STARTING_MSG = "Duel starting in %s seconds";
    private static final String DUEL_STARTED_MSG = "Duel started, fight!";
    private static final String DUEL_FINISHED = "%s won in a duel against %s!";

    private static final Collection<Duel> QUEUED_DUELS = new LinkedList<>();

    static {
        Scheduler.scheduleAtFixedRate(DuelHandler::tick, 20, 20);
    }

    private DuelHandler() {
    }

    public static void startDuel(@NotNull ServerPlayerEntity player1, @NotNull ServerPlayerEntity player2) {
        Duel duel = new Duel(player1, player2);
        QUEUED_DUELS.add(duel);
    }

    public static void finishDuel(@NotNull PlayerEntity winner, @NotNull PlayerEntity loser) {
        ((ExtendedPlayerEntity) winner).setDuelTarget(null);
        ((ExtendedPlayerEntity) loser).setDuelTarget(null);

        ((INibblesPlayer) winner).playSound(SoundEvents.ENTITY_WITHER_DEATH, SoundCategory.PLAYERS, 1.0f, 1.5f);
        ((INibblesPlayer) loser).playSound(SoundEvents.ENTITY_WITHER_DEATH, SoundCategory.PLAYERS, 1.0f, 1.5f);

        Text finishedMessage = Text.literal(
                String.format(DUEL_FINISHED,
                        PlayerApiUtils.getEffectiveName((ServerPlayerEntity) winner),
                        PlayerApiUtils.getEffectiveName((ServerPlayerEntity) loser)
                )
        );

        for (ServerPlayerEntity player : ServerInstance.get().getPlayerManager().getPlayerList()) {
            player.sendMessage(finishedMessage, MessageTypes.POSITIVE);
        }
    }

    private static void tick() {
        new ArrayList<>(QUEUED_DUELS).forEach(Duel::tick);
    }

    private static final class Duel {

        private final ServerPlayerEntity player1;
        private final ServerPlayerEntity player2;

        private int countdown = 5;

        private Duel(ServerPlayerEntity player1, ServerPlayerEntity player2) {
            this.player1 = player1;
            this.player2 = player2;
        }

        public void tick() {
            INibblesPlayer nPlayer1 = (INibblesPlayer) player1;
            INibblesPlayer nPlayer2 = (INibblesPlayer) player2;

            if (this.countdown > 0) {
                this.sendMessage(Text.literal(String.format(DUEL_STARTING_MSG, this.countdown)));


                nPlayer1.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.2f);
                nPlayer2.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.2f);
            } else {
                this.sendMessage(Text.literal(DUEL_STARTED_MSG).styled(style -> style.withBold(true)));

                ((ExtendedPlayerEntity) this.player1).setDuelTarget(player2);
                ((ExtendedPlayerEntity) this.player2).setDuelTarget(player1);

                nPlayer1.playSound(SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 1.0f, 1.2f);
                nPlayer2.playSound(SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 1.0f, 1.2f);

                QUEUED_DUELS.remove(this);
            }

            this.countdown--;
        }

        private void sendMessage(Text message) {
            this.player1.sendMessage(message, MessageTypes.NEUTRAL);
            this.player2.sendMessage(message, MessageTypes.NEUTRAL);
        }
    }
}
