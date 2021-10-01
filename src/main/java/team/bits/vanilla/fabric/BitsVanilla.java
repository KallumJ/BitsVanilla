package team.bits.vanilla.fabric;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.event.damage.PlayerDamageEvent;
import team.bits.nibbles.event.misc.PlayerConnectEvent;
import team.bits.nibbles.event.misc.PlayerDisconnectEvent;
import team.bits.nibbles.event.misc.PlayerMoveEvent;
import team.bits.nibbles.event.misc.ServerInstanceReadyEvent;
import team.bits.nibbles.utils.PropertiesFileUtils;
import team.bits.nibbles.utils.Scheduler;
import team.bits.vanilla.fabric.commands.Commands;
import team.bits.vanilla.fabric.commands.VersionCommand;
import team.bits.vanilla.fabric.database.DatabaseConnection;
import team.bits.vanilla.fabric.database.player.PlayerNameLoader;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.database.util.ServerUtils;
import team.bits.vanilla.fabric.database.warp.WarpUtils;
import team.bits.vanilla.fabric.listeners.*;
import team.bits.vanilla.fabric.statistics.lib.DatabaseStatHandler;
import team.bits.vanilla.fabric.statistics.lib.StatTracker;
import team.bits.vanilla.fabric.teleport.Teleporter;
import team.bits.vanilla.fabric.util.AFKManager;
import team.bits.vanilla.fabric.util.color.NameColors;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class BitsVanilla implements ModInitializer, ServerLifecycleEvents.ServerStopping {

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

        ServerInstanceReadyEvent.EVENT.register(minecraftDedicatedServer -> VersionCommand.init());

        LOGGER.info(String.format("Server name is '%s'", ServerUtils.getServerName()));

        Properties config = PropertiesFileUtils.loadFromFile(new File("config", "bits-vanilla.cfg"));
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(config.getProperty("hostname"));
        connectionFactory.setUsername(config.getProperty("username"));
        connectionFactory.setPassword(config.getProperty("password"));
        Connection connection;
        try {
            connection = connectionFactory.newConnection(ServerUtils.getServerName());
        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException("Error while connecting to RabbitMQ", ex);
        }

        PlayerUtils.init(connection);
        WarpUtils.init(connection);

        DatabaseConnection.open();

        NameColors.INSTANCE.load();

        Commands.registerCommands();

        SleepListener sleepListener = new SleepListener();
        EntitySleepEvents.START_SLEEPING.register(sleepListener);
        EntitySleepEvents.STOP_SLEEPING.register(sleepListener);

        Teleporter teleporter = new Teleporter();
        PlayerMoveEvent.EVENT.register(teleporter);
        PlayerDamageEvent.EVENT.register(teleporter);

        ServerLifecycleEvents.SERVER_STOPPING.register(this);

        PlayerConnectEvent.EVENT.register((player, conn) -> {
            PlayerUtils.updatePlayerUsername(player);
            PlayerNameLoader.loadNameData(player);
        });
        PlayerConnectEvent.EVENT.register(new NewPlayerListener());
        PlayerConnectEvent.EVENT.register(new CustomClientHandler());

        PlayerConnectEvent.EVENT.register(new PlayerConnectListener());
        PlayerMoveEvent.EVENT.register(new PlayerMoveListener());
        PlayerDisconnectEvent.EVENT.register(new PlayerDisconnectListener());

        AFKManager.initAfkManager();

        Scheduler.scheduleAtFixedRate(new StatTracker(), 0, 20);

        DatabaseStatHandler.init();
        this.executor = Executors.newSingleThreadExecutor();
        this.executor.execute(new DatabaseStatHandler());
    }

    @Override
    public void onServerStopping(MinecraftServer server) {
        this.executor.shutdownNow();
        Scheduler.stop();
        DatabaseConnection.close();
    }
}
