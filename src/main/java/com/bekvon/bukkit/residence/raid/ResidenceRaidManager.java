package com.bekvon.bukkit.residence.raid;

import java.util.HashMap;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ResidenceRaidManager {

    private static HashMap<UUID, ResidenceRaid> residenceRaids = new HashMap<UUID, ResidenceRaid>();

    public ResidenceRaidManager() {

    }

    public static @Nullable ResidenceRaid getResidenceRaidOrNull(ClaimedResidence residence) {
        return residenceRaids.get(residence.getUniqueId());
    }

    public static @NotNull ResidenceRaid getResidenceRaid(ClaimedResidence residence) {
        return residenceRaids.computeIfAbsent(residence.getUniqueId(), k -> new ResidenceRaid(residence));
    }

    public static void addResidenceRaid(ResidenceRaid raid) {
        residenceRaids.put(raid.getRes().getUniqueId(), raid);
    }

    public static void removeResidenceRaid(UUID residenceId) {
        residenceRaids.remove(residenceId);
    }
}
