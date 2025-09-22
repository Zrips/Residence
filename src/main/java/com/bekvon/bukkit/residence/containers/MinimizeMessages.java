package com.bekvon.bukkit.residence.containers;

public class MinimizeMessages {

    private String enter = "";
    private String leave = "";

    private int id = 0;

    public MinimizeMessages(int id, String enter, String leave) {
        this.id = id;
        this.enter = enter == null ? "" : enter;
        this.leave = leave == null ? "" : leave;
    }

    public boolean same(String enter, String leave) {
        return this.enter.equals(enter) && this.leave.equals(leave);
    }

    public boolean add(String enter, String leave) {
        if (!same(enter, leave))
            return false;
        this.enter = enter;
        this.leave = leave;
        return true;
    }

    public int getId() {
        return id;
    }

    public String getEnter() {
        return enter;
    }

    public String getLeave() {
        return leave;
    }

}
