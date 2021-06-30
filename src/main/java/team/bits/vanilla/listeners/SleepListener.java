package team.bits.vanilla.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.event.sleep.PlayerSleepCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerWakeUpCallback;
import team.bits.vanilla.fabric.teleport.Teleporter;
import team.bits.vanilla.fabric.util.Location;
import team.bits.vanilla.fabric.util.Scheduler;

import java.util.HashSet;
import java.util.Set;

public class SleepListener implements PlayerSleepCallback, PlayerWakeUpCallback {

    private final Set<PlayerEntity> sleeping = new HashSet<>();

    private static int getOnlinePlayerCount(MinecraftServer server) {
        return Math.toIntExact(server.getPlayerManager().getPlayerList().stream()
                        .filter(player -> player.interactionManager.getGameMode() == GameMode.SURVIVAL)
//                .filter(player -> AFKDetector.INSTANCE.isNotAFK(player))
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

        Scheduler.schedule(() -> {
            Teleporter.queueTeleport(player, Location.get(player).add(10, 0, 0), null);
        }, 20);
    }

    private void checkSleeping(@NotNull ServerWorld world) {
        MinecraftServer server = world.getServer();

        int sleeping = this.sleeping.size();
        int online = Math.max(getOnlinePlayerCount(server), 1);

        if ((double) sleeping / online >= 0.5) {

            this.sleeping.clear();

            world.setTimeOfDay(0);
            world.setWeather(12000, 0, false, false);

//                Bukkit.getOnlinePlayers().forEach(player -> player.setStatistic(Statistic.TIME_SINCE_REST, 0));

            sendSleepingMessage(server, sleeping, online, true);

        } else {
            sendSleepingMessage(server, sleeping, online, false);
        }
    }
}
