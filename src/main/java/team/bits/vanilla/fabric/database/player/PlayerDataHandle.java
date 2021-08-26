package team.bits.vanilla.fabric.database.player;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bits.nibbles.utils.MojangApiUtils;
import team.bits.nibbles.utils.ServerInstance;
import team.bits.vanilla.fabric.database.DatabaseConnection;
import team.bits.vanilla.fabric.database.util.QueryHelper;
import team.bits.vanilla.fabric.database.util.model.DataTypes;
import team.bits.vanilla.fabric.util.Utils;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class PlayerDataHandle {

    private final UUID playerUUID;

    private int uuidID = -1;

    private String nickname;
    private boolean vip;
    private Color colour;

    private PlayerDataHandle(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public @NotNull UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public @Nullable String getNickname() {
        return this.nickname;
    }

    public void setNickname(@Nullable String nickname) {
        this.nickname = nickname;
    }

    public boolean isVIP() {
        return this.vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }

    public @NotNull Color getColour() {
        return this.colour != null ? this.colour : Color.WHITE;
    }

    public void setColour(@Nullable Color colour) {
        this.colour = colour;
    }

    public @NotNull String getUsername() {
        PlayerManager playerManager = ServerInstance.get().getPlayerManager();
        ServerPlayerEntity player = playerManager.getPlayer(this.playerUUID);
        if (player != null) {
            return player.getName().getString();
        } else {
            return MojangApiUtils.getUsernameFromUUID(this.playerUUID);
        }
    }

    public @NotNull String getEffectiveName() {
        return Objects.requireNonNullElseGet(this.nickname, this::getUsername);
    }

    public void load() {
        Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            this.loadUUID();

            PreparedStatement getPlayerDataStatement = QueryHelper.prepareStatement(
                    databaseConnection,
                    "SELECT * FROM player_data WHERE uuid=?",
                    Collections.singleton(
                            DataTypes.INTEGER.create(this.uuidID)
                    )
            );

            ResultSet resultSet = getPlayerDataStatement.executeQuery();
            if (resultSet.next()) {
                this.nickname = resultSet.getString("nickname");
                this.vip = resultSet.getBoolean("vip");

                int result = resultSet.getInt("colour");
                this.colour = new Color(result);
                if (resultSet.wasNull()) {
                    this.colour = null;
                }
            } else {
                this.nickname = null;
                this.vip = false;
                this.colour = null;

                this.insert();
            }

        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while obtaining player data", ex);
        }

        this.loadCustomName();
    }

    private void insert() {
        Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            this.loadUUID();

            PreparedStatement insertStatement = QueryHelper.prepareStatement(
                    databaseConnection,
                    "INSERT INTO player_data (uuid, username) VALUES (?, ?)",
                    Arrays.asList(
                            DataTypes.INTEGER.create(uuidID),
                            DataTypes.STRING.create(this.getUsername())
                    )
            );
            insertStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while inserting player data", ex);
        }

        this.loadCustomName();
    }

    public void save() {
        Connection databaseConnection = DatabaseConnection.getConnection();
        try {
            this.loadUUID();

            PreparedStatement updateStatement = QueryHelper.prepareStatement(
                    databaseConnection,
                    "UPDATE player_data SET username=?,nickname=?,vip=?,colour=? WHERE uuid=?",
                    Arrays.asList(
                            DataTypes.STRING.create(this.getUsername()),
                            DataTypes.STRING.create(this.nickname),
                            DataTypes.BOOLEAN.create(this.vip),
                            DataTypes.INTEGER.create(this.colour != null ? this.getColorRGB() : null),
                            DataTypes.INTEGER.create(uuidID)
                    )
            );
            updateStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("SQLException while obtaining player data", ex);
        }

        this.loadCustomName();
    }

    public @Nullable Text getFormattedNickname() {
        return new LiteralText(this.nickname).styled(style -> style.withColor(this.getColorRGB()));
    }

    public @NotNull Text getFormattedUsername() {
        return new LiteralText(this.getUsername()).styled(style -> style.withColor(this.getColorRGB()));
    }

    public void loadCustomName(ServerPlayerEntity player) {
        if (player != null) {
            if (this.nickname != null) {
                player.setCustomName(this.getFormattedNickname());
            } else {
                player.setCustomName(this.getFormattedUsername());
            }

            Utils.updatePlayerDisplayName(player);
        }
    }

    private void loadCustomName() {
        if (this.playerUUID != null) {
            this.loadCustomName(
                    ServerInstance.get().getPlayerManager().getPlayer(this.playerUUID)
            );
        }
    }

    private void loadUUID() throws SQLException {
        if (this.uuidID == -1) {
            Connection databaseConnection = DatabaseConnection.getConnection();
            this.uuidID = UUIDHelper.getUUIDId(this.playerUUID, databaseConnection);
        }
    }

    private int getColorRGB() {
        if (this.colour != null) {
            return (this.colour.getRed() << 16) | (this.colour.getGreen() << 8) | (this.colour.getBlue());
        }
        return 0xFFFFFF;
    }

    public static @NotNull PlayerDataHandle get(@NotNull ServerPlayerEntity player) {
        return get(player.getUuid());
    }

    public static @NotNull PlayerDataHandle get(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);

        PlayerDataHandle handle = new PlayerDataHandle(uuid);
        handle.load();

        return handle;
    }
}
