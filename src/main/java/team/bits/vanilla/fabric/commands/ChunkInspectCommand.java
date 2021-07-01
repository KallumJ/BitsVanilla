package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import team.bits.vanilla.fabric.BitsVanilla;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChunkInspectCommand extends Command {
    //TODO: check player is operator

    private static final String ENTITY_RECORD_STRING = "%s at %s, count %d";
    private static final String TELEPORT_COMMAND = "/execute in %s run tp %s %s";
    private static final int YELLOW_THRESHOLD = 15;
    private static final int RED_THRESHOLD = 25;

    private final Executor executor = Executors.newSingleThreadExecutor();

    public ChunkInspectCommand() {
        super("chunkinspect", new String[]{"ci"}, new CommandHelpInformation()
                .setDescription("Provides a list of large groups of entities across the server")
                .setPublic(false)
        );
    }

    private static void sendEntityList(ServerPlayerEntity player, List<EntityRecord> entityRecords) {
        Map<RegistryKey<World>, List<TextComponent>> entityStrings = new HashMap<>();

        for (EntityRecord entityRecord : entityRecords) {

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
                entityStrings.computeIfAbsent(dimension, k -> new ArrayList<>());
                entityStrings.get(dimension).add(textComponent);
            }
        }

        // Create text components for each dimension, with all entity records in it
        List<TextComponent> finalMessages = new ArrayList<>();

        entityStrings.forEach((worldRegistryKey, textComponents) -> {
            TextComponent message = Component.text("---" + worldRegistryKey.getValue() + "--- \n").color(NamedTextColor.GREEN);
            if (!textComponents.isEmpty()) {
                message = message.append(Component.text().append(textComponents));
            } else {
                message = message.append(Component.text("No entities found \n").color(NamedTextColor.GRAY));
            }

            finalMessages.add(message);
        });


        // Collate the per dimension information into one text component
        TextComponent collatedMessage = Component.text().append(finalMessages).build();

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

    private static EntityRecord findMatchingEntityRecord(EntityType<?> entityType, ChunkPos chunkPos, List<EntityRecord> entityRecords) {
        for (EntityRecord entityRecord : entityRecords) {
            if (entityRecord.getEntityType().equals(entityType) && entityRecord.getChunkPos().equals(chunkPos)) {
                return entityRecord;
            }
        }
        return null;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final List<EntityRecord> entityRecords = new ArrayList<>();

        final MinecraftServer server = context.getSource().getMinecraftServer();
        final ServerPlayerEntity player = context.getSource().getPlayer();

        final Set<RegistryKey<World>> worldRegKeys = server.getWorldRegistryKeys();

        // execute the rest of this command on a separate thread
        this.executor.execute(() -> {

            // For every dimension on the server
            for (RegistryKey<World> worldRegKey : worldRegKeys) {
                ServerWorld world = server.getWorld(worldRegKey);

                // Iterate over every loaded entity
                world.iterateEntities().forEach(entity -> {
                    ChunkPos entityChunkPos = entity.getChunkPos();
                    EntityType<?> entityType = entity.getType();

                    // See if we have already recorded an entity of this type in this chunk...
                    EntityRecord matchingEntityRecord = findMatchingEntityRecord(entityType, entityChunkPos, entityRecords);

                    // ...if this we have, increment its count...
                    if (matchingEntityRecord != null) {
                        matchingEntityRecord.incrementCount();

                        //...if we haven't, create a new record
                    } else {
                        entityRecords.add(new EntityRecord(entityType, entityChunkPos, worldRegKey));
                    }
                });
            }


            // Send the records to the player
            sendEntityList(player, entityRecords);
        });

        return 1;
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
