package team.bits.vanilla.fabric.commands;

public class CommandInformation {
    private String description;
    private String usage;
    private boolean isPublic;

    public CommandInformation() {
    }

    public CommandInformation(String description, String usage, boolean isPublic, boolean permissionLevel) {
        this.description = description;
        this.usage = usage;
        this.isPublic = isPublic;
    }

    public String getDescription() {
        return description;
    }

    public CommandInformation setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getUsage() {
        return usage;
    }

    public CommandInformation setUsage(String usage) {
        this.usage = usage;
        return this;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public CommandInformation setPublic(boolean aPublic) {
        isPublic = aPublic;
        return this;
    }
}
