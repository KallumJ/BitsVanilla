package team.bits.vanilla.fabric.database.player;

import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.database.util.QueryHelper;
import team.bits.vanilla.fabric.database.util.model.DataTypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.UUID;

public final class UUIDHelper {

    private UUIDHelper() {
    }

    public static int getUUIDId(@NotNull UUID uuid, @NotNull Connection databaseConnection) throws SQLException {
        PreparedStatement getUUIDStatement = QueryHelper.prepareStatement(
                databaseConnection,
                "SELECT id FROM uuid WHERE uuid=?",
                Collections.singleton(
                        DataTypes.STRING.create(uuid.toString())
                )
        );

        ResultSet uuidResult = getUUIDStatement.executeQuery();
        if (uuidResult.next()) {
            return uuidResult.getInt("id");
        } else {
            PreparedStatement storeUUIDStatement = QueryHelper.prepareStatement(
                    databaseConnection,
                    "INSERT INTO uuid (uuid) VALUES (?)",
                    Collections.singleton(
                            DataTypes.STRING.create(uuid.toString())
                    )
            );

            return QueryHelper.executeUpdateQuery(storeUUIDStatement);
        }
    }

    public static @NotNull UUID getUUID(int uuidID, @NotNull Connection databaseConnection) throws SQLException {
        PreparedStatement getUUIDStatement = QueryHelper.prepareStatement(
                databaseConnection,
                "SELECT uuid FROM uuid WHERE id=?",
                Collections.singleton(
                        DataTypes.INTEGER.create(uuidID)
                )
        );

        ResultSet uuidResult = getUUIDStatement.executeQuery();
        if (uuidResult.next()) {
            return UUID.fromString(uuidResult.getString("uuid"));
        } else {
            throw new NullPointerException(String.format("Cannot find UUID with ID %s", uuidID));
        }

    }
}
