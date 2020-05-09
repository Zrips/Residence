package com.bekvon.bukkit.residence.itemlist;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class WorldItemList extends ItemList {

    protected String world;
    protected String group;

    public WorldItemList(ListType listType) {
        super(listType);
    }

    protected WorldItemList() {

    }

    public static WorldItemList readList(ConfigurationSection node) {
        WorldItemList list = new WorldItemList();
        ItemList.readList(node, list);
        list.world = node.getString("World", null);
        list.group = node.getString("Group", null);
        return list;
    }

    public String getWorld() {
        return world;
    }

    public String getGroup() {
        return group;
    }

    public boolean isAllowed(Material mat, String inworld, String ingroup) {
        if (mat == null)
            return true;
        if (!listApplicable(inworld, ingroup))
            return true;
        return super.isAllowed(mat);
    }

    public boolean isIgnored(Material mat, String inworld, String ingroup) {
        if (!listApplicable(inworld, ingroup))
            return false;
        return super.isIgnored(mat);
    }

    public boolean isListed(Material mat, String inworld, String ingroup) {
        if (!listApplicable(inworld, ingroup))
            return false;
        return super.isListed(mat);
    }

    public boolean listApplicable(String inworld, String ingroup) {
        if (world != null) {
            if (!world.equals(inworld)) {
                return false;
            }
        }
        if (group != null) {
            if (!group.equals(ingroup)) {
                return false;
            }
        }
        return true;
    }
}
