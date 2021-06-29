package team.bits.vanilla.fabric.commands;

public class CommandHelpInformation {
    private String description;
    private String usage;
    private boolean isPublic;

    public CommandHelpInformation() {}

    public CommandHelpInformation(String description, String usage, boolean isPublic) {
        this.description = description;
        this.usage = usage;
        this.isPublic = isPublic;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public CommandHelpInformation setDescription(String description) {
        this.description = description;
        return this;
    }

    public CommandHelpInformation setUsage(String usage) {
        this.usage = usage;
        return this;
    }

    public CommandHelpInformation setPublic(boolean aPublic) {
        isPublic = aPublic;
        return this;
    }
}
