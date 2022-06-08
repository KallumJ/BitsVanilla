package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.entity.*;
import net.minecraft.server.*;
import net.minecraft.server.command.*;
import net.minecraft.server.world.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.*;
import net.minecraft.world.*;
import team.bits.nibbles.command.*;

import java.util.*;
import java.util.concurrent.*;

public class ChunkInspectCommand extends Command {

    private static final String ENTITY_RECORD_STRING = "%s at %s, count %d";
    private static final String TELEPORT_COMMAND = "/execute in %s run tp %s %s";
    private static final int YELLOW_THRESHOLD = 15;
    private static final int RED_THRESHOLD = 25;

    private final Executor executor = Executors.newSingleThreadExecutor();

    public ChunkInspectCommand() {
        super("chunkinspect", new CommandInformation()
                .setDescription("Provides a list of large groups of entities across the server")
                .setPublic(false)
        );
    }

    private static void sendEntityList(ServerCommandSource source, List<EntityRecord> entityRecords) {
        Map<RegistryKey<World>, List<Text>> entityStrings = new HashMap<>();

        for (EntityRecord entityRecord : entityRecords) {

            // If there's enough entities in this chunk to scare us
            if (entityRecord.getCount() >= YELLOW_THRESHOLD) {

                // If there's a lot of entities, make the text red
                Formatting color = entityRecord.getCount() >= RED_THRESHOLD ? Formatting.RED : Formatting.YELLOW;

                // Gather entity information
                String entityKey = entityRecord.getEntityType().getTranslationKey();
                RegistryKey<World> dimension = entityRecord.getDimension();
                ChunkPos entityChunkPos = entityRecord.getChunkPos();
                int entityCount = entityRecord.getCount();

                // Add it to text component
                Text textComponent = Text.literal(
                                String.format(ENTITY_RECORD_STRING,
                                        getEntityStringFromKey(entityKey),
                                        entityChunkPos,
                                        entityCount
                                )
                        ).styled(style -> style
                                .withColor(color)
                                .withClickEvent(
                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(TELEPORT_COMMAND,
                                                dimension.getValue(),
                                                source.getName(),
                                                getCommandLocationString(entityChunkPos)
                                        ))
                                )
                        )
                        .append(Text.literal("\n"));


                // Add text to relevant dimension list
                entityStrings.computeIfAbsent(dimension, k -> new LinkedList<>());
                entityStrings.get(dimension).add(textComponent);
            }
        }

        // Create text components for each dimension, with all entity records in it
        List<Text> finalMessages = new LinkedList<>();

        entityStrings.forEach((worldRegistryKey, textComponents) -> {
            MutableText message = Text.literal("---" + worldRegistryKey.getValue() + "---")
                    .styled(style -> style.withColor(Formatting.GREEN))
                    .append(Text.literal("\n"));
            if (!textComponents.isEmpty()) {
                textComponents.forEach(message::append);
            } else {
                message.append(Text.literal("No entities found")
                        .styled(style -> style.withColor(Formatting.GRAY))
                        .append(Text.literal("\n")));
            }

            finalMessages.add(message);
        });


        // Collate the per dimension information into one text component
        if (finalMessages.isEmpty()) {
            finalMessages.add(Text.literal("No problems to report")
                    .styled(style -> style.withColor(Formatting.RED))
            );
        }
        MutableText collatedMessage = Text.empty();
        finalMessages.forEach(collatedMessage::append);

        // Send the message
        source.sendFeedback(collatedMessage, false);
    }

    // Get actual co ordinates from chunk pos, default to y 100;
    private static String getCommandLocationString(ChunkPos chunkPos) {
        return chunkPos.getCenterX() + " 100 " + chunkPos.getCenterZ();
    }

    // remove the namespace information from entity key
    private static String getEntityStringFromKey(String key) {
        return key.split("\\.")[key.split("\\.").length - 1];
    }

    private static Optional<EntityRecord> findMatchingEntityRecord(EntityType<?> entityType, ChunkPos chunkPos, List<EntityRecord> entityRecords) {
        for (EntityRecord entityRecord : entityRecords) {
            if (entityRecord.getEntityType().equals(entityType) && entityRecord.getChunkPos().equals(chunkPos)) {
                return Optional.of(entityRecord);
            }
        }
        return Optional.empty();
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final List<EntityRecord> entityRecords = new LinkedList<>();

        final MinecraftServer server = context.getSource().getServer();

        final Set<RegistryKey<World>> worldRegKeys = server.getWorldRegistryKeys();

        // execute the rest of this command on a separate thread
        this.executor.execute(() -> {

            // For every dimension on the server
            for (RegistryKey<World> worldRegKey : worldRegKeys) {
                ServerWorld world = Objects.requireNonNull(server.getWorld(worldRegKey));

                // Iterate over every loaded entity
                world.iterateEntities().forEach(entity -> {
                    ChunkPos entityChunkPos = entity.getChunkPos();
                    EntityType<?> entityType = entity.getType();

                    // See if we have already recorded an entity of this type in this chunk...
                    Optional<EntityRecord> matchingEntityRecord = findMatchingEntityRecord(entityType, entityChunkPos, entityRecords);

                    // ...if this we have, increment its count...
                    if (matchingEntityRecord.isPresent()) {
                        matchingEntityRecord.get().incrementCount();

                        //...if we haven't, create a new statRecord
                    } else {
                        entityRecords.add(new EntityRecord(entityType, entityChunkPos, worldRegKey));
                    }
                });
            }


            // Send the records to the player
            sendEntityList(context.getSource(), entityRecords);
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
