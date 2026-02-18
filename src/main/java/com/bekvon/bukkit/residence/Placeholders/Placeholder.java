package com.bekvon.bukkit.residence.Placeholders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.GetTime;

import me.clip.placeholderapi.PlaceholderAPI;
import net.Zrips.CMILib.Chat.ChatFilterRule;

public class Placeholder {

    private Residence plugin;

    public Placeholder(Residence plugin) {
        this.plugin = plugin;
    }

    private static ChatFilterRule numericalRule = new ChatFilterRule().setPattern("(\\$)(\\d)");

    static LinkedHashMap<String, CMIPlaceHolders> byNameStatic = new LinkedHashMap<String, CMIPlaceHolders>();
    static LinkedHashMap<String, LinkedHashSet<CMIPlaceHolders>> byNameComplex = new LinkedHashMap<String, LinkedHashSet<CMIPlaceHolders>>();

    public enum CMIPlaceHolders {
        residence_ownername_$1("Get owner name by residence name", "residenceName"),
        residence_user_main_name,
        residence_user_main_owner,
        residence_user_amount,
        residence_user_group,
        residence_user_admin,
        residence_user_cancreate,
        residence_user_maxres,
        residence_user_maxew,
        residence_user_maxns,
        residence_user_maxud,
        residence_user_maxsub,
        residence_user_maxsubdepth,
        residence_user_maxrents,
        residence_user_maxrentdays,
        residence_user_blockcost,
        residence_user_blocksell,
        residence_user_current_owner,
        residence_user_current_res,
        residence_user_current_resname,
        residence_user_current_bank,
        residence_user_current_qsize,
        residence_user_current_ssize,
        residence_user_current_forsale,
        residence_user_current_saleprice,
        residence_user_current_forrent,
        residence_user_current_rentprice,
        residence_user_current_rentedby,
        residence_user_current_rentdays,
        residence_user_current_rentends,
        residence_user_current_flag_$1("Get flags from current residence by flag name", "flagName"),
        ;

        static {
            for (CMIPlaceHolders one : CMIPlaceHolders.values()) {
                String fullName = one.toString();
                if (!one.isComplex()) {
                    byNameStatic.put(fullName.toLowerCase(), one);
                    continue;
                }
                String[] split = fullName.split("_");
                String first = split[0] + "_" + split[1];
                LinkedHashSet<CMIPlaceHolders> old = byNameComplex.getOrDefault(first, new LinkedHashSet<CMIPlaceHolders>());
                old.add(one);
                byNameComplex.put(first, old);
            }
        }

        private String[] vars;
        private List<Integer> groups = new ArrayList<Integer>();
        private ChatFilterRule rule = null;
        private boolean cache = true;
        private String desc = null;

        private int cacheForMS = 1000;

        private int MAX_ENTRIES = 20;
        LinkedHashMap<UUID, CMIPlaceholderCache> map = new LinkedHashMap<UUID, CMIPlaceholderCache>(MAX_ENTRIES + 1, .75F, false) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<UUID, CMIPlaceholderCache> eldest) {
                return size() > MAX_ENTRIES;
            }
        };

        CMIPlaceHolders() {
        }

        CMIPlaceHolders(String desc, String... vars) {
            this(desc, true, vars);
        }

        CMIPlaceHolders(String desc, boolean cache, String... vars) {
            this.desc = desc;
            this.vars = vars;
            this.cache = cache;

            try {
                Matcher matcher = numericalRule.getMatcher(this.toString());
                if (matcher != null) {
                    rule = new ChatFilterRule();
                    List<String> ls = new ArrayList<>();
                    ls.add("(%)" + this.toString().replaceAll("\\$\\d", "([^\"^%]*)") + "(%)");
                    ls.add("(\\{)" + this.toString().replaceAll("\\$\\d", "([^\"^%]*)") + "(\\})");
                    rule.setPattern(ls);
                    while (matcher.find()) {
                        try {
                            int id = Integer.parseInt(matcher.group(2));

                            groups.add(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        public Object getCachedValue(UUID uuid) {
            if (!this.isCache() || this.isComplex() || uuid == null)
                return null;
            CMIPlaceholderCache cache = map.get(uuid);
            if (cache == null || System.currentTimeMillis() > cache.getValidUntil())
                return null;
            return cache.getResult();
        }

        public void addCachedValue(UUID uuid, String value, int validForMiliSeconds) {
            if (uuid == null)
                return;
            CMIPlaceholderCache cache = map.getOrDefault(uuid, new CMIPlaceholderCache());
            cache.setResult(value);
            cache.setValidUntil(System.currentTimeMillis() + validForMiliSeconds);
            map.put(uuid, cache);
        }

        public static CMIPlaceHolders getByName(String name) {

            if (name.startsWith("%") || name.startsWith("{"))
                name = name.replaceAll("%|\\{|\\}", "");

            CMIPlaceHolders got = byNameStatic.get(name);
            if (got != null)
                return got;
            String original = name;
            String[] split = name.split("_");

            if (split.length < 3) {
                return null;
            }

            String prefix = split[0] + "_" + split[1];

            Set<CMIPlaceHolders> main = byNameComplex.get(prefix);

            if (main == null) {
                return null;
            }

            for (CMIPlaceHolders mainOne : main) {
                if (!mainOne.getComplexRegexMatchers(original).isEmpty()) {
                    return mainOne;
                }
            }
            return null;
        }

        public static CMIPlaceHolders getByNameExact(String name) {
            return getByName(name);
        }

        public String getFull() {
            if (this.isComplex()) {
                String name = this.name();
                int i = 0;
                for (String one : this.name().split("_")) {
                    if (!one.startsWith("$"))
                        continue;
                    if (vars.length >= i - 1)
                        name = name.replace(one, "[" + vars[i] + "]");
                    i++;
                }

                return "%" + name + "%";
            }
            return "%" + this.name() + "%";
        }

        public List<String> getComplexRegexMatchers(String text) {
            List<String> lsInLs = new ArrayList<String>();
            if (!this.isComplex())
                return lsInLs;

            if (!text.startsWith("%") && !text.endsWith("%"))
                text = "%" + text + "%";

            Matcher matcher = this.getRule().getMatcher(text);
            if (matcher == null)
                return lsInLs;
            while (matcher.find()) {
                lsInLs.add(matcher.group());
            }
            return lsInLs;
        }

        public List<String> getComplexValues(String text) {

            List<String> lsInLs = new ArrayList<String>();
            if (!this.isComplex() || text == null)
                return lsInLs;

            if (!text.startsWith("%") && !text.endsWith("%"))
                text = "%" + text + "%";

            Matcher matcher = this.getRule().getMatcher(text);
            if (matcher == null)
                return lsInLs;
            while (matcher.find()) {
                try {
                    for (Integer oneG : groups) {
                        lsInLs.add(matcher.group(oneG + 1));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            return lsInLs;
        }

        public boolean isComplex() {
            return rule != null;
        }

        public ChatFilterRule getRule() {
            return rule;
        }

        public void setRule(ChatFilterRule rule) {
            this.rule = rule;
        }

        public String getDescription() {
            return desc;
        }

        public boolean isCache() {
            return cache;
        }
//	public static CMIPlaceHolders getByName(String name) {
//	    name = name.replace("_", "");
//	    for (CMIPlaceHolders one : CMIPlaceHolders.values()) {
//		String n = one.name().replace("_", "");
//		if (n.equalsIgnoreCase(name)) {
//		    return one;
//		}
//	    }
//	    name = "residence" + name;
//	    for (CMIPlaceHolders one : CMIPlaceHolders.values()) {
//		String n = one.name().replace("_", "");
//		if (n.equalsIgnoreCase(name)) {
//		    return one;
//		}
//	    }
//	    return null;
//	}
//
//	public static CMIPlaceHolders getByNameExact(String name) {
//	    name = name.toLowerCase();
//	    for (CMIPlaceHolders one : CMIPlaceHolders.values()) {
//		String n = one.name();
//		if (n.equals(name))
//		    return one;
//	    }
//	    return null;
//	}
//
//	public String getFull() {
//	    return "%" + this.name() + "%";
//	}

        public int getCacheForMS() {
            return cacheForMS;
        }
    }

    public List<String> updatePlaceHolders(Player player, List<String> messages) {
        List<String> ms = new ArrayList<String>(messages);
        for (int i = 0, l = messages.size(); i < l; ++i) {
            ms.set(i, updatePlaceHolders(player, messages.get(i)));
        }
        return ms;
    }

    public String updatePlaceHolders(Player player, String message) {
        if (message == null)
            return null;
        if (message.contains("%"))
            message = translateOwnPlaceHolder(player, message);
        if (!plugin.isPlaceholderAPIEnabled())
            return message;
        if (message.contains("%"))
            message = PlaceholderAPI.setPlaceholders(player, message);
        return message;
    }

    Pattern placeholderPatern = Pattern.compile("(%)([^\"^%]*)(%)");

    private String translateOwnPlaceHolder(Player player, String message) {
        if (message == null)
            return null;

        if (message.contains("%")) {
            Matcher match = placeholderPatern.matcher(message);

            while (match.find()) {
                String cmd = match.group(2);
                if (!message.contains("%"))
                    break;
                CMIPlaceHolders place = CMIPlaceHolders.getByNameExact(cmd);

                if (place == null)
                    continue;

                String with = this.getValue(player, place, cmd);

                if (with == null)
                    continue;
                if (place.isComplex()) {
                    message = message.replace("%" + cmd + "%", with);
                } else
                    message = message.replace(place.getFull(), with);
            }
        }

        return message;
    }

    @Deprecated
    public String getValue(Player player, CMIPlaceHolders placeHolder) {
        return getValue(player, placeHolder, null);
    }

    public String getValue(Player player, CMIPlaceHolders placeHolder, String value) {

        if (placeHolder == null)
            return null;

        Object cached = placeHolder.getCachedValue(player != null ? player.getUniqueId() : null);
        if (cached != null) {
            return (String) cached;
        }

        ResidencePlayer user = ResidencePlayer.get(player);

        String result = null;

        if (user != null) {

            if (!placeHolder.isComplex()) {
                switch (placeHolder) {
                case residence_user_admin:
                    result = variable(Residence.getInstance().getPermissionManager().isResidenceAdmin(player));
                    break;
                case residence_user_amount:
                    result = String.valueOf(user.getResAmount());
                    break;
                case residence_user_blockcost:
                    if (Residence.getInstance().getEconomyManager() != null) {
                        result = String.valueOf(user.getGroup().getCostperarea());
                    }
                    break;
                case residence_user_blocksell:
                    if (Residence.getInstance().getEconomyManager() != null) {
                        result = String.valueOf(user.getGroup().getSellperarea());
                    }
                    break;
                case residence_user_cancreate:
                    result = variable(user.getGroup().canCreateResidences());
                    break;
                case residence_user_group:
                    result = user.getGroup().getGroupName();
                    break;
                case residence_user_maxew:
                    PermissionGroup group = user.getGroup();
                    result = group.getMinX() + "-" + user.getMaxX();
                    break;
                case residence_user_maxns:
                    group = user.getGroup();
                    result = group.getMinZ() + "-" + user.getMaxZ();
                    break;
                case residence_user_maxrentdays:
                    result = String.valueOf(user.getGroup().getMaxRentDays());
                    break;
                case residence_user_maxrents:
                    result = String.valueOf(user.getMaxRents());
                    break;
                case residence_user_maxres:
                    result = String.valueOf(user.getMaxRes());
                    break;
                case residence_user_maxsub:
                    result = String.valueOf(user.getMaxSubzones());
                    break;
                case residence_user_maxsubdepth:
                    result = String.valueOf(user.getMaxSubzoneDepth());
                    break;
                case residence_user_maxud:
                    group = user.getGroup();
                    result = group.getMinYSize() + "-" + group.getMaxYSize();
                    break;
                case residence_user_current_owner:
                    ClaimedResidence res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
                    result = res == null ? "" : res.getOwner();
                    break;
                case residence_user_current_res:
                    res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
                    result = res == null ? "" : res.getName();
                    break;
                case residence_user_current_resname:
                    res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
                    result = res == null ? "" : res.getResidenceName();
                    break;
                case residence_user_current_bank:
                    res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
                    result = res == null ? "0" : res.getBank().getStoredMoneyFormated();
                    break;
                case residence_user_current_qsize:
                    res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
                    result = res == null ? "0" : String.valueOf(res.getTotalSize());
                    break;
                case residence_user_current_ssize:
                    res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
                    result = res == null ? "0" : String.valueOf(res.getXZSize());
                    break;
                case residence_user_current_forsale:
                    res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
                    result = res == null ? "" : String.valueOf(res.isForSell());
                    break;
                case residence_user_current_saleprice:
                    res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
                    result = res == null || !res.isForSell() ? "" : String.valueOf(res.getSellPrice());
                    break;
                case residence_user_current_rentprice:
                    res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
                    result = res == null || !res.isForRent() ? "" : String.valueOf(res.getRentable().cost);
                    break;
                case residence_user_current_rentdays:
                    res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
                    result = res == null || !res.isForRent() ? "" : String.valueOf(res.getRentable().days);
                    break;
                case residence_user_current_rentedby:
                    res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
                    result = res == null || !res.isForRent() || res.getRentedLand() == null || res.getRentedLand().getRenterName() == null ? "" : res.getRentedLand().getRenterName();
                    break;
                case residence_user_current_rentends:
                    res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
                    result = res == null || !res.isForRent() || res.getRentedLand() == null || !res.getRentedLand().hasValidRenter() ? "" : GetTime.getTime(res.getRentedLand().endTime, true);
                    break;
                case residence_user_current_forrent:
                    res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
                    result = res == null ? "" : String.valueOf(res.isForRent());
                    break;
                case residence_user_main_name:
                    res = user.getMainResidence();
                    result = res == null ? "" : res.getName();
                    break;
                case residence_user_main_owner:
                    res = user.getMainResidence();
                    result = res == null ? "" : res.getOwner();
                    break;
                default:
                    break;
                }
                if (placeHolder.isCache() && placeHolder.getCachedValue(user.getUniqueId()) == null) {
                    placeHolder.addCachedValue(user.getUniqueId(), result, placeHolder.getCacheForMS());
                }
            } else {
                if (value != null && player != null) {
                    switch (placeHolder) {
                    case residence_user_current_flag_$1:
                        List<String> values = placeHolder.getComplexValues(value);
                        if (values.isEmpty())
                            return "";
                        ClaimedResidence res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
                        if (res == null)
                            return "";
                        Flags flag = Flags.getFlag(values.get(0));
                        if (flag == null)
                            return "";
                        return variable(res.getPermissions().playerHas(player, flag, FlagCombo.TrueOrNone));
                    case residence_ownername_$1:
                        values = placeHolder.getComplexValues(value);
                        if (values.isEmpty())
                            return "";
                         res = plugin.getResidenceManager().getByName(values.get(0));                         
                        if (res == null)
                            return "";
                        return res == null ? "" : res.getName();
                    }
                }
            }
        }

        return result;
    }

    private String variable(Boolean state) {
        return state ? plugin.getLM().getMessage(lm.General_True) : plugin.getLM().getMessage(lm.General_False);
    }
}