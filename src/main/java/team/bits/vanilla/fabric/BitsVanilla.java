package team.bits.vanilla.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.commands.Commands;
import team.bits.vanilla.fabric.database.driver.DatabaseConnection;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.event.damage.PlayerDamageCallback;
import team.bits.vanilla.fabric.event.misc.PlayerConnectEvent;
import team.bits.vanilla.fabric.event.sleep.PlayerMoveCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerSleepCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerWakeUpCallback;
import team.bits.vanilla.fabric.listeners.SleepListener;
import team.bits.vanilla.fabric.teleport.Teleporter;

public class BitsVanilla implements ModInitializer, ServerLifecycleEvents.ServerStopped {

    private static FabricServerAudiences adventure;

    public static FabricServerAudiences adventure() {
        if (adventure == null) {
            throw new IllegalStateException("Tried to access Adventure without a running server!");
        }
        return adventure;
    }

    public static @NotNull Audience audience(@NotNull CommandOutput source) {
        return adventure().audience(source);
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> adventure = FabricServerAudiences.of(server));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> adventure = null);

        DatabaseConnection.open();

        Commands.registerCommands();

        SleepListener sleepListener = new SleepListener();
        PlayerSleepCallback.EVENT.register(sleepListener);
        PlayerWakeUpCallback.EVENT.register(sleepListener);

        Teleporter teleporter = new Teleporter();
        PlayerMoveCallback.EVENT.register(teleporter);
        PlayerDamageCallback.EVENT.register(teleporter);

        ServerLifecycleEvents.SERVER_STOPPED.register(this);

        PlayerConnectEvent.EVENT.register((player, connection) -> PlayerUtils.updatePlayerUsername(player));
    }

    @Override
    public void onServerStopped(MinecraftServer server) {
        DatabaseConnection.close();
    }
}
