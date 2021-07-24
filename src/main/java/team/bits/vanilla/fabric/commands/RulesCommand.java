package team.bits.vanilla.fabric.commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.command.ServerCommandSource;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;
import team.bits.vanilla.fabric.BitsVanilla;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class RulesCommand extends Command {

    private static JsonObject rulesJson = new JsonObject();
    private static final String RULES_FILE_NAME = "rules.json";
    private static final String RULE_STRING = "%d. %s \n\n";

    public RulesCommand() {
        super("rules", new CommandInformation()
            .setDescription("Lists the rules to the player")
            .setPublic(true)
        );


        try (InputStream input = BitsVanilla.class.getClassLoader().getResourceAsStream(RULES_FILE_NAME)) {
            Gson gson = new Gson();
            Scanner s = new Scanner(Objects.requireNonNull(input)).useDelimiter("\\A");
            String jsonString = s.hasNext() ? s.next() : "";

            rulesJson = gson.fromJson(jsonString, JsonObject.class);
        } catch (IOException ex) {
            throw new RuntimeException("No rules file found");
        }

    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        JsonArray rulesArray = rulesJson.getAsJsonArray("rules");

        List<TextComponent> ruleComponents = new LinkedList<>();
        int ruleCount = 1;
        for (JsonElement jsonElement : rulesArray) {
            JsonObject jsonObject = (JsonObject) jsonElement;
            String rule = String.format(RULE_STRING, ruleCount++, jsonObject.get("rule").getAsString());
            String hover = jsonObject.get("hover").getAsString();

            TextComponent ruleComponent = Component.text(rule).hoverEvent(HoverEvent.showText(Component.text(hover))).color(NamedTextColor.WHITE);
            ruleComponents.add(ruleComponent);
        }
        String ruleNote = rulesJson.get("note").getAsString();

        TextComponent message = Component.text("---Rules--- \n").color(NamedTextColor.GREEN)
                .append(Component.text().append(ruleComponents))
                .append(Component.text(ruleNote));

        BitsVanilla.audience(context.getSource().getPlayer()).sendMessage(message);


        return 1;
    }
}
