package team.bits.vanilla.fabric.database.warp;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.database.driver.DatabaseConnection;
import team.bits.vanilla.fabric.database.util.QueryHelper;
import team.bits.vanilla.fabric.database.util.ServerUtils;
import team.bits.vanilla.fabric.database.util.model.DataTypes;
import team.bits.vanilla.fabric.util.Location;
import team.bits.vanilla.fabric.util.ServerInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public final class WarpUtils {

    private WarpUtils() {
    }

    public static @NotNull Collection<String> getWarpsList() {
        final Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            PreparedStatement getWarpsStatement = QueryHelper.prepareStatement(
                    databaseConnection,
                    "SELECT name FROM warp WHERE server=(SELECT id FROM server WHERE name=?)",
                    Collections.singleton(
                            DataTypes.STRING.create(ServerUtils.getServerName())
                    )
            );

            ResultSet resultSet = getWarpsStatement.executeQuery();

            Collection<String> names = new LinkedList<>();
            while (resultSet.next()) {
                names.add(resultSet.getString(1));
            }

            return Collections.unmodifiableCollection(names);

        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while getting warps list", ex);
        }
    }

    public static @NotNull Optional<Warp> getWarp(@NotNull String name) {
        final MinecraftDedicatedServer server = ServerInstance.get();
        final Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            PreparedStatement getWarpStatement = QueryHelper.prepareStatement(
                    databaseConnection,
                    "SELECT world, x, y, z FROM warp WHERE server=(SELECT id FROM server WHERE name=?) AND name=?",
                    Arrays.asList(
                            DataTypes.STRING.create(ServerUtils.getServerName()),
                            DataTypes.STRING.create(name)
                    )
            );

            ResultSet resultSet = getWarpStatement.executeQuery();
            if (resultSet.next()) {

                String worldName = resultSet.getString("world");
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");
                int z = resultSet.getInt("z");

                ServerWorld world = server.getWorld(nameToWorldKey(worldName));
                Location location = new Location(new Vec3d(x + 0.5f, y + 0.5f, z + 0.5f), world);

                return Optional.of(new Warp(name, location));

            } else {
                return Optional.empty();
            }

        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while getting warp", ex);
        }
    }

    public static void addWarp(@NotNull Warp warp) {
        final Location location = warp.location();
        final Vec3d pos = location.position();
        final World world = location.world();

        final Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            PreparedStatement addWarpStatement = QueryHelper.prepareStatement(
                    databaseConnection,
                    "INSERT INTO warp (name, world, x, y, z, server) VALUES (?, ?, ?, ?, ?, (SELECT id FROM server WHERE name=?))",
                    Arrays.asList(
                            DataTypes.STRING.create(warp.name()),
                            DataTypes.STRING.create(worldKeyToName(world.getRegistryKey())),
                            DataTypes.INTEGER.create((int) pos.x),
                            DataTypes.INTEGER.create((int) pos.y),
                            DataTypes.INTEGER.create((int) pos.z),
                            DataTypes.STRING.create(ServerUtils.getServerName())
                    )
            );

            addWarpStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while adding warp", ex);
        }
    }

    public static void deleteWarp(@NotNull Warp warp) {
        final Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            PreparedStatement addWarpStatement = QueryHelper.prepareStatement(
                    databaseConnection,
                    "DELETE FROM warp WHERE server=(SELECT id FROM server WHERE name=?) AND name=?",
                    Arrays.asList(
                            DataTypes.STRING.create(ServerUtils.getServerName()),
                            DataTypes.STRING.create(warp.name())
                    )
            );

            addWarpStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while adding warp", ex);
        }
    }

    private static @NotNull RegistryKey<World> nameToWorldKey(@NotNull String name) {
        return switch (name.toLowerCase()) {
            case "world" -> World.OVERWORLD;
            case "world_nether" -> World.NETHER;
            case "world_the_end" -> World.END;
            default -> throw new IllegalArgumentException("Cannot find world " + name);
        };
    }

    private static @NotNull String worldKeyToName(RegistryKey<World> key) {
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
