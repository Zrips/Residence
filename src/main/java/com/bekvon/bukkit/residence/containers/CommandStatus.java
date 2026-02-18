package com.bekvon.bukkit.residence.containers;

public class CommandStatus {

    private boolean simple;
    private int priority = 0;
    private String info;
    private String[] usage;

    public CommandStatus(boolean simple, int priority, String info, String[] usage) {
        this.simple = simple;
        this.priority = priority;
        this.info = info;
        this.usage = usage;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public boolean getSimple() {
        return simple;
    }

    public void setSimple(Boolean simple) {
        this.simple = simple;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String[] getUsage() {
        return usage;
    }

    public void setUsage(String[] usage) {
        this.usage = usage;
    }
}
