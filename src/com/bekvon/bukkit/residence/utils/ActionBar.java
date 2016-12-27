package com.bekvon.bukkit.residence.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import com.bekvon.bukkit.residence.containers.ABInterface;

/**
*
* @author hamzaxx
*/
public class ActionBar implements ABInterface {
    private String version = "";
    public Method getHandle;
    public Method sendPacket;
    public Field playerConnection;
    private Method nmsChatSerializer;
    private Method fromString;
    private Constructor<?> nmsPacketPlayOutChat;
    private Constructor<?> nmsPacketPlayOutTitle;
    private Class<?> enumTitleAction;
    private boolean simpleMessages = false;
    private boolean dontSendTitle = false;

    public ActionBar() {
	try {
	    String[] v = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
	    version = v[v.length - 1];
//	    version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	    Class<?> typePacketPlayOutChat = Class.forName(getPacketPlayOutChatClasspath());
	    Class<?> nmsIChatBaseComponent = Class.forName(getIChatBaseComponentClasspath());
	    Class<?> typePacketPlayOutTitle = Class.forName(getPacketPlayOutTitleClasspath());
	    enumTitleAction = Class.forName(getEnumTitleActionClasspath());

	    nmsChatSerializer = Class.forName(getChatSerializerClasspath()).getMethod(getChatSerializerMethod(), String.class);
	    getHandle = Class.forName(getCraftPlayerClasspath()).getMethod(getHandleMethod());
	    playerConnection = Class.forName(getNMSPlayerClasspath()).getField(getPlayerConnection());
	    sendPacket = Class.forName(getPlayerConnectionClasspath()).getMethod(getSendPacket(), Class.forName(getPacketClasspath()));
	    fromString = Class.forName(getClassMessageClasspath()).getMethod(getFromString(), String.class);

	    nmsPacketPlayOutTitle = typePacketPlayOutTitle.getConstructor(enumTitleAction, nmsIChatBaseComponent);
	    if (!version.contains("1_7")) {
		nmsPacketPlayOutChat = typePacketPlayOutChat.getConstructor(nmsIChatBaseComponent, byte.class);
	    } else {
		nmsPacketPlayOutChat = typePacketPlayOutChat.getConstructor(nmsIChatBaseComponent, int.class);
	    }
	} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | NoSuchFieldException ex) {
	    simpleMessages = true;
	    Bukkit.getLogger().log(Level.SEVERE, "Your server can't fully support action bar messages. They will be shown in chat instead.");
	}
    }

    @Override
    public void send(CommandSender sender, String msg) {
	if (sender instanceof Player)
	    send((Player) sender, msg);
	else
	    sender.sendMessage(msg);
    }

    @Override
    public void send(Player player, String msg) {
	if (simpleMessages) {
	    player.sendMessage(msg);
	    return;
	}
	try {
	    Object packet;
	    Object serialized = nmsChatSerializer.invoke(null, "{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', JSONObject
		.escape(msg)) + "\"}");
	    if (!version.contains("1_7")) {
		packet = nmsPacketPlayOutChat.newInstance(serialized, (byte) 2);
	    } else {
		packet = nmsPacketPlayOutChat.newInstance(serialized, 2);
	    }
	    sendPacket(player, packet);
	} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
	    simpleMessages = true;
	    Bukkit.getLogger().log(Level.SEVERE, "Your server can't fully support action bar messages. They will be shown in chat instead.");
	}
    }

    @Override
    public void sendTitle(Player player, Object title, Object subtitle) {
	if (dontSendTitle) return;
	try {
	    if (title != null) {
		Object packetTitle = nmsPacketPlayOutTitle.newInstance(enumTitleAction.getField("TITLE").get(null),
			((Object[]) fromString.invoke(null, ChatColor.translateAlternateColorCodes('&',String.valueOf(title))))[0]);
		sendPacket(player, packetTitle);
	    }
	    if (subtitle != null) {
		Object packetSubtitle = nmsPacketPlayOutTitle.newInstance(enumTitleAction.getField("SUBTITLE").get(null),
			((Object[]) fromString.invoke(null, ChatColor.translateAlternateColorCodes('&',String.valueOf(subtitle))))[0]);
		sendPacket(player, packetSubtitle);
	    }
	} catch ( SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
	    dontSendTitle = true;
	    Bukkit.getLogger().log(Level.SEVERE, "Your server can't fully support title messages. They will be disabled.");
	}
    }

    private void sendPacket(Player player, Object packet) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
	Object handle = getHandle.invoke(player);
	Object connection = playerConnection.get(handle);
	sendPacket.invoke(connection, packet);
    }

    private String getCraftPlayerClasspath() {
	return "org.bukkit.craftbukkit." + version + ".entity.CraftPlayer";
    }

    private String getClassMessageClasspath() {
	return "org.bukkit.craftbukkit." + version + ".util.CraftChatMessage";
    }

    private String getPlayerConnectionClasspath() {
	return "net.minecraft.server." + version + ".PlayerConnection";
    }

    private String getNMSPlayerClasspath() {
	return "net.minecraft.server." + version + ".EntityPlayer";
    }

    private String getPacketClasspath() {
	return "net.minecraft.server." + version + ".Packet";
    }

    private String getIChatBaseComponentClasspath() {
	return "net.minecraft.server." + version + ".IChatBaseComponent";
    }

    private String getChatSerializerClasspath() {
	if (version.equals("v1_8_R1") || version.contains("1_7")) {
	    return "net.minecraft.server." + version + ".ChatSerializer";
	}
	return "net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer";// 1_8_R2 moved to IChatBaseComponent
    }

    private String getPacketPlayOutChatClasspath() {
	return "net.minecraft.server." + version + ".PacketPlayOutChat";
    }

    private String getPacketPlayOutTitleClasspath() {
	return "net.minecraft.server." + version + ".PacketPlayOutTitle";
    }

    private String getEnumTitleActionClasspath() {
	return getPacketPlayOutTitleClasspath() + "$EnumTitleAction";
    }

    private String getChatSerializerMethod() {
	return "a";
    }

    private String getHandleMethod() {
	return "getHandle";
    }

    private String getPlayerConnection() {
	return "playerConnection";
    }

    private String getSendPacket() {
	return "sendPacket";
    }

    private String getFromString() {
	return "fromString";
    }
}
