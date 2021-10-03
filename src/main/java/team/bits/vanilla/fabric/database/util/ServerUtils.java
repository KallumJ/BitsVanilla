package team.bits.vanilla.fabric.database.util;

import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.utils.PropertiesFileUtils;

import java.io.File;
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
}
