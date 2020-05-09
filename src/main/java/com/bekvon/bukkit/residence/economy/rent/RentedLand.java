package com.bekvon.bukkit.residence.economy.rent;

import java.util.HashMap;
import java.util.Map;

public class RentedLand {

    public String player = "";
    public long startTime = 0L;
    public long endTime = 0L;
    public boolean AutoPay = true;

    public Map<String, Object> save() {
	Map<String, Object> rentables = new HashMap<>();
	rentables.put("Player", player);
	rentables.put("StartTime", startTime);
	rentables.put("EndTime", endTime);
	rentables.put("AutoRefresh", AutoPay);
	return rentables;
    }
}
