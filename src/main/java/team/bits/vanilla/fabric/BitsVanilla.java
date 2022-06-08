package team.bits.vanilla.fabric;

import com.rabbitmq.client.*;
import net.fabricmc.api.*;
import org.apache.logging.log4j.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.event.base.*;
import team.bits.nibbles.event.server.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.commands.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.listeners.*;
import team.bits.vanilla.fabric.teleport.*;
import team.bits.vanilla.fabric.util.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class BitsVanilla implements ModInitializer, ServerStoppingEvent.Listener {

    public static final Logger LOGGER = LogManager.getLogger();

    private Connection apiConnection;

    @Override
    public void onInitialize() {
        EventManager.INSTANCE.registerEvents((ServerInstanceReadyEvent.Listener) serverInstanceReadyEvent -> VersionCommand.init());

        try {
            LOGGER.info(String.format("Server name is '%s'", ServerUtils.getServerName()));
        } catch (Exception ex) {
            throw new RuntimeException("Cannot find server-name in server.properties");
        }

        File configFile = new File("config", "bits-vanilla.cfg");
        if (!configFile.exists()) {
            throw new RuntimeException("Cannot find file 'config/bits-vanilla.cfg'");
        }

        Properties config = PropertiesFileUtils.loadFromFile(configFile);
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(config.getProperty("hostname"));
        connectionFactory.setUsername(config.getProperty("username"));
        connectionFactory.setPassword(config.getProperty("password"));
        try {
            this.apiConnection = connectionFactory.newConnection(ServerUtils.getServerName());
        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException("Error while connecting to RabbitMQ", ex);
        }

        PlayerApiUtils.init(this.apiConnection);
        WarpApiUtils.init(this.apiConnection);

        Scheduler.scheduleAtFixedRate(Teleporter::teleportTask, 0, Teleporter.TASK_INTERVAL);

        NameColors.INSTANCE.load();

        Commands.registerCommands();

        EventManager.INSTANCE.registerEvents(this);
        EventManager.INSTANCE.registerEvents(new SleepListener());
        EventManager.INSTANCE.registerEvents(new Teleporter());

        EventManager.INSTANCE.registerEvents((PlayerConnectEvent.Listener) event -> {
            PlayerApiUtils.updatePlayerUsername(event.getPlayer());
            PlayerNameLoader.loadNameData(event.getPlayer());
        });

        EventManager.INSTANCE.registerEvents(new NewPlayerListener());
//        EventManager.INSTANCE.registerEvents(new CustomClientHandler());

        EventManager.INSTANCE.registerEvents(new PlayerConnectListener());
        EventManager.INSTANCE.registerEvents(new PlayerDisconnectListener());

        EventManager.INSTANCE.registerEvents(new TreeFellingListener());
        EventManager.INSTANCE.registerEvents(new RunningBootsRunListener());

        EventManager.INSTANCE.registerEvents(new MobHeadListener());

        AFKManager.initAfkManager();
    }

    @Override
    public void onServerStopping(@NotNull ServerStoppingEvent event) {
        Scheduler.stop();

        try {
            this.apiConnection.close();
        } catch (IOException ex) {
            LOGGER.error("Error while closing RabbitMQ connection", ex);
        }
    }
}
