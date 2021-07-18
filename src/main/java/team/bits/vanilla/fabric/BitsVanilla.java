package team.bits.vanilla.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.commands.Commands;
import team.bits.vanilla.fabric.database.driver.DatabaseConnection;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.database.util.ServerUtils;
import team.bits.vanilla.fabric.event.damage.PlayerDamageCallback;
import team.bits.vanilla.fabric.event.misc.PlayerConnectEvent;
import team.bits.vanilla.fabric.event.misc.PlayerDisconnectEvent;
import team.bits.vanilla.fabric.event.sleep.PlayerMoveCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerSleepCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerWakeUpCallback;
import team.bits.vanilla.fabric.listeners.*;
import team.bits.vanilla.fabric.statistics.lib.DatabaseStatHandler;
import team.bits.vanilla.fabric.statistics.lib.StatTracker;
import team.bits.vanilla.fabric.teleport.Teleporter;
import team.bits.vanilla.fabric.util.AFKManager;
import team.bits.vanilla.fabric.util.Scheduler;
import team.bits.vanilla.fabric.util.color.NameColors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BitsVanilla implements ModInitializer, ServerLifecycleEvents.ServerStopped {

    private static final Logger LOGGER = LogManager.getLogger();

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

    public static @NotNull Audience audience(@NotNull ServerCommandSource source) {
        return adventure().audience(source);
    }

    private ExecutorService executor;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> adventure = FabricServerAudiences.of(server));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> adventure = null);

        LOGGER.info(String.format("Server name is '%s'", ServerUtils.getServerName()));

        DatabaseConnection.open();

        NameColors.INSTANCE.load();

        Commands.registerCommands();

        SleepListener sleepListener = new SleepListener();
        PlayerSleepCallback.EVENT.register(sleepListener);
        PlayerWakeUpCallback.EVENT.register(sleepListener);

        Teleporter teleporter = new Teleporter();
        PlayerMoveCallback.EVENT.register(teleporter);
        PlayerDamageCallback.EVENT.register(teleporter);

        ServerLifecycleEvents.SERVER_STOPPED.register(this);

        PlayerConnectEvent.EVENT.register((player, connection) -> PlayerUtils.updatePlayerUsername(player));
        PlayerConnectEvent.EVENT.register(new NewPlayerListener());

        PlayerConnectEvent.EVENT.register(new PlayerConnectListener());
        PlayerMoveCallback.EVENT.register(new PlayerMoveListener());
        PlayerDisconnectEvent.EVENT.register(new PlayerDisconnectListener());

        AFKManager.initAfkManager();

        Scheduler.scheduleAtFixedRate(new StatTracker(), 0, 20);

        DatabaseStatHandler.init();
        this.executor = Executors.newSingleThreadExecutor();
        this.executor.execute(new DatabaseStatHandler());
    }

    @Override
    public void onServerStopped(MinecraftServer server) {
        this.executor.shutdownNow();
        Scheduler.stop();
        DatabaseConnection.close();
    }
}
