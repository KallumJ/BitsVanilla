package team.bits.vanilla.fabric.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.utils.Colors;
import team.bits.nibbles.utils.Scheduler;
import team.bits.nibbles.utils.ServerInstance;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class DuelHandler {

    private static final String DUEL_STARTING_MSG = "Duel starting in %s seconds";
    private static final String DUEL_STARTED_MSG = "Duel started, fight!";
    private static final String DUEL_FINISHED = "%s won in a duel against %s!";

    private static final Collection<Duel> QUEUED_DUELS = new LinkedList<>();

    static {
        Scheduler.scheduleAtFixedRate(DuelHandler::tick, 20, 20);
    }

    public static void startDuel(@NotNull PlayerEntity player1, @NotNull PlayerEntity player2) {
        Duel duel = new Duel(player1, player2);
        QUEUED_DUELS.add(duel);
    }

    public static void finishDuel(@NotNull PlayerEntity winner, @NotNull PlayerEntity loser) {
        ((ExtendedPlayerEntity) winner).setDuelTarget(null);
        ((ExtendedPlayerEntity) loser).setDuelTarget(null);

        playSound(winner, SoundEvents.ENTITY_WITHER_DEATH, 1.0f, 1.5f);
        playSound(loser, SoundEvents.ENTITY_WITHER_DEATH, 1.0f, 1.5f);

        Component finishedMessage = Component.text(String.format(DUEL_FINISHED,
                PlayerUtils.getEffectiveName((ServerPlayerEntity) winner),
                PlayerUtils.getEffectiveName((ServerPlayerEntity) loser)
        ), Colors.POSITIVE);

        for (ServerPlayerEntity player : ServerInstance.get().getPlayerManager().getPlayerList()) {
            BitsVanilla.audience(player).sendMessage(finishedMessage);
        }
    }

    private static void tick() {
        new ArrayList<>(QUEUED_DUELS).forEach(Duel::tick);
    }

    private static void playSound(PlayerEntity player, SoundEvent sound, float volume, float pitch) {
        final ServerWorld world = ((ServerPlayerEntity) player).getServerWorld();
        final Vec3d position = player.getPos();

        world.playSound(
                null, position.x, position.y, position.z,
                sound, SoundCategory.PLAYERS, volume, pitch
        );
    }

    private static final class Duel {

        private final PlayerEntity player1;
        private final PlayerEntity player2;

        private int countdown = 5;

        private Duel(PlayerEntity player1, PlayerEntity player2) {
            this.player1 = player1;
            this.player2 = player2;
        }

        public void tick() {

            if (this.countdown > 0) {
                this.sendMessage(Component.text(String.format(DUEL_STARTING_MSG, this.countdown), Colors.NEUTRAL));

                playSound(this.player1, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                playSound(this.player2, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            } else {
                this.sendMessage(Component.text(DUEL_STARTED_MSG, Style.style(Colors.NEUTRAL, TextDecoration.BOLD)));

                ((ExtendedPlayerEntity) this.player1).setDuelTarget(player2);
                ((ExtendedPlayerEntity) this.player2).setDuelTarget(player1);

                playSound(this.player1, SoundEvents.ENTITY_WITHER_SPAWN, 1.0f, 1.2f);
                playSound(this.player2, SoundEvents.ENTITY_WITHER_SPAWN, 1.0f, 1.2f);

                QUEUED_DUELS.remove(this);
            }

            this.countdown--;
        }

        private void sendMessage(Component message) {
            BitsVanilla.audience(this.player1).sendActionBar(message);
            BitsVanilla.audience(this.player2).sendActionBar(message);
        }
    }
}
