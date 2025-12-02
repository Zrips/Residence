package com.bekvon.bukkit.residence.containers;

import java.util.HashMap;
import java.util.Map;

public class ResidencePlayerData {

    private long lastSeen = 0L;
    private String lastKnownWorld = null;
    private boolean ownedResidence = false;

    public long getLastSeen() {
        return lastSeen;
    }

    public ResidencePlayerData setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
        return this;
    }

    public String getLastKnownWorld() {
        return lastKnownWorld;
    }

    public ResidencePlayerData setLastKnownWorld(String lastKnownWorld) {
        this.lastKnownWorld = lastKnownWorld;
        return this;
    }

    public boolean ownedResidence() {
        return ownedResidence;
    }

    public void ownedResidence(boolean ownedResidence) {
        this.ownedResidence = ownedResidence;
    }

    public Map<String, Object> serialize(boolean hasResidences) {
        Map<String, Object> map = new HashMap<String, Object>();

        if (getLastSeen() > 0L)
            map.put("Seen", getLastSeen());

        if (getLastKnownWorld() != null)
            map.put("World", getLastKnownWorld());

        if (!hasResidences && ownedResidence())
            map.put("OwnedResidence", true);
        else
            map.put("OwnedResidence", null);

        return map;
    }

    public static ResidencePlayerData deserialize(Map<String, Object> map) {

        ResidencePlayerData data = new ResidencePlayerData();

        if (map.containsKey("Seen")) {
            try {
                data.setLastSeen((Long) map.get("Seen"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (map.containsKey("World")) {
            try {
                data.setLastKnownWorld((String) map.get("World"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (map.containsKey("OwnedResidence")) {
            try {
                data.ownedResidence((boolean) map.get("OwnedResidence"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return data;
    }
}
