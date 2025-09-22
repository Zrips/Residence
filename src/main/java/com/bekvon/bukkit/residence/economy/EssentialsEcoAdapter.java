package com.bekvon.bukkit.residence.economy;

import java.math.BigDecimal;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;

import net.ess3.api.MaxMoneyException;

public class EssentialsEcoAdapter implements EconomyInterface {

    Essentials plugin;

    public EssentialsEcoAdapter(Essentials p) {
        plugin = p;
        String serverland = Residence.getInstance().getServerLandName();
        if (!Economy.playerExists(serverland)) {
            Economy.createNPC(serverland);
        }
    }

    @Override
    public double getBalance(Player player) {
        return getBalance(player.getUniqueId());

    }

    @Override
    public double getBalance(UUID player) {
        try {
            if (Economy.playerExists(player)) {
                return Economy.getMoneyExact(player).doubleValue();
            }
            return 0;
        } catch (UserDoesNotExistException ex) {
            return 0;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public double getBalance(String playerName) {
        try {
            if (Economy.playerExists(playerName)) {
                return Economy.getMoney(playerName);
            }
            return 0;
        } catch (UserDoesNotExistException ex) {
            return 0;
        }
    }

    @Override
    public boolean canAfford(Player player, double amount) {
        return canAfford(player.getUniqueId(), amount);
    }

    @Override
    public boolean canAfford(UUID player, double amount) {
        if (amount < 0)
            return false;
        try {
            if (Economy.playerExists(player)) {
                return Economy.hasEnough(player, BigDecimal.valueOf(amount));
            }
            return false;
        } catch (UserDoesNotExistException ex) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canAfford(String playerName, double amount) {
        if (amount < 0)
            return false;
        try {
            if (Economy.playerExists(playerName)) {
                return Economy.hasEnough(playerName, amount);
            }
            return false;
        } catch (UserDoesNotExistException ex) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean add(String playerName, double amount) {
        if (amount < 0)
            return false;
        if (Economy.playerExists(playerName)) {
            try {
                Economy.add(playerName, amount);
                return true;
            } catch (UserDoesNotExistException ex) {
                return false;
            } catch (NoLoanPermittedException ex) {
                return false;
            } catch (MaxMoneyException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean add(UUID playerUUID, double amount) {
        if (amount < 0)
            return false;
        if (Economy.playerExists(playerUUID)) {
            try {
                Economy.add(playerUUID, BigDecimal.valueOf(amount));
                return true;
            } catch (UserDoesNotExistException ex) {
                return false;
            } catch (NoLoanPermittedException ex) {
                return false;
            } catch (MaxMoneyException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean subtract(Player player, double amount) {
        return subtract(player.getUniqueId(), amount);
    }

    @Override
    public boolean subtract(UUID playerUUID, double amount) {
        if (amount < 0)
            return false;
        if (Economy.playerExists(playerUUID)) {
            try {
                Economy.subtract(playerUUID, BigDecimal.valueOf(amount));
                return true;
            } catch (UserDoesNotExistException ex) {
                return false;
            } catch (NoLoanPermittedException ex) {
                return false;
            } catch (MaxMoneyException e) {
                return false;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean subtract(String playerName, double amount) {
        if (amount < 0)
            return false;
        if (Economy.playerExists(playerName)) {
            try {
                Economy.subtract(playerName, amount);
                return true;
            } catch (UserDoesNotExistException ex) {
                return false;
            } catch (NoLoanPermittedException ex) {
                return false;
            } catch (MaxMoneyException e) {
                return false;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
        if (amount < 0)
            return false;
        try {
            if (Economy.playerExists(playerFrom) && Economy.playerExists(playerTo) && Economy.hasEnough(playerFrom, amount)) {
                if (!subtract(playerFrom, amount))
                    return false;
                if (!add(playerTo, amount)) {
                    add(playerFrom, amount);
                    return false;
                }
                return true;
            }
        } catch (UserDoesNotExistException ex) {
            return false;
        }
        return false;
    }

    @Override
    public boolean transfer(UUID playerFrom, UUID playerTo, double amount) {
        if (amount < 0)
            return false;
        try {
            if (Economy.playerExists(playerFrom) && Economy.playerExists(playerTo) && Economy.hasEnough(playerFrom, BigDecimal.valueOf(amount))) {
                if (!subtract(playerFrom, amount))
                    return false;
                if (!add(playerTo, amount)) {
                    add(playerFrom, amount);
                    return false;
                }
                return true;
            }
        } catch (UserDoesNotExistException ex) {
            return false;
        }
        return false;
    }

    @Override
    public String getName() {
        return "EssentialsEconomy";
    }

    @Override
    public String format(double amount) {
        return Economy.format(BigDecimal.valueOf(amount));
    }
}
