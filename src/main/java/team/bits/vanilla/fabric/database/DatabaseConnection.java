package team.bits.vanilla.fabric.database;

import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.database.driver.DatabaseDriverFactory;
import team.bits.nibbles.database.driver.IDatabaseDriver;
import team.bits.nibbles.utils.PropertiesFileUtils;
import team.bits.vanilla.fabric.database.util.ServerUtils;

import java.sql.Connection;
import java.util.Objects;
import java.util.Properties;

public final class DatabaseConnection {

    private static IDatabaseDriver driver;

    public static void open() {
        Properties properties = PropertiesFileUtils.loadFromClasspath("database.properties");
        driver = DatabaseDriverFactory.create(properties);
        driver.open();

        ServerUtils.getServerID(getConnection());
    }

    public static @NotNull Connection getConnection() {
        return Objects.requireNonNull(driver.getConnection());
    }

    public static void close() {
        if (driver != null) {
            driver.close();
        }
    }
}
