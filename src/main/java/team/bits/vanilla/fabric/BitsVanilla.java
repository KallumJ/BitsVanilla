package team.bits.vanilla.fabric;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import net.fabricmc.api.ModInitializer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.event.base.EventManager;
import team.bits.nibbles.event.misc.*;
import team.bits.nibbles.utils.PropertiesFileUtils;
import team.bits.nibbles.utils.Scheduler;
import team.bits.vanilla.fabric.commands.Commands;
import team.bits.vanilla.fabric.commands.VersionCommand;
import team.bits.vanilla.fabric.database.player.PlayerNameLoader;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.database.util.ServerUtils;
import team.bits.vanilla.fabric.database.warp.WarpUtils;
import team.bits.vanilla.fabric.listeners.*;
import team.bits.vanilla.fabric.statistics.lib.PlayerAPIStatsSyncHandler;
import team.bits.vanilla.fabric.statistics.lib.StatTracker;
import team.bits.vanilla.fabric.teleport.Teleporter;
import team.bits.vanilla.fabric.util.AFKManager;
import team.bits.vanilla.fabric.util.SpawnPreventionHandler;
import team.bits.vanilla.fabric.util.color.NameColors;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class BitsVanilla implements ModInitializer, ServerStoppingEvent.Listener {

    private static final Logger LOGGER = LogManager.getLogger();

    private static FabricServerAudiences adventure;

    private Connection apiConnection;

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

    @Override
    public void onInitialize() {
        EventManager.INSTANCE.registerEvents((ServerStartingEvent.Listener) event -> adventure = FabricServerAudiences.of(event.getServer()));
        EventManager.INSTANCE.registerEvents((ServerStoppedEvent.Listener) event -> adventure = null);

        EventManager.INSTANCE.registerEvents((ServerInstanceReadyEvent.Listener) serverInstanceReadyEvent -> VersionCommand.init());

        LOGGER.info(String.format("Server name is '%s'", ServerUtils.getServerName()));

        Properties config = PropertiesFileUtils.loadFromFile(new File("config", "bits-vanilla.cfg"));
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(config.getProperty("hostname"));
        connectionFactory.setUsername(config.getProperty("username"));
        connectionFactory.setPassword(config.getProperty("password"));
        try {
            this.apiConnection = connectionFactory.newConnection(ServerUtils.getServerName());
        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException("Error while connecting to RabbitMQ", ex);
        }

        PlayerUtils.init(this.apiConnection);
        WarpUtils.init(this.apiConnection);

        int spawnProtectionRadius = Integer.parseInt(config.getProperty("spawn-radius", "32"));
        // we want to initialize this after the first tick, because we have to wait for
        // the world to be loaded before we can do this.
        Scheduler.runAfterTick(() -> SpawnPreventionHandler.INSTANCE.init(spawnProtectionRadius));

        NameColors.INSTANCE.load();

        Commands.registerCommands();

        EventManager.INSTANCE.registerEvents(this);
        EventManager.INSTANCE.registerEvents(new SleepListener());
        EventManager.INSTANCE.registerEvents(new Teleporter());

        EventManager.INSTANCE.registerEvents((PlayerConnectEvent.Listener) event -> {
            PlayerUtils.updatePlayerUsername(event.getPlayer());
            PlayerNameLoader.loadNameData(event.getPlayer());
        });

        EventManager.INSTANCE.registerEvents(new NewPlayerListener());
//        EventManager.INSTANCE.registerEvents(new CustomClientHandler());

        EventManager.INSTANCE.registerEvents(new PlayerConnectListener());
        EventManager.INSTANCE.registerEvents(new PlayerDisconnectListener());

        AFKManager.initAfkManager();

        Scheduler.scheduleAtFixedRate(new StatTracker(), 0, 200);

        PlayerAPIStatsSyncHandler.init(this.apiConnection);
    }

    @Override
    public void onServerStopping(@NotNull ServerStoppingEvent event) {
        PlayerAPIStatsSyncHandler.stop();
        Scheduler.stop();

        try {
            this.apiConnection.close();
        } catch (IOException ex) {
            LOGGER.error("Error while closing RabbitMQ connection", ex);
        }
    }
}
