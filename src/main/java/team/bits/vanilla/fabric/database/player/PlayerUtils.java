package team.bits.vanilla.fabric.database.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Connection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bits.nibbles.utils.ServerInstance;
import team.bits.servicelib.client.GraphQLRPCClient;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class PlayerUtils {

    private static GraphQLRPCClient rpcClient;

    private PlayerUtils() {
    }

    public static void init(@NotNull Connection connection) {
        try {
            rpcClient = new GraphQLRPCClient(connection, "player-api");
        } catch (IOException ex) {
            throw new RuntimeException("Error while creating RPC client", ex);
        }
    }

    /**
     * Returns the username of a player with a given nickname. Result will be
     * empty if no player with the given nickname can be found.
     *
     * @param nickname the nickname of the player
     * @return the username of the player
     */
    public static @NotNull Optional<String> getUsername(@NotNull String nickname) {
        JsonObject response;
        try {
            response = rpcClient.call(
                    "query($name: String!) { player(name: $name) { username } }",
                    Map.of("name", nickname)
            ).get();
        } catch (InterruptedException | ExecutionException | IOException ex) {
            throw new RuntimeException("Error while getting player data", ex);
        }

        if (response.size() > 0) {
            return Optional.of(response.getAsJsonObject("player").get("username").getAsString());
        }

        return Optional.empty();
    }

    /**
     * Get a list of nicknames of all the players that played on the server and have a nickname.
     *
     * @return a list of nicknames of all players that have one
     */
    public static @NotNull CompletableFuture<Collection<String>> getNicknamesAsync() {
        try {
            return rpcClient.call(
                            "query { players { nickname } }",
                            Map.of()
                    )
                    .thenApply(PlayerUtils::readEffectiveNames);
        } catch (IOException ex) {
            throw new RuntimeException("Error while getting player data", ex);
        }
    }

    /**
     * Get a list of effective names of all the players that played on the server.
     * An effective name is a player's nickname if they have one, otherwise their username.
     *
     * @return a list of effective names of all players that have ever played
     */
    public static @NotNull CompletableFuture<Collection<String>> getAllNamesAsync() {
        try {
            return rpcClient.call(
                            "query { players { nickname, username } }",
                            Map.of()
                    )
                    .thenApply(PlayerUtils::readEffectiveNames);
        } catch (IOException ex) {
            throw new RuntimeException("Error while getting player data", ex);
        }
    }

    /**
     * Get a list of effective names of all the online players on the server.
     * An effective name is a player's nickname if they have one, otherwise their username.
     *
     * @return a list of effective names of all players on the server
     */
    public static @NotNull CompletableFuture<Collection<String>> getOnlinePlayerNamesAsync() {
        final PlayerManager playerManager = ServerInstance.get().getPlayerManager();

        try {
            return rpcClient.call(
                            "query($names: [String!]!) { players(usernames: $names) { nickname, username } }",
                            Map.of("names", Arrays.asList(playerManager.getPlayerNames()))
                    )
                    .thenApply(PlayerUtils::readEffectiveNames);
        } catch (IOException ex) {
            throw new RuntimeException("Error while getting player data", ex);
        }
    }

    private static @NotNull Collection<String> readEffectiveNames(@NotNull JsonObject response) {
        if (response.size() > 0) {
            Collection<String> nicknames = new LinkedList<>();
            JsonArray players = response.getAsJsonArray("players");
            for (JsonElement player : players) {
                JsonObject playerObject = player.getAsJsonObject();
                if (playerObject.has("nickname")) {
                    nicknames.add(playerObject.get("nickname").getAsString());
                } else {
                    nicknames.add(playerObject.get("username").getAsString());
                }
            }
            return Collections.unmodifiableCollection(nicknames);
        }

        return Collections.emptySet();
    }

    /**
     * Get the effective name of a player.
     * An effective name is a player's nickname if they have one, otherwise their username.
     *
     * @param player the player to get the name for
     * @return the effective name of that player
     */
    public static @NotNull String getEffectiveName(@NotNull ServerPlayerEntity player) {
        JsonObject response;
        try {
            response = rpcClient.call(
                    "query($uuid: ID!) { player(uuid: $uuid) { nickname, username } }",
                    Map.of("uuid", player.getUuidAsString())
            ).get();
        } catch (InterruptedException | ExecutionException | IOException ex) {
            throw new RuntimeException("Error while getting player data", ex);
        }

        if (response.size() > 0) {
            JsonObject playerObject = response.getAsJsonObject("player");
            if (playerObject.has("nickname")) {
                return playerObject.get("nickname").getAsString();
            } else {
                return playerObject.get("username").getAsString();
            }
        }

        return player.getName().getString();
    }

    /**
     * Check if a player has VIP status.
     *
     * @param player the player to check
     * @return true if the player has VIP status
     */
    public static boolean isVIP(@NotNull ServerPlayerEntity player) {
        JsonObject response;
        try {
            response = rpcClient.call(
                    "query($uuid: ID!) { player(uuid: $uuid) { vip } }",
                    Map.of("uuid", player.getUuidAsString())
            ).get();
        } catch (InterruptedException | ExecutionException | IOException ex) {
            throw new RuntimeException("Error while getting player data", ex);
        }

        if (response.size() > 0) {
            return response.getAsJsonObject("player").get("vip").getAsBoolean();
        }

        return false;
    }

    public static boolean hasTPDisabled(@NotNull ServerPlayerEntity player) {
        JsonObject response;
        try {
            response = rpcClient.call(
                    "query($uuid: ID!) { player(uuid: $uuid) { no_tp, vip } }",
                    Map.of("uuid", player.getUuidAsString())
            ).get();
        } catch (InterruptedException | ExecutionException | IOException ex) {
            throw new RuntimeException("Error while getting player data", ex);
        }

        if (response.size() > 0) {
            return response.getAsJsonObject("player").get("no_tp").getAsBoolean();
        }

        return false;
    }

    /**
     * Get a player uuid from their nickname or username.
     * The player does not have to be online.
     *
     * @param name a nickname or username of a player
     * @return the uuid of that player (if found)
     */
    public static Optional<UUID> nameToUUID(@NotNull String name) {
        JsonObject response;
        try {
            response = rpcClient.call(
                    "query($name: String!) { player(name: $name) { uuid } }",
                    Map.of("name", name)
            ).get();
        } catch (InterruptedException | ExecutionException | IOException ex) {
            throw new RuntimeException("Error while getting player data", ex);
        }

        if (response.size() > 0) {
            String uuidString = response.getAsJsonObject("player").get("uuid").getAsString();
            return Optional.of(UUID.fromString(uuidString));
        }

        return Optional.empty();
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
     * Get the data needed in order to format a player's name (username, nickname, color)
     *
     * @param player the player to get the name data for
     * @return username, nickname and color for the player
     */
    public static @NotNull CompletableFuture<Optional<PlayerNameLoader.PlayerNameData>> getNameDataAsync(@NotNull ServerPlayerEntity player) {
        try {
            // make an RPC to query a player's username, nickname, and color
            return rpcClient.call(
                            "query($uuid: ID!) { player(uuid: $uuid) { username, nickname, color } }",
                            Map.of("uuid", player.getUuidAsString())
                    )
                    // when the response is received, put the data into a record for ease of use
                    .thenApply(response -> {
                        if (response.size() > 0) { // only do this if we actually have data
                            JsonObject playerData = response.getAsJsonObject("player");
                            return Optional.of(new PlayerNameLoader.PlayerNameData(
                                    playerData.get("username").getAsString(),
                                    // nickname and color are both optional and may be null
                                    playerData.has("nickname") ? playerData.get("nickname").getAsString() : null,
                                    playerData.has("color") ? playerData.get("color").getAsInt() : null
                            ));
                        }

                        return Optional.empty();
                    });
        } catch (IOException ex) {
            throw new RuntimeException("Error while getting player data", ex);
        }
    }

    /**
     * This function will update a player's username in the database.
     * Usually this will do nothing, but it handles player name changes.
     */
    public static void updatePlayerUsername(@NotNull ServerPlayerEntity player) {
        updatePlayer(player, Map.of("username", player.getName().getString()));
    }

    public static void setColor(@NotNull ServerPlayerEntity player, @NotNull Color color) {
        int rgb = (color.getRed() << 16) | (color.getGreen() << 8) | (color.getBlue());

        updatePlayer(player, Map.of("color", rgb));
    }

    public static void setNickname(@NotNull ServerPlayerEntity player, @Nullable String nickname) {
        if (nickname == null) {
            nickname = "";
        }

        updatePlayer(player, Map.of("nickname", nickname));
    }

    public static void setVIP(@NotNull ServerPlayerEntity player, boolean vip) {
        updatePlayer(player, Map.of("vip", vip));
    }

    public static void setNoTP(@NotNull ServerPlayerEntity player, boolean noTP) {
        updatePlayer(player, Map.of("no_tp", noTP));
    }

    private static void updatePlayer(@NotNull ServerPlayerEntity player, @NotNull Map playerData) {
        try {
            rpcClient.call(
                    "mutation($uuid: ID!, $player: PlayerInput!) { player(uuid: $uuid, player: $player) { uuid } }",
                    Map.of(
                            "uuid", player.getUuidAsString(),
                            "player", playerData
                    )
            );
        } catch (IOException ex) {
            throw new RuntimeException("Error while updating player data", ex);
        }
    }
}
