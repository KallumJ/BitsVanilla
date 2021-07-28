package team.bits.vanilla.fabric.database.util;

import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.utils.PropertiesFileUtils;

import java.io.File;
import java.sql.*;
import java.util.Objects;
import java.util.Properties;

public class ServerUtils {

    public static @NotNull String getServerName() {
        Properties properties = PropertiesFileUtils.loadFromFile(new File("server.properties"));
        if (properties.containsKey("server-name")) {
            return Objects.requireNonNull(properties.getProperty("server-name"));
        }
        throw new NullPointerException("Unable to find server-name property");
    }

    public static int getServerID(@NotNull Connection databaseConnection) {
        String serverName = getServerName();
        try {
            String query = "SELECT id FROM server WHERE name=?";
            PreparedStatement statement = databaseConnection.prepareStatement(query);
            statement.setString(1, serverName);
            ResultSet serverResult = statement.executeQuery();

            if (serverResult.next()) {
                return serverResult.getInt(1);
            } else {
                return createNewServerID(serverName, databaseConnection);
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Unable to obtain server ID", ex);
        }
    }

    private static int createNewServerID(@NotNull String serverName, @NotNull Connection databaseConnection) throws SQLException {
        String query = "INSERT INTO server (name) VALUES (?)";
        PreparedStatement statement = databaseConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, serverName);
        statement.executeUpdate();
        return statement.getGeneratedKeys().getInt(1);
    }
}
