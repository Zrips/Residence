package com.bekvon.bukkit.residence.containers;

import net.Zrips.CMILib.Version.Schedulers.CMITask;

public class DelayTeleport {
    private CMITask messageTask = null;
    private CMITask teleportTask = null;
    private int remainingTime = 0;

    public DelayTeleport() {
    }

    public DelayTeleport(CMITask task, int remainingTime) {
        this.messageTask = task;
        this.remainingTime = remainingTime;
    }

    public CMITask getMessageTask() {
        return messageTask;
    }

    public void setMessageTask(CMITask task) {
        this.messageTask = task;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void lowerRemainingTime() {
        remainingTime -= 1;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public CMITask getTeleportTask() {
        return teleportTask;
    }

    public void setTeleportTask(CMITask teleportTask) {
        this.teleportTask = teleportTask;
    }
}
