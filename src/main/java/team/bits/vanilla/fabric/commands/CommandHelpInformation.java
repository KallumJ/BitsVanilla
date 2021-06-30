package team.bits.vanilla.fabric.commands;

public class CommandHelpInformation {
    private String description;
    private String usage;
    private boolean isPublic;

    public CommandHelpInformation() {
    }

    public CommandHelpInformation(String description, String usage, boolean isPublic) {
        this.description = description;
        this.usage = usage;
        this.isPublic = isPublic;
    }

    public String getDescription() {
        return description;
    }

    public CommandHelpInformation setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getUsage() {
        return usage;
    }

    public CommandHelpInformation setUsage(String usage) {
        this.usage = usage;
        return this;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public CommandHelpInformation setPublic(boolean aPublic) {
        isPublic = aPublic;
        return this;
    }
}
