package team.bits.vanilla.fabric.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesFileUtils {

    private PropertiesFileUtils() {
    }

    public static @NotNull Properties loadFromClasspath(@NotNull String path) {
        Properties properties = new Properties();
        try (InputStream input = PropertiesFileUtils.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new RuntimeException("Unable to find properties file " + path);
            }

            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to read properties file " + path, ex);
        }
        return properties;
    }

    public static @NotNull Properties loadFromFile(@NotNull File file) {
        if (!file.exists()) {
            throw new RuntimeException("Unable to find properties file " + file.getName());
        }

        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(file)) {
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to read properties file " + file.getName(), ex);
        }
        return properties;
    }
}
