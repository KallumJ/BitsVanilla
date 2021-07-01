package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.apache.logging.log4j.core.jmx.Server;
import org.w3c.dom.Text;
import team.bits.vanilla.fabric.BitsVanilla;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChunkInspectCommand extends Command {
    private static final List<EntityRecord> ENTITY_RECORD_LIST = new ArrayList<>();
    private static final String ENTITY_RECORD_STRING = "%s at %s, count %d";
    private static final String TELEPORT_COMMAND = "/execute in %s run tp %s %s";
    private static final int YELLOW_THRESHOLD = 15;
    private static final int RED_THRESHOLD = 25;

    public ChunkInspectCommand() {
        super("chunkinspect", new String[]{"ci"}, new CommandHelpInformation()
            .setDescription("Provides a list of large groups of entities across the server")
            .setPublic(false)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getMinecraftServer();

        Set<RegistryKey<World>> worldRegKeys = server.getWorldRegistryKeys();

        // For every dimension on the server
        for (RegistryKey<World> worldRegKey : worldRegKeys) {
            ServerWorld world = server.getWorld(worldRegKey);

            // Iterate over every loaded entity
            world.iterateEntities().forEach(entity -> {
                ChunkPos entityChunkPos = entity.getChunkPos();
                EntityType<?> entityType = entity.getType();

                // See if we have already recorded an entity of this type in this chunk...
                EntityRecord matchingEntityRecord = findMatchingEntityRecord(entityType, entityChunkPos);

                // ...if this we have, increment its count...
                if (matchingEntityRecord != null) {
                    matchingEntityRecord.incrementCount();

                    //...if we haven't, create a new record
                } else {
                    ENTITY_RECORD_LIST.add(new EntityRecord(entityType, entityChunkPos, worldRegKey));
                }
            });
        }


        // Send the records to the player
        sendEntityList(context.getSource().getPlayer());

        ENTITY_RECORD_LIST.clear();

        return 1;
    }

    private static void sendEntityList(ServerPlayerEntity player) {
        // This solution is non ideal, but it works for now. only problem arises if we ever had more dimensions
        List<TextComponent> overworldEntityStrings = new ArrayList<>();
        List<TextComponent> netherEntityStrings = new ArrayList<>();
        List<TextComponent> endEntityStrings = new ArrayList<>();

        for (EntityRecord entityRecord : ENTITY_RECORD_LIST) {

            // If theres enough entities in this chunk to scare us
            if (entityRecord.getCount() >= YELLOW_THRESHOLD) {

                // If theres a lot of entities, make the text red
                NamedTextColor color = entityRecord.getCount() >= RED_THRESHOLD ? NamedTextColor.RED : NamedTextColor.YELLOW;

                // Gather entity information
                String entityKey = entityRecord.getEntityType().getTranslationKey();
                RegistryKey<World> dimension = entityRecord.getDimension();
                ChunkPos entityChunkPos = entityRecord.getChunkPos();
                int entityCount = entityRecord.getCount();

                // Add it to text component
                TextComponent textComponent = Component.text(String.format(ENTITY_RECORD_STRING,
                            getEntityStringFromKey(entityKey),
                            entityChunkPos,
                            entityCount) + "\n")
                        .color(color)
                        // Add command to teleport to entities on click
                        .clickEvent(ClickEvent.runCommand(String.format(TELEPORT_COMMAND,
                                dimension.getValue(),
                                player.getEntityName(),
                                getCommandLocationString(entityChunkPos)
                        )));


                // Add text to relevant dimension list
                if (dimension.equals(World.OVERWORLD)) {
                    overworldEntityStrings.add(textComponent);
                } else if (dimension.equals(World.NETHER)) {
                    netherEntityStrings.add(textComponent);
                } else if (dimension.equals(World.END)) {
                    endEntityStrings.add(textComponent);
                }

            }
        }

        // Create text components for each dimension, with all entity records in it
        TextComponent overworldMessage = Component.text("---Overworld--- \n").color(NamedTextColor.GREEN);
        if (!overworldEntityStrings.isEmpty()) {
            overworldMessage = overworldMessage.append(Component.text().append(overworldEntityStrings));
        } else {
            overworldMessage = overworldMessage.append(Component.text("No entities found \n").color(NamedTextColor.GRAY));
        }

        TextComponent netherMessage = Component.text("---Nether--- \n").color(NamedTextColor.GREEN);
        if (!netherEntityStrings.isEmpty()) {
            netherMessage = netherMessage.append(Component.text().append(netherEntityStrings));
        } else {
            netherMessage = netherMessage.append(Component.text("No entities found \n").color(NamedTextColor.GRAY));
        }

        TextComponent endMessage = Component.text("---End--- \n").color(NamedTextColor.GREEN);
        if (!endEntityStrings.isEmpty()) {
            endMessage = endMessage.append(Component.text().append(endEntityStrings));
        } else {
            endMessage = endMessage.append(Component.text("No entities found \n").color(NamedTextColor.GRAY));
        }

        // Collate the per dimension information into one text component
        TextComponent collatedMessage = Component.text().append(overworldMessage).append(netherMessage).append(endMessage).build();

        // Send the message
        BitsVanilla.adventure().audience(player).sendMessage(collatedMessage);
    }

    // Get actual co ordinates from chunk pos, default to y 100;
    private static String getCommandLocationString(ChunkPos chunkPos) {
        return chunkPos.getCenterX() + " 100 " + chunkPos.getCenterZ();
    }

    // remove the namespace information from entity key
    private static String getEntityStringFromKey(String key) {
        return key.split("\\.")[key.split("\\.").length - 1];
    }

    private static EntityRecord findMatchingEntityRecord(EntityType<?> entityType, ChunkPos chunkPos) {
        for (EntityRecord entityRecord : ENTITY_RECORD_LIST) {
            if (entityRecord.getEntityType().equals(entityType) && entityRecord.getChunkPos().equals(chunkPos)) {
                return entityRecord;
            }
        }
        return null;
    }
}

class EntityRecord {
    private final EntityType<?> entityType;
    private final ChunkPos chunkPos;
    private final RegistryKey<World> dimension;
    private int count = 1;

    public EntityRecord(EntityType<?> entityType, ChunkPos chunkPos, RegistryKey<World> dimension) {
        this.entityType = entityType;
        this.chunkPos = chunkPos;
        this.dimension = dimension;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        this.count++;
    }

    public RegistryKey<World> getDimension() {
        return dimension;
    }
}
