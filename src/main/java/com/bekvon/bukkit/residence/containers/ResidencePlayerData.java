package com.bekvon.bukkit.residence.containers;

import java.util.HashMap;
import java.util.Map;

public class ResidencePlayerData {

    private long lastSeen = 0L;
    private String lastKnownWorld = null;
    private boolean hadResidence = false;

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

    public boolean hadResidence() {
        return hadResidence;
    }

    public void setHadResidence(boolean hadResidence) {
        this.hadResidence = hadResidence;
    }

    public Map<String, Object> serialize(boolean hasResidences) {
        Map<String, Object> map = new HashMap<String, Object>();

        if (getLastSeen() > 0L)
            map.put("Seen", getLastSeen());

        if (getLastKnownWorld() != null)
            map.put("World", getLastKnownWorld());

        if (!hasResidences && hadResidence())
            map.put("HadResidence", true);
        else
            map.put("HadResidence", null);

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

        if (map.containsKey("HadResidence")) {
            try {
                data.setHadResidence((boolean) map.get("HadResidence"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return data;
    }
}
