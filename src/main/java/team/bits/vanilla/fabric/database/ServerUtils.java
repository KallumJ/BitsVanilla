package team.bits.vanilla.fabric.database;

import org.jetbrains.annotations.*;
import team.bits.nibbles.utils.*;

import java.io.*;
import java.util.*;

public class ServerUtils {

    private ServerUtils() {
    }

    public static @NotNull String getServerName() {
        Properties properties = PropertiesFileUtils.loadFromFile(new File("server.properties"));
        if (properties.containsKey("server-name")) {
            return Objects.requireNonNull(properties.getProperty("server-name"));
        }
        throw new NullPointerException("Unable to find server-name property");
    }
}
