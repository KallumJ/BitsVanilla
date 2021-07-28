package team.bits.vanilla.fabric.database.driver;

import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.utils.PropertiesFileUtils;
import team.bits.vanilla.fabric.database.util.ServerUtils;

import java.sql.Connection;
import java.util.Objects;
import java.util.Properties;

public final class DatabaseConnection {

    private static IDatabaseDriver driver;

    public static void open() {
        Properties properties = PropertiesFileUtils.loadFromClasspath("database.properties");
        driver = new DatabaseDriver(new DatabaseProperties(properties));
        driver.open();

        ServerUtils.getServerID(getConnection());
    }

    public static @NotNull IDatabaseDriver getDriver() {
        return driver;
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
