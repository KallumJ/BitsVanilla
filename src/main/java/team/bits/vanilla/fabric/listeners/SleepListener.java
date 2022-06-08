package team.bits.vanilla.fabric.listeners;

import net.minecraft.entity.player.*;
import net.minecraft.server.*;
import net.minecraft.server.network.*;
import net.minecraft.server.world.*;
import net.minecraft.stat.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.event.interaction.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.mixin.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

public class SleepListener implements PlayerSleepEvent.Listener, PlayerWakeUpEvent.Listener {

    private final Set<PlayerEntity> sleeping = new HashSet<>();

    private static int getOnlinePlayerCount(MinecraftServer server) {
        return Math.toIntExact(server.getPlayerManager().getPlayerList().stream()
                .filter(player -> player.interactionManager.getGameMode() == GameMode.SURVIVAL)
                .filter(player -> !AFKManager.isAFK(player))
                .count()
        );
    }

    private static void sendSleepingMessage(int sleeping, int online, boolean green) {
        final float ratio = (float) sleeping / online;
        final Text message = Text.literal(
                String.format("%s/%s player(s) sleeping, %s%%",
                        sleeping, Math.max(online, 1), Math.min(Math.round(ratio * 100f), 100f)
                )
        ).styled(style -> style.withColor(green ? Formatting.GREEN : Formatting.YELLOW));

        ServerInstance.broadcast(message, MessageTypes.PLAIN);
    }

    @Override
    public void onPlayerSleep(@NotNull PlayerSleepEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity player) {
            ServerWorld world = player.getWorld();

            this.sleeping.add(player);
            this.checkSleeping(world);
        }
    }

    @Override
    public void onPlayerWakeUp(@NotNull PlayerWakeUpEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity player && !this.sleeping.isEmpty()) {
            this.sleeping.remove(player);

            final MinecraftServer server = Objects.requireNonNull(player.getServer());
            final int sleepingCount = this.sleeping.size();
            final int online = Math.max(getOnlinePlayerCount(server), 1);

            sendSleepingMessage(sleepingCount, online, false);
        }
    }

    private void checkSleeping(@NotNull ServerWorld world) {
        final MinecraftServer server = world.getServer();

        final int sleepingCount = this.sleeping.size();
        final int online = Math.max(getOnlinePlayerCount(server), 1);

        if ((float) sleepingCount / online >= 0.5f) {

            this.sleeping.clear();

            world.setTimeOfDay(0);
            ((ServerWorldInvoker) world).invokeResetWeather();

            PlayerManager playerManager = ServerInstance.get().getPlayerManager();
            playerManager.getPlayerList().forEach(player ->
                    player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST))
            );

            sendSleepingMessage(sleepingCount, online, true);

        } else {
            sendSleepingMessage(sleepingCount, online, false);
        }
    }
}
