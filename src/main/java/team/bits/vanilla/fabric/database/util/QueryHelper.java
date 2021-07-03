package team.bits.vanilla.fabric.database.util;

import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.database.util.model.IDataObject;

import java.sql.*;
import java.util.Collection;

public final class QueryHelper {

    private QueryHelper() {
    }

    public static @NotNull PreparedStatement prepareStatement(@NotNull Connection connection, @NotNull String rawQuery, @NotNull Collection<IDataObject> data) {
        try {
            PreparedStatement query = connection.prepareStatement(rawQuery, Statement.RETURN_GENERATED_KEYS);

            int index = 1;
            for (IDataObject object : data) {
                query.setObject(index++, object.serialize());
            }

            return query;
        } catch (SQLException ex) {
            throw new DatabaseException("Unable to prepare SQL query", ex);
        }
    }

    public static int executeUpdateQuery(@NotNull PreparedStatement query) {
        try {
            query.executeUpdate();

            try (ResultSet generatedKeys = query.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    return -1;
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Unable to execute SQL query", ex);
        }
    }
}
