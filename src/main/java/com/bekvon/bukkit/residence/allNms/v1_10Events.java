package com.bekvon.bukkit.residence.allNms;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;

public class v1_10Events implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)

    public void onPlayerFireInteract(EntityDamageEvent event) {
	// disabling event on world
	if (Residence.getInstance().isDisabledWorldListener(event.getEntity().getWorld()))
	    return;

	if (event.getCause() != DamageCause.HOT_FLOOR)
	    return;

	Entity ent = event.getEntity();

	if (!Residence.getInstance().getPermsByLoc(ent.getLocation()).has(Flags.hotfloor, true)) {
	    event.setCancelled(true);
	    return;
	}
    }
}
