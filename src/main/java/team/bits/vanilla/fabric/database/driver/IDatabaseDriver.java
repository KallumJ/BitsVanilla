package team.bits.vanilla.fabric.database.driver;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public interface IDatabaseDriver {

    void open() throws DatabaseDriverException;

    void close() throws DatabaseDriverException;

    @NotNull Connection getConnection();
}
