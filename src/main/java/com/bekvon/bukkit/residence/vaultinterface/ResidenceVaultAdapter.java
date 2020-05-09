package com.bekvon.bukkit.residence.vaultinterface;

import com.bekvon.bukkit.residence.economy.EconomyInterface;
import com.bekvon.bukkit.residence.permissions.PermissionsInterface;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class ResidenceVaultAdapter implements EconomyInterface, PermissionsInterface {

    public static Permission permissions = null;
    public static Economy economy = null;
    public static Chat chat = null;

    public ResidenceVaultAdapter(Server s) {
        setupPermissions(s);
        setupEconomy(s);
        setupChat(s);
    }

    private static boolean setupPermissions(Server s) {
        RegisteredServiceProvider<Permission> permissionProvider = s.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permissions = permissionProvider.getProvider();
        }
        return (permissions != null);
    }

    private static boolean setupChat(Server s) {
        RegisteredServiceProvider<Chat> chatProvider = s.getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }
        return (chat != null);
    }

    private static boolean setupEconomy(Server s) {
        RegisteredServiceProvider<Economy> economyProvider = s.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    public static boolean hasPermission(OfflinePlayer player, String perm, String world) {
        if (permissions == null)
            return false;
        try {
            return permissions.playerHas(world, player, perm);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean permissionsOK() {
        if (permissions != null && !permissions.getName().equalsIgnoreCase("SuperPerms")) {
            return true;
        }
        return false;
    }

    public boolean economyOK() {
        return economy != null;
    }

    public boolean chatOK() {
        return chat != null;
    }

    @Override
    public String getPlayerGroup(Player player) {
        String group = permissions.getPrimaryGroup(player).toLowerCase();
        if (group == null) {
            return group;
        }
        return group.toLowerCase();
    }

    @Override
    public String getPlayerGroup(String player, String world) {
        @SuppressWarnings("deprecation")
        String group = permissions.getPrimaryGroup(world, player);
        if (group == null) {
            return group;
        }
        return group.toLowerCase();
    }

    @Override
    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    @SuppressWarnings("deprecation")
    @Override
    public double getBalance(String playerName) {
        return economy.getBalance(playerName);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canAfford(String playerName, double amount) {
        return economy.has(playerName, amount);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean add(String playerName, double amount) {
        return economy.depositPlayer(playerName, amount).transactionSuccess();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean subtract(String playerName, double amount) {
        return economy.withdrawPlayer(playerName, amount).transactionSuccess();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
        if (economy.withdrawPlayer(playerFrom, amount).transactionSuccess()) {
            if (economy.depositPlayer(playerTo, amount).transactionSuccess()) {
                return true;
            }
            economy.depositPlayer(playerFrom, amount);
            return false;
        }
        return false;
    }

    public String getEconomyName() {
        if (economy != null) {
            return economy.getName();
        }
        return "";
    }

    public String getPermissionsName() {
        if (permissions != null) {
            return permissions.getName();
        }
        return "";
    }

    public String getChatName() {
        if (chat != null) {
            return chat.getName();
        }
        return "";
    }

    @Override
    public String getName() {
        return "Vault";
    }

    @Override
    public String format(double amount) {
        return economy.format(amount);
    }

}