package com.bekvon.bukkit.residence.containers;

public class HelpLines {

    private String command;
    private String desc;

    public HelpLines(String command, String desc) {
	this.command = command;
	this.desc = desc;
    }

    public String getCommand() {
	return this.command;
    }

    public String getDesc() {
	return this.desc;
    }
}
