package team.bits.vanilla.fabric.statistics.lib;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.database.driver.DatabaseConnection;
import team.bits.vanilla.fabric.database.util.QueryHelper;
import team.bits.vanilla.fabric.database.util.ServerUtils;
import team.bits.vanilla.fabric.database.util.model.DataTypes;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DatabaseStatHandler implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final BlockingQueue<QueuedDatabaseUpdate> databaseQueue = new LinkedBlockingQueue<>();

    public static void enqueue(@NotNull TrackedStat stat, @NotNull ServerPlayerEntity player, int level) {
        databaseQueue.add(new QueuedDatabaseUpdate(stat, player, level));
    }

    public static void init() {
        // try to insert all the statistic names to
        // make sure they all exist in the database
        for (TrackedStat stat : CustomStats.TRACKED_STATS.values()) {
            try {
                QueryHelper.prepareStatement(DatabaseConnection.getConnection(),
                        "INSERT INTO statistic (name) VALUES (?)",
                        Collections.singleton(
                                DataTypes.STRING.create(stat.customName())
                        )
                ).executeUpdate();
            } catch (SQLException ignored) {
                // we expect an exception if the statistic already
                // exists, but we can just ignore it
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                this.handleEntry(databaseQueue.take());
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void handleEntry(@NotNull QueuedDatabaseUpdate entry) {
        final ServerPlayerEntity player = entry.player();
        final ServerStatHandler statHandler = player.getStatHandler();
        final TrackedStat trackedStat = entry.stat();

        int count = statHandler.getStat(trackedStat.stat());

        final Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            QueryHelper.prepareStatement(
                    databaseConnection, ("" +
                            // this query will attempt to insert a new statistic_data record, but
                            // if there is already an record for this player,statistic,server combination
                            // it will update the level and count of the existing record instead
                            "INSERT INTO statistic_data (player, statistic, server, count, level)" +
                            "   VALUES (" +
                            "               (SELECT id FROM uuid WHERE uuid=?)," +
                            "               (SELECT id FROM statistic WHERE name=?)," +
                            "               (SELECT id FROM server WHERE name=?)," +
                            "               ?, ?" +
                            "          )" +
                            "   ON DUPLICATE KEY UPDATE count=VALUES(count), level=VALUES(level)")
                            .replaceAll("\\s+", " "), // remove any excess whitespace to compress the query
                    Arrays.asList(
                            DataTypes.STRING.create(player.getUuidAsString()), // player uuid
                            DataTypes.STRING.create(trackedStat.customName()), // statistic name
                            DataTypes.STRING.create(ServerUtils.getServerName()), // server name
                            DataTypes.INTEGER.create(count), // statistic count
                            DataTypes.INTEGER.create(entry.level()) // statistic level
                    )
            ).executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while updating statistic data", ex);
        }
    }

    private record QueuedDatabaseUpdate(@NotNull TrackedStat stat, @NotNull ServerPlayerEntity player, int level) {
    }

    public static void handlePlayerJoin(@NotNull ServerPlayerEntity player) {
        final ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player;

        if (!ePlayer.hasMigratedStats()) {

            final Connection databaseConnection = DatabaseConnection.getConnection();
            PreparedStatement statement = QueryHelper.prepareStatement(
                    databaseConnection, ("" +
                            // get the level for every statistic for a given player,
                            // using a join to map the statistic id to a name
                            "SELECT statistic.name, level FROM statistic_data" +
                            "   JOIN statistic ON statistic.id=statistic_data.statistic" +
                            "   WHERE player=(SELECT id FROM uuid WHERE uuid=?)" +
                            "   AND server=(SELECT id FROM server WHERE name=?)" +
                            "   AND level > 0")
                            .replaceAll("\\s+", " "), // remove any excess whitespace to compress the query
                    Arrays.asList(
                            DataTypes.STRING.create(player.getUuidAsString()), // player uuid
                            DataTypes.STRING.create(ServerUtils.getServerName()) // server name
                    )
            );

            // load all the results in a hashmap where the key
            // is the name of the stat, and the value is the level
            Map<String, Integer> levels = new HashMap<>();
            try {
                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    levels.put(result.getString(1), result.getInt(2));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                throw new RuntimeException("SQLException while migrating statistic data", ex);
            }

            // look at the level for every statistic, if the player is
            // higher than level 0, store the new level
            for (TrackedStat stat : CustomStats.TRACKED_STATS.values()) {
                int level = levels.getOrDefault(stat.customName(), 0);
                if (level > 0) {
                    StatTracker.setStoredLevel(player, stat.stat(), level);
                }
            }

            ePlayer.markMigratedStats();

            LOGGER.info(String.format("Migrated statistics for player %s", player.getDisplayName().getString()));
        }
    }
}
