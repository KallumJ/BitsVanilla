package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * A command node which will not show up in the autocomplete suggestions
 * if the source does not meet the requirements for this node.
 * Allows you to hide subcommands from players who don't have the right permissions.
 */
public class RestrictedCommandNode extends LiteralCommandNode<ServerCommandSource> {

    private RestrictedCommandNode(String literal, Command<ServerCommandSource> command,
                                  Predicate<ServerCommandSource> requirement,
                                  CommandNode<ServerCommandSource> redirect,
                                  RedirectModifier<ServerCommandSource> modifier,
                                  boolean forks) {
        super(literal, command, requirement, redirect, modifier, forks);
    }

    public static @NotNull RestrictedCommandNode create(@NotNull LiteralArgumentBuilder<ServerCommandSource> node) {
        return new RestrictedCommandNode(
                node.getLiteral(), node.getCommand(), node.getRequirement(),
                node.getRedirect(), node.getRedirectModifier(), node.isFork()
        );
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext<ServerCommandSource> context,
                                                          SuggestionsBuilder builder) {
        if (this.getRequirement().test(context.getSource())) {
            return super.listSuggestions(context, builder);
        } else {
            return Suggestions.empty();
        }
    }
}
