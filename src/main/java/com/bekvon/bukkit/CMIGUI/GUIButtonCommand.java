package com.bekvon.bukkit.CMIGUI;

class GUIButtonCommand {
    private String command;
    private CommandType vis = CommandType.gui;

    public GUIButtonCommand(String command) {
        this.command = command;
    }

    public GUIButtonCommand(String command, CommandType vis) {
        this.command = command;
        this.vis = vis;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public CommandType getVis() {
        return vis;
    }

    public void setVis(CommandType vis) {
        this.vis = vis;
    }

}
