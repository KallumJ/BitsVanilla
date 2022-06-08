package team.bits.vanilla.fabric.commands;

import com.google.gson.*;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.server.command.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import team.bits.nibbles.command.*;
import team.bits.vanilla.fabric.*;

import java.io.*;
import java.util.*;

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

        List<Text> ruleComponents = new LinkedList<>();
        int ruleCount = 1;
        for (JsonElement jsonElement : rulesArray) {
            JsonObject jsonObject = (JsonObject) jsonElement;
            String rule = String.format(RULE_STRING, ruleCount++, jsonObject.get("rule").getAsString());
            String hover = jsonObject.get("hover").getAsString();

            Text ruleComponent = Text.literal(rule).styled(style ->
                    style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(hover)))
            );
            ruleComponents.add(ruleComponent);
        }
        String ruleNote = rulesJson.get("note").getAsString();

        MutableText message = Text.literal("---Rules--- \n")
                .styled(style -> style.withColor(Formatting.GREEN));
        ruleComponents.forEach(message::append);
        message.append(ruleNote);

        context.getSource().sendFeedback(message, false);

        return 1;
    }
}
