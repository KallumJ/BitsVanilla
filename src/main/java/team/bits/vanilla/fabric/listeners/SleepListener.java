package team.bits.vanilla.fabric.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.event.sleep.PlayerSleepCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerWakeUpCallback;
import team.bits.vanilla.fabric.mixin.ServerWorldInvoker;
import team.bits.vanilla.fabric.util.AFKManager;
import team.bits.vanilla.fabric.util.ServerInstance;

import java.util.HashSet;
import java.util.Set;

public class SleepListener implements PlayerSleepCallback, PlayerWakeUpCallback {

    private final Set<PlayerEntity> sleeping = new HashSet<>();

    private static int getOnlinePlayerCount(MinecraftServer server) {
        return Math.toIntExact(server.getPlayerManager().getPlayerList().stream()
                .filter(player -> player.interactionManager.getGameMode() == GameMode.SURVIVAL)
                .filter(player -> !AFKManager.isAFK(player))
                .count()
        );
    }

    private static void sendSleepingMessage(MinecraftServer server, int sleeping, int online, boolean green) {
        float ratio = (float) sleeping / online;
        TextComponent message = Component.text()
                .append(
                        Component.text()
                                .append(Component.text(sleeping))
                                .append(Component.text('/'))
                                .append(Component.text(Math.max(online, 1)))
                                .append(Component.text(" player(s) sleeping, "))
                                .append(Component.text(Math.min(Math.round(ratio * 100f), 100f)))
                                .append(Component.text("%"))
                )
                .color(green ? NamedTextColor.GREEN : NamedTextColor.YELLOW)
                .build();

        BitsVanilla.adventure().audience(server.getPlayerManager().getPlayerList())
                .sendMessage(message);
    }

    @Override
    public void onSleep(@NotNull PlayerEntity player) {
        ServerWorld world = (ServerWorld) player.world;

        this.sleeping.add(player);
        this.checkSleeping(world);
    }

    @Override
    public void onWakeUp(@NotNull PlayerEntity player) {
        this.sleeping.remove(player);
    }

    private void checkSleeping(@NotNull ServerWorld world) {
        MinecraftServer server = world.getServer();

        int sleeping = this.sleeping.size();
        int online = Math.max(getOnlinePlayerCount(server), 1);

        if ((double) sleeping / online >= 0.5) {

            this.sleeping.clear();

            world.setTimeOfDay(0);
            ((ServerWorldInvoker) world).invokeResetWeather();

            PlayerManager playerManager = ServerInstance.get().getPlayerManager();
            playerManager.getPlayerList().forEach(player ->
                    player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST))
            );

            sendSleepingMessage(server, sleeping, online, true);

        } else {
            sendSleepingMessage(server, sleeping, online, false);
        }
    }
}
