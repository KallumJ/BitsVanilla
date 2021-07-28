package team.bits.vanilla.fabric.database.player;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.utils.ServerInstance;
import team.bits.vanilla.fabric.database.driver.DatabaseConnection;
import team.bits.vanilla.fabric.database.util.QueryHelper;
import team.bits.vanilla.fabric.database.util.model.DataTypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Most of these operations can be done with the {@link PlayerDataHandle} class,
 * but these functions have been specifically optimized to only use one SQL query
 * and return the minimum amount of data. If you need to use more than one of these
 * functions at the same time, it's better to use {@link PlayerDataHandle} instead.
 */
public final class PlayerUtils {

    private static final Logger LOGGER = LogManager.getLogger();

    private PlayerUtils() {
    }

    /**
     * Returns the username of a player with a given nickname. Result will be
     * empty if no player with the given nickname can be found.
     *
     * @param nickname the nickname of the player
     * @return the username of the player
     */
    public static @NotNull Optional<String> getUsername(@NotNull String nickname) {
        final Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            PreparedStatement getUsernameStatement = QueryHelper.prepareStatement(
                    databaseConnection,
                    "SELECT username FROM player_data WHERE nickname=? LIMIT 1",
                    Collections.singleton(
                            DataTypes.STRING.create(nickname)
                    )
            );

            // if there is any result, return the first value
            ResultSet resultSet = getUsernameStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(resultSet.getString(1));
            }

            return Optional.empty();

        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while obtaining player name", ex);
        }
    }

    /**
     * Get a list of nicknames of all the players that played on the server and have a nickname.
     *
     * @return a list of nicknames of all players that have one
     */
    public static @NotNull Collection<String> getNicknames() {
        final Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            // only get nicknames that aren't NULL
            ResultSet resultSet = databaseConnection.prepareStatement(
                    "SELECT nickname FROM player_data WHERE nickname IS NOT NULL"
            ).executeQuery();

            // convert the result set into a list
            Collection<String> names = new LinkedList<>();
            while (resultSet.next()) {
                names.add(resultSet.getString(1));
            }

            return Collections.unmodifiableCollection(names);

        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while obtaining player data", ex);
        }
    }

    /**
     * Get a list of effective names of all the players that played on the server.
     * An effective name is a player's nickname if they have one, otherwise their username.
     *
     * @return a list of effective names of all players that have ever played
     */
    public static @NotNull Collection<String> getAllNames() {
        final Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            // the SQL COALESCE function will get the first non-null
            // value in a list. So if the player has a nickname, it will
            // be used, otherwise the username will be used
            ResultSet resultSet = databaseConnection.prepareStatement(
                    "SELECT COALESCE(nickname, username) AS name FROM player_data"
            ).executeQuery();

            // convert the result set into a list
            Collection<String> names = new LinkedList<>();
            while (resultSet.next()) {
                names.add(resultSet.getString(1));
            }

            return Collections.unmodifiableCollection(names);

        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while obtaining player data", ex);
        }
    }

    /**
     * Get a list of effective names of all the online players on the server.
     * An effective name is a player's nickname if they have one, otherwise their username.
     *
     * @return a list of effective names of all players on the server
     */
    public static @NotNull Collection<String> getOnlinePlayerNames() {
        final PlayerManager playerManager = ServerInstance.get().getPlayerManager();

        // in order to only get online players, get the list of all online players
        // and create a template string with the same number of question marks
        // as online players. These will be replaced by the usernames in the SQL query.
        int playerCount = playerManager.getCurrentPlayerCount();
        String[] playerNamesArray = playerManager.getPlayerNames();
        String playerNamesTemplate = ",?".repeat(playerCount).substring(1);

        // as an example, if the players 'Koenn' and 'KallumJ' are on the server,
        // the playerNamesTemplate will become '?, ?' and the final SQL query will
        // look like "SELECT ... WHERE username IN ('Koenn', 'KallumJ');

        final Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            // we use the COALESCE function just like above to get
            // a player's effective name.
            PreparedStatement getNamesStatement = QueryHelper.prepareStatement(
                    databaseConnection,
                    String.format("SELECT COALESCE(nickname, username) AS name FROM player_data WHERE username IN (%s)", playerNamesTemplate),
                    Arrays.stream(playerNamesArray)
                            .map(DataTypes.STRING::create)
                            .collect(Collectors.toUnmodifiableList())
            );

            ResultSet resultSet = getNamesStatement.executeQuery();

            // convert the result set into a list
            Collection<String> names = new LinkedList<>();
            while (resultSet.next()) {
                names.add(resultSet.getString(1));
            }

            return Collections.unmodifiableCollection(names);

        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while obtaining player data", ex);
        }
    }

    /**
     * Get the effective name of a player.
     * An effective name is a player's nickname if they have one, otherwise their username.
     *
     * @param player the player to get the name for
     * @return the effective name of that player
     */
    public static @NotNull String getEffectiveName(@NotNull ServerPlayerEntity player) {
        final Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            // we use the COALESCE function just like above to get
            // a player's effective name.
            PreparedStatement getEffectiveNameStatement = QueryHelper.prepareStatement(
                    databaseConnection,
                    "SELECT COALESCE(nickname, username) AS name FROM player_data WHERE uuid=(SELECT id FROM uuid WHERE uuid=?)",
                    Collections.singleton(
                            DataTypes.STRING.create(player.getUuidAsString())
                    )
            );

            // if there is any result, return the first value
            ResultSet resultSet = getEffectiveNameStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);

            } else {
                // return the player's username if there are no results
                return player.getName().getString();
            }

        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while obtaining player name", ex);
        }
    }

    /**
     * Check if a player has VIP status.
     *
     * @param player the player to check
     * @return true if the player has VIP status
     */
    public static boolean isVIP(@NotNull ServerPlayerEntity player) {
        final Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            // We select the vip column for the given player, but only
            // if the value of vip is true. That way if there is any result
            // at all, we know the player is a VIP
            PreparedStatement isVIPStatement = QueryHelper.prepareStatement(
                    databaseConnection,
                    "SELECT vip FROM player_data WHERE uuid=(SELECT id FROM uuid WHERE uuid=?) AND vip=1",
                    Collections.singleton(
                            DataTypes.STRING.create(player.getUuidAsString())
                    )
            );

            // return true if the query gave any results
            return isVIPStatement.executeQuery().next();

        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while obtaining player name", ex);
        }
    }

    /**
     * Get a player uuid from their nickname or username.
     * The player does not have to be online.
     *
     * @param name a nickname or username of a player
     * @return the uuid of that player (if found)
     */
    public static Optional<UUID> nameToUUID(@NotNull String name) {
        final Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            // select the uuid from any player which matches the nickname or the username
            PreparedStatement getPlayerDataStatement = QueryHelper.prepareStatement(
                    databaseConnection,
                    "SELECT uuid FROM player_data WHERE nickname=? OR username=?",
                    Arrays.asList(
                            DataTypes.STRING.create(name),
                            DataTypes.STRING.create(name)
                    )
            );

            ResultSet resultSet = getPlayerDataStatement.executeQuery();
            if (resultSet.next()) {

                // convert the uuid ID into an actual uuid
                int uuidID = resultSet.getInt(1);
                return Optional.of(UUIDHelper.getUUID(uuidID, databaseConnection));

            } else {
                return Optional.empty();
            }

        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while obtaining player data", ex);
        }
    }

    /**
     * Get a player object from their nickname or username.
     * Note that the player must be online.
     *
     * @param name a nickname or username of a player
     */
    public static Optional<ServerPlayerEntity> getPlayer(@NotNull String name) {
        PlayerManager playerManager = ServerInstance.get().getPlayerManager();

        // check if we can find a player by that username
        ServerPlayerEntity player = playerManager.getPlayer(name);
        if (player != null) {
            return Optional.of(player);

        } else {
            // if not, look up their uuid in the database and
            // convert the result to a player object
            return nameToUUID(name).map(playerManager::getPlayer);
        }
    }

    /**
     * This function will update a player's username in the database.
     * Usually this will do nothing, but it handles player name changes.
     */
    public static void updatePlayerUsername(@NotNull ServerPlayerEntity player) {
        PlayerDataHandle dataHandle = PlayerDataHandle.get(player);
        LOGGER.info(String.format("Player '%s' updated in database", dataHandle.getEffectiveName()));
        dataHandle.save();
    }
}
