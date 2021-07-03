package team.bits.vanilla.fabric.database.driver;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Properties;

public class DatabaseProperties {

    private final String driverClass;
    private final String address;
    private final short port;
    private final String databaseName;
    private final String username;
    private final String password;

    public DatabaseProperties(@NotNull String driverClass, @NotNull String address, short port, @NotNull String databaseName, @NotNull String username, @NotNull String password) {
        this.driverClass = driverClass;
        this.address = Objects.requireNonNull(address);
        this.port = port;
        this.databaseName = Objects.requireNonNull(databaseName);
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
    }

    public DatabaseProperties(@NotNull Properties properties) {
        this(
                properties.getProperty("db.driver_class"),
                properties.getProperty("db.address"),
                Short.parseShort(properties.getProperty("db.port")),
                properties.getProperty("db.name"),
                properties.getProperty("db.username"),
                properties.getProperty("db.password")
        );
    }

    public @NotNull String getDriverClass() {
        return this.driverClass;
    }

    public @NotNull String getAddress() {
        return this.address;
    }

    public short getPort() {
        return this.port;
    }

    public @NotNull String getDatabaseName() {
        return this.databaseName;
    }

    public @NotNull String getUsername() {
        return this.username;
    }

    public @NotNull String getPassword() {
        return this.password;
    }
}
