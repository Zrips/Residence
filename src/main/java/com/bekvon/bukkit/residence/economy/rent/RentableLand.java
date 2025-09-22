package com.bekvon.bukkit.residence.economy.rent;

import java.util.HashMap;
import java.util.Map;

import com.bekvon.bukkit.residence.Residence;

public class RentableLand {

    public int days = 0;
    public int cost = Integer.MAX_VALUE;
    public boolean AllowRenewing = Residence.getInstance().getConfigManager().isRentAllowRenewing();
    public boolean StayInMarket = Residence.getInstance().getConfigManager().isRentStayInMarket();
    public boolean AllowAutoPay = Residence.getInstance().getConfigManager().isRentAllowAutoPay();

    public RentableLand() {
    }

    public Map<String, Object> save() {
	Map<String, Object> rented = new HashMap<>();
	rented.put("Days", days);
	rented.put("Cost", cost);
	rented.put("Repeatable", AllowRenewing);
	rented.put("StayInMarket", StayInMarket);
	rented.put("AllowAutoPay", AllowAutoPay);
	return rented;
    }
}
