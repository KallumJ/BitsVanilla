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

    public static final Text ENTER_MSG = Text.literal("Entering freecam mode").styled(style -> style.withColor(Colors.POSITIVE));
    public static final Text EXIT_MSG = Text.literal("Returning to body").styled(style -> style.withColor(Colors.POSITIVE));

    public static final Map<ServerPlayerEntity, Freecam> ACTIVE_FREECAMS = new HashMap<>();

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

        if (!ACTIVE_FREECAMS.containsKey(player)) {
            Optional<Freecam> freecam = Freecam.Factory.create(player);
            if (freecam.isPresent()) {
                ACTIVE_FREECAMS.put(player, freecam.get());
                player.sendMessage(ENTER_MSG);
            }

        } else {
            ACTIVE_FREECAMS.get(player).removeAndReturnPlayer();
            player.sendMessage(EXIT_MSG);
        }

        return 1;
    }
}
