package com.bekvon.bukkit.residence.shopStuff;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import com.bekvon.bukkit.residence.CommentedYamlConfiguration;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITask;

public class ShopSignUtil {

    List<Board> AllBoards = new ArrayList<Board>();

    private Residence plugin;

    public ShopSignUtil(Residence plugin) {
        this.plugin = plugin;
    }

    public void setAllSigns(List<Board> AllBoards) {
        this.AllBoards = AllBoards;
    }

    public List<Board> getAllBoards() {
        return AllBoards;
    }

    public void removeBoard(Board Board) {
        AllBoards.remove(Board);
    }

    public void addBoard(Board Board) {
        AllBoards.add(Board);
    }

    public boolean exist(Board board) {
        List<Location> loc2 = board.GetLocations();
        for (Board one : AllBoards) {
            List<Location> loc1 = one.GetLocations();
            for (Location oneL : loc1) {
                if (!loc2.contains(oneL))
                    continue;
                return true;
            }
        }
        return false;
    }

    // Res Shop vote file
    public void LoadShopVotes() {
        File file = new File(plugin.getDataFolder(), "ShopVotes.yml");
        YamlConfiguration f = YamlConfiguration.loadConfiguration(file);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if (!f.isConfigurationSection("ShopVotes"))
            return;

        ConfigurationSection ConfCategory = f.getConfigurationSection("ShopVotes");
        ArrayList<String> categoriesList = new ArrayList<String>(ConfCategory.getKeys(false));
        if (categoriesList.isEmpty())
            return;

        for (String category : categoriesList) {
            List<String> List = ConfCategory.getStringList(category);
            List<ShopVote> VoteList = new ArrayList<ShopVote>();
            for (String oneEntry : List) {
                if (!oneEntry.contains("%"))
                    continue;

                String name = oneEntry.split("%")[0];
                UUID uuid = null;

                if (name.contains(":")) {
                    try {
                        uuid = UUID.fromString(name.split(":")[1]);
                    } catch (Exception e) {
                    }
                    name = name.split(":")[0];
                }

                int vote = -1;

                try {
                    String voteString = oneEntry.split("%")[1];
                    if (voteString.contains("!")) {
                        voteString = oneEntry.split("%")[1].split("!")[0];
                    }
                    vote = Integer.parseInt(voteString);
                } catch (Exception ex) {
                    continue;
                }
                if (vote < 0)
                    vote = 0;
                else if (vote > 10)
                    vote = 10;

                long time = 0L;

                if (oneEntry.contains("!"))
                    try {
                        time = Long.parseLong(oneEntry.split("!")[1]);
                    } catch (Exception ex) {
                        time = System.currentTimeMillis();
                    }

                VoteList.add(new ShopVote(name, uuid, vote, time));

            }

            ClaimedResidence res = plugin.getResidenceManager().getByName(category.replace("_", "."));

            if (res == null)
                continue;
            res.clearShopVotes();
            res.addShopVote(VoteList);
        }
        return;
    }

    CMITask shopSaveTask = null;

    public void saveShopVotes(boolean delay) {

        if (!delay) {
            saveShopVotes();
            return;
        }

        if (shopSaveTask != null)
            return;

        shopSaveTask = CMIScheduler.runLaterAsync(plugin, this::saveShopVotes, 20L * 15L); 
    }

    public void forceSaveIfPending() {

        if (shopSaveTask == null)
            return;

        shopSaveTask.cancel();
        shopSaveTask = null;

        saveShopVotes();
    }

    // Shops votes save file
    public void saveShopVotes() {
        
        if (shopSaveTask != null) {
            shopSaveTask.cancel();
            shopSaveTask = null;
        }

        File f = new File(plugin.getDataFolder(), "ShopVotes.yml");
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);

        CommentedYamlConfiguration writer = new CommentedYamlConfiguration();
        conf.options().copyDefaults(true);

        writer.addComment("ShopVotes", "DO NOT EDIT THIS FILE BY HAND!");

        if (!conf.isConfigurationSection("ShopVotes"))
            conf.createSection("ShopVotes");

        for (ClaimedResidence res : plugin.getResidenceManager().getShops()) {

            if (res == null || res.getAllShopVotes().isEmpty())
                continue;

            String path = "ShopVotes." + res.getName().replace(".", "_");

            List<String> list = new ArrayList<String>();

            for (ShopVote oneVote : res.getAllShopVotes()) {
                list.add(oneVote.getName() + ":" + oneVote.getUuid().toString() + "%" + oneVote.getVote() + "!" + oneVote.getTime());
            }
            writer.set(path, list);
        }

        try {
            writer.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    // Res Shop vote file
    public Vote getAverageVote(String resName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(resName);
        return getAverageVote(res);
    }

    public Vote getAverageVote(ClaimedResidence res) {

        if (res == null || res.getAllShopVotes().isEmpty())
            return new Vote(plugin.getConfigManager().getVoteRangeTo() / 2, 0);

        List<ShopVote> votes = res.getAllShopVotes();

        double total = 0;
        for (ShopVote oneVote : votes) {
            total += oneVote.getVote();
        }

        double vote = ((int) ((total / votes.size()) * 100)) / 100.0;

        return new Vote(vote, votes.size());
    }

    // Res Shop vote file
    public int getLikes(String resName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(resName);
        return getLikes(res);
    }

    public int getLikes(ClaimedResidence res) {
        if (res == null || res.getAllShopVotes().isEmpty())
            return 0;

        List<ShopVote> votes = res.getAllShopVotes();

        int likes = 0;
        for (ShopVote oneVote : votes) {
            if (oneVote.getVote() >= plugin.getConfigManager().getVoteRangeTo() / 2)
                likes++;
        }

        return likes;
    }

    public Map<String, Double> getSortedShopList() {

        Map<String, Double> allvotes = new HashMap<String, Double>();

        List<ClaimedResidence> shops = plugin.getResidenceManager().getShops();

        for (ClaimedResidence one : shops) {
            if (plugin.getConfigManager().isOnlyLike())
                allvotes.put(one.getName(), (double) getLikes(one));
            else
                allvotes.put(one.getName(), getAverageVote(one).getVote());
        }

        allvotes = sortByComparator(allvotes);

        return allvotes;
    }

    private static Map<String, Double> sortByComparator(Map<String, Double> allvotes) {

        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(allvotes.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
            Map.Entry<String, Double> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    // Shop Sign file
    public void LoadSigns() {
        getAllBoards().clear();
        File file = new File(plugin.getDataFolder(), "ShopSigns.yml");
        YamlConfiguration f = YamlConfiguration.loadConfiguration(file);

        if (!f.isConfigurationSection("ShopSigns"))
            return;

        ConfigurationSection ConfCategory = f.getConfigurationSection("ShopSigns");
        ArrayList<String> categoriesList = new ArrayList<String>(ConfCategory.getKeys(false));
        if (categoriesList.size() == 0)
            return;
        for (String category : categoriesList) {
            ConfigurationSection NameSection = ConfCategory.getConfigurationSection(category);
            Board newTemp = new Board();
            newTemp.setStartPlace(NameSection.getInt("StartPlace"));

            World w = Bukkit.getWorld(NameSection.getString("World"));

            if (w == null)
                continue;

            Location loc1 = new Location(w, NameSection.getInt("TX"), NameSection.getInt("TY"), NameSection.getInt("TZ"));
            Location loc2 = new Location(w, NameSection.getInt("BX"), NameSection.getInt("BY"), NameSection.getInt("BZ"));

            newTemp.setTopLoc(loc1);
            newTemp.setBottomLoc(loc2);

            addBoard(newTemp);
        }
        return;
    }

    // Signs save file
    public void saveSigns() {
        File f = new File(plugin.getDataFolder(), "ShopSigns.yml");
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);

        CommentedYamlConfiguration writer = new CommentedYamlConfiguration();
        conf.options().copyDefaults(true);

        writer.addComment("ShopSigns", "DO NOT EDIT THIS FILE BY HAND!");

        if (!conf.isConfigurationSection("ShopSigns"))
            conf.createSection("ShopSigns");

        int cat = 0;
        for (Board one : getAllBoards()) {
            cat++;
            String path = "ShopSigns." + cat;
            writer.set(path + ".StartPlace", one.GetStartPlace());
            writer.set(path + ".World", one.GetWorld());
            writer.set(path + ".TX", one.getTopLoc().getBlockX());
            writer.set(path + ".TY", one.getTopLoc().getBlockY());
            writer.set(path + ".TZ", one.getTopLoc().getBlockZ());
            writer.set(path + ".BX", one.getBottomLoc().getBlockX());
            writer.set(path + ".BY", one.getBottomLoc().getBlockY());
            writer.set(path + ".BZ", one.getBottomLoc().getBlockZ());
        }

        try {
            writer.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    public boolean boardUpdateDelayed() {
        CMIScheduler.runTaskLater(plugin, () -> boardUpdate(), 20L);
        return true;
    }

    public boolean boardUpdate() {
        for (Board board : getAllBoards()) {
            board.clearSignLoc();
            List<Location> SignsLocation = board.GetLocations();

            ArrayList<String> ShopNames = new ArrayList<String>(getSortedShopList().keySet());

            int Start = board.GetStartPlace();
            for (Location OneSignLoc : SignsLocation) {

                Block block = OneSignLoc.getBlock();

                if (!(block.getState() instanceof Sign))
                    continue;

                String Shop = "";
                if (ShopNames.size() > Start)
                    Shop = ShopNames.get(Start);

                ClaimedResidence res = plugin.getResidenceManager().getByName(Shop);

                Sign sign = (Sign) block.getState();

                if (res == null || Shop == null || Shop.equalsIgnoreCase("")) {
                    sign.setLine(0, "");
                    sign.setLine(1, "");
                    sign.setLine(2, "");
                    sign.setLine(3, "");
                    sign.update();
                    continue;
                }

                Vote vote = null;
                String votestat = "";
                if (plugin.getResidenceManager().getShops().size() >= Start) {
                    vote = getAverageVote(ShopNames.get(Start));

                    if (plugin.getConfigManager().isOnlyLike()) {
                        votestat = vote.getAmount() == 0 ? "" : plugin.msg(lm.Shop_ListLiked, getLikes(ShopNames.get(Start)));
                    } else
                        votestat = vote.getAmount() == 0 ? "" : plugin.msg(lm.Shop_SignLines_4, vote.getVote(), vote.getAmount());
                }

                sign.setLine(0, plugin.msg(lm.Shop_SignLines_1, Start + 1));
                sign.setLine(1, plugin.msg(lm.Shop_SignLines_2, res.getName()));
                sign.setLine(2, plugin.msg(lm.Shop_SignLines_3, res.getOwner()));
                sign.setLine(3, votestat);
                sign.update();
                board.addSignLoc(res.getName(), sign.getLocation());

                Start++;
            }
        }
        return true;
    }
}
