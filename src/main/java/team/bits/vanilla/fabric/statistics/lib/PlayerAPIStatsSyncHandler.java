package team.bits.vanilla.fabric.statistics.lib;

import com.rabbitmq.client.Connection;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import team.bits.servicelib.client.GraphQLRPCClient;
import team.bits.vanilla.fabric.database.util.ServerUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class PlayerAPIStatsSyncHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final BlockingQueue<QueuedDatabaseUpdate> databaseQueue = new LinkedBlockingQueue<>();

    private static GraphQLRPCClient rpcClient;

    public static void enqueue(@NotNull TrackedStat stat,
                               @NotNull ServerPlayerEntity player,
                               @NotNull StatRecord statRecord) {
        databaseQueue.add(new QueuedDatabaseUpdate(stat, player, statRecord));
    }

    public static void init(@NotNull Connection connection) {
        try {
            rpcClient = new GraphQLRPCClient(connection, "player-api");
        } catch (IOException ex) {
            throw new RuntimeException("Error while creating RPC client", ex);
        }

        PlayerAPIStatsSyncHandler handler = new PlayerAPIStatsSyncHandler();
        executor.execute(handler::run);
    }

    public static void stop() {
        executor.shutdownNow();
    }

    private void run() {
        while (true) {
            try {
                this.handleEntry(databaseQueue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void handleEntry(@NotNull QueuedDatabaseUpdate entry) {
        final ServerPlayerEntity player = entry.player();
        final TrackedStat trackedStat = entry.stat();
        final StatRecord statRecord = entry.statRecord();

        try {
            rpcClient.call(
                            "mutation($uuid: ID!, $server: String!, $statistic: StatisticInput!) {" +
                                    "   statistic(uuid: $uuid, server: $server, statistic: $statistic) { name }" +
                                    "}",
                            Map.of(
                                    "uuid", player.getUuidAsString(),
                                    "server", ServerUtils.getServerName(),
                                    "statistic", Map.of(
                                            "name", trackedStat.customName(),
                                            "count", statRecord.count(),
                                            "level", statRecord.level()
                                    )
                            )
                    )
                    // if an error occurs, log it to the console
                    .whenComplete((jsonObject, throwable) -> {
                        if (throwable != null) {
                            LOGGER.error("Error while updating player statistic", throwable);
                        }
                    });
        } catch (IOException ex) {
            throw new RuntimeException("Error while updating player data", ex);
        }
    }

    private static record QueuedDatabaseUpdate(@NotNull TrackedStat stat,
                                               @NotNull ServerPlayerEntity player,
                                               @NotNull StatRecord statRecord) {
    }
}
