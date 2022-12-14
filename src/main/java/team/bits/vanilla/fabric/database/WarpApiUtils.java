package team.bits.vanilla.fabric.database;

import com.google.gson.*;
import com.rabbitmq.client.*;
import net.minecraft.server.dedicated.*;
import net.minecraft.server.world.*;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.teleport.*;
import team.bits.nibbles.utils.*;
import team.bits.servicelib.client.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public final class WarpApiUtils {

    private static GraphQLRPCClient rpcClient;

    private WarpApiUtils() {
    }

    public static void init(@NotNull Connection connection) {
        try {
            rpcClient = new GraphQLRPCClient(connection, "warp-api");
        } catch (IOException ex) {
            throw new RuntimeException("Error while creating RPC client", ex);
        }
    }

    public static @NotNull CompletableFuture<Collection<String>> getWarpsListAsync() {
        try {
            return rpcClient.call(
                            "query($server: String!) { warps(server: $server) { name } }",
                            Map.of("server", ServerUtils.getServerName())
                    )
                    .thenApply(response -> response.getAsJsonArray("warps"))
                    .thenApply(WarpApiUtils::readWarpNames);
        } catch (IOException ex) {
            throw new RuntimeException("Error while getting player data", ex);
        }
    }

    public static @NotNull CompletableFuture<Optional<Warp>> getWarpAsync(@NotNull String name) {
        try {
            return rpcClient.call("""
                                    query($server: String!, $name: String!) {
                                        warp(server: $server, name: $name) { name, location { world, x, y, z } }
                                    }
                                    """,
                            Map.of(
                                    "server", ServerUtils.getServerName(),
                                    "name", name
                            )
                    )
                    .thenApply(response -> Optional.ofNullable(response.getAsJsonObject("warp")))
                    .thenApply(warp -> warp.map(WarpApiUtils::readWarp));
        } catch (IOException ex) {
            throw new RuntimeException("Error while getting warp data", ex);
        }
    }

    public static @NotNull CompletableFuture<Boolean> addWarpAsync(@NotNull Warp warp) {
        final Location location = warp.location();
        final Vec3d pos = location.position();
        final World world = location.world();

        try {
            return rpcClient.call("""
                                    mutation($server: String!, $name: String!, $location: LocationInput!) {
                                       addWarp(server: $server, name: $name, location: $location) { name }
                                    }
                                    """,
                            Map.of(
                                    "server", ServerUtils.getServerName(),
                                    "name", warp.name(),
                                    "location", Map.of(
                                            "world", worldKeyToName(world.getRegistryKey()),
                                            "x", (int) pos.x,
                                            "y", (int) pos.y,
                                            "z", (int) pos.z
                                    )
                            )
                    )
                    .thenApply(response -> response.has("addWarp"));
        } catch (IOException ex) {
            throw new RuntimeException("Error while adding warp", ex);
        }
    }

    public static @NotNull CompletableFuture<Boolean> deleteWarpAsync(@NotNull Warp warp) {
        try {
            return rpcClient.call("""
                                    mutation($server: String!, $name: String!) {
                                       deleteWarp(server: $server, name: $name)
                                    }
                                    """,
                            Map.of(
                                    "server", ServerUtils.getServerName(),
                                    "name", warp.name()
                            )
                    )
                    .thenApply(response -> true);
        } catch (IOException ex) {
            throw new RuntimeException("Error while deleting warp", ex);
        }
    }

    private static @NotNull Collection<String> readWarpNames(@NotNull JsonArray json) {
        Collection<String> warps = new LinkedList<>();
        for (JsonElement warp : json) {
            warps.add(warp.getAsJsonObject().get("name").getAsString());
        }
        return warps;
    }

    private static @NotNull Warp readWarp(@NotNull JsonObject json) {
        final MinecraftDedicatedServer server = ServerInstance.get();

        String warpName = json.get("name").getAsString();
        JsonObject locationJson = json.getAsJsonObject("location");
        String worldName = locationJson.get("world").getAsString();
        int x = locationJson.get("x").getAsInt();
        int y = locationJson.get("y").getAsInt();
        int z = locationJson.get("z").getAsInt();

        ServerWorld world = server.getWorld(nameToWorldKey(worldName));
        Location location = new Location(new Vec3d(x + 0.5f, y + 0.5f, z + 0.5f), world);

        return new Warp(warpName, location);
    }

    public static @NotNull RegistryKey<World> nameToWorldKey(@NotNull String name) {
        return switch (name.toLowerCase()) {
            case "world" -> World.OVERWORLD;
            case "world_nether" -> World.NETHER;
            case "world_the_end" -> World.END;
            default -> throw new IllegalArgumentException("Cannot find world " + name);
        };
    }

    public static @NotNull String worldKeyToName(@NotNull RegistryKey<World> key) {
        if (key.equals(World.OVERWORLD)) {
            return "world";
        } else if (key.equals(World.NETHER)) {
            return "world_nether";
        } else if (key.equals(World.END)) {
            return "world_the_end";
        }
        throw new IllegalArgumentException("Unknown world " + key);
    }
}
