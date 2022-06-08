package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.*;
import net.minecraft.server.command.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.freecam.*;

import java.util.*;

public class FreecamCommand extends Command {

    public static final Text ENTER_MSG = Text.literal("Entering freecam mode");
    public static final Text EXIT_MSG = Text.literal("Returning to body");

    private static final Map<ServerPlayerEntity, Freecam> activeFreecams = new HashMap<>();

    public FreecamCommand() {
        super("freecam", new CommandInformation()
                        .setDescription("Toggle between freecam and normal view")
                        .setPublic(true),
                "fc"
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

        if (!activeFreecams.containsKey(player)) {
            Optional<Freecam> freecam = Freecam.Factory.create(player);
            if (freecam.isPresent()) {
                activeFreecams.put(player, freecam.get());
                player.sendMessage(ENTER_MSG, MessageTypes.POSITIVE);
            }

        } else {
            activeFreecams.get(player).removeAndReturnPlayer();
            activeFreecams.remove(player);
            player.sendMessage(EXIT_MSG, MessageTypes.POSITIVE);
        }

        return 1;
    }
}
