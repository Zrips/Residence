package com.bekvon.bukkit.residence.containers;

import java.util.HashSet;
import java.util.Set;

import net.Zrips.CMILib.Items.CMIMaterial;

public enum Flags {
    anvil(CMIMaterial.ANVIL, FlagMode.Both, "Allows or denys interaction with anvil", true),
    admin(CMIMaterial.BEDROCK, FlagMode.Player, "Gives a player permission to change flags on a residence", true),
    animalfeeding(CMIMaterial.CARROT, FlagMode.Both, "Allows or denys animal feeding", true),
    animalkilling(CMIMaterial.CHICKEN, FlagMode.Both, "Allows or denys animal killing", true),
    animals(CMIMaterial.PIG_SPAWN_EGG, FlagMode.Residence, "Allows or denys animal spawns", true),
    anchor(CMIMaterial.RESPAWN_ANCHOR, FlagMode.Both, "Allows or denys respawn anchor usage", true),
    anvilbreak(CMIMaterial.ANVIL, FlagMode.Residence, "Allows or denys anvil break in residence", true),
    safezone(CMIMaterial.APPLE, FlagMode.Residence, "Setting to true makes the residence clean bad effects from its occupants", false),
    backup(CMIMaterial.BOOKSHELF, FlagMode.Residence, "If set to true, restores previous look of area (WordEdit required)", false),
    bank(CMIMaterial.ENDER_CHEST, FlagMode.Both, "Allows or denys deposit/withdraw money from res bank", true),
    bed(CMIMaterial.WHITE_BED, FlagMode.Both, "Allows or denys players to use beds", true),
    honey(CMIMaterial.BEEHIVE, FlagMode.Both, "Allows or denys players to get honey", true),
    honeycomb(CMIMaterial.BEE_NEST, FlagMode.Both, "Allows or denys players to get honeycomb", true),
    beacon(CMIMaterial.BEACON, FlagMode.Both, "Allows or denys interaction with beacon", true),
    brew(CMIMaterial.BREWING_STAND, FlagMode.Both, "Allows or denys players to use brewing stands", true),
    build(CMIMaterial.BRICKS, FlagMode.Both, "Allows or denys building", true),
    burn(CMIMaterial.TORCH, FlagMode.Residence, "Allows or denys Mob combustion in residences", true),
    button(CMIMaterial.OAK_BUTTON, FlagMode.Both, "Allows or denys players to use buttons", true),
    boarding(CMIMaterial.ACACIA_BOAT, FlagMode.Residence, "Allows or denys animal boarding in residences", true),
    brush(CMIMaterial.BRUSH, FlagMode.Both, "Allows or denys block brushing", true),
    cake(CMIMaterial.CAKE, FlagMode.Both, "Allows or denys players to eat cake", true),
    canimals(CMIMaterial.SHEEP_SPAWN_EGG, FlagMode.Residence, "Allows or denys custom animal spawns", true),
    chorustp(CMIMaterial.CHORUS_FRUIT, FlagMode.Both, "Allow or disallow teleporting to the residence with chorus fruit", true),
    chat(CMIMaterial.WRITABLE_BOOK, FlagMode.Both, "Allows to join residence chat room", true),
    cmonsters(CMIMaterial.CREEPER_SPAWN_EGG, FlagMode.Residence, "Allows or denys custom monster spawns", true),
    commandblock(CMIMaterial.COMMAND_BLOCK, FlagMode.Both, "Allows or denys command block interaction", false),
    command(CMIMaterial.COMMAND_BLOCK, FlagMode.Both, "Allows or denys comamnd use in residences", false),
    container(CMIMaterial.CHEST_MINECART, FlagMode.Both, "Allows or denys use of furnaces, chests, dispensers, etc...", true),
    coords(CMIMaterial.COMPASS, FlagMode.Residence, "Hides residence coordinates", true),
    copper(CMIMaterial.COPPER_BLOCK, FlagMode.Both, "Allows to modify copper blocks", true),
    coraldryup(CMIMaterial.FIRE_CORAL, FlagMode.Residence, "Allow or deny all corals from drying up", true),
    craft(CMIMaterial.STONE, FlagMode.Residence, "Gives table, enchant, brew flags", true),
    creeper(CMIMaterial.CREEPER_SPAWN_EGG, FlagMode.Residence, "Allow or deny creeper explosions", true),
    dragongrief(CMIMaterial.DIRT, FlagMode.Residence, "Prevents ender dragon block griefing", true),
    day(CMIMaterial.DANDELION, FlagMode.Residence, "Sets day time in residence", true),
    dye(CMIMaterial.ORANGE_DYE, FlagMode.Both, "Allows or denys sheep dyeing", true),
    damage(CMIMaterial.GOLDEN_SWORD, FlagMode.Residence, "Allows or denys all entity damage within the residence", false),
    decay(CMIMaterial.OAK_LEAVES, FlagMode.Residence, "Allows or denys leave decay in the residence", true),
    destroy(CMIMaterial.END_STONE, FlagMode.Both, "Allows or denys only destruction of blocks, overrides the build flag", true),
    dryup(CMIMaterial.BLUE_STAINED_GLASS_PANE, FlagMode.Residence, "Prevents land from drying up", true),
    diode(CMIMaterial.REPEATER, FlagMode.Both, "Allows or denys players to use redstone repeaters", true),
    door(CMIMaterial.OAK_DOOR, FlagMode.Both, "Allows or denys players to use doors and trapdoors", true),
    egg(CMIMaterial.EGG, FlagMode.Both, "Allows or denys interaction with dragon egg", true),
    enchant(CMIMaterial.ENCHANTING_TABLE, FlagMode.Both, "Allows or denys players to use enchanting tables", true),
    explode(CMIMaterial.TNT_MINECART, FlagMode.Residence, "Allows or denys explosions in residences", true),
    elytra(CMIMaterial.ELYTRA, FlagMode.Both, "Allows or denys elytra usage in residences", false),
    enderpearl(CMIMaterial.ENDER_PEARL, FlagMode.Both, "Allow or disallow teleporting to the residence with enderpearl", true),
    fallinprotection(CMIMaterial.SAND, FlagMode.Residence, "Protects from blocks falling into residence", true),
    falldamage(CMIMaterial.LEATHER_BOOTS, FlagMode.Residence, "Protects players from fall damage", true),
    feed(CMIMaterial.COOKED_BEEF, FlagMode.Residence, "Setting to true makes the residence feed its occupants", true),
    friendlyfire(CMIMaterial.SUNFLOWER, FlagMode.Player, "Allow or disallow friendly fire", false),
    fireball(CMIMaterial.FIRE_CHARGE, FlagMode.Residence, "Allows or denys fire balls in residences", true),
    firespread(CMIMaterial.BLAZE_POWDER, FlagMode.Residence, "Allows or denys fire spread", true),
    flowinprotection(CMIMaterial.OAK_BOAT, FlagMode.Residence, "Allows or denys liquid flow into residence", true),
    flow(CMIMaterial.LILY_PAD, FlagMode.Residence, "Allows or denys liquid flow", true),
    flowerpot(CMIMaterial.FLOWER_POT, FlagMode.Both, "Allows or denys interaction with flower pot", true),
    grow(CMIMaterial.WHEAT_SEEDS, FlagMode.Residence, "Allows or denys plant growing", true),
    glow(CMIMaterial.SEA_LANTERN, FlagMode.Residence, "Players will start glowing when entering residence", true),
    goathorn(CMIMaterial.GOAT_HORN, FlagMode.Both, "Allows or denys goat horn usage", true),
    golemopenchest(CMIMaterial.COPPER_GOLEM_STATUE, FlagMode.Residence, "Allow or deny golem open chest", true),
    harvest(CMIMaterial.SWEET_BERRIES, FlagMode.Both, "Allows harvesting", true),
    hotfloor(CMIMaterial.MAGMA_BLOCK, FlagMode.Residence, "Prevent damage from magma blocks", true),
    hidden(CMIMaterial.GLASS_PANE, FlagMode.Residence, "Hides residence from list or listall commands", false),
    hook(CMIMaterial.FISHING_ROD, FlagMode.Both, "Allows or denys fishing rod hooking entities", true),
    healing(CMIMaterial.POTION, FlagMode.Residence, "Setting to true makes the residence heal its occupants", true),
    iceform(CMIMaterial.ICE, FlagMode.Residence, "Prevents from ice forming", true),
    icemelt(CMIMaterial.ICE, FlagMode.Residence, "Prevents ice from melting", true),
    ignite(CMIMaterial.FLINT_AND_STEEL, FlagMode.Both, "Allows or denys fire ignition", false),
    itemdrop(CMIMaterial.FEATHER, FlagMode.Both, "Allows or denys item drop", true),
    itempickup(CMIMaterial.GUNPOWDER, FlagMode.Both, "Allows or denys item pickup", true),
    jump2(CMIMaterial.SLIME_BLOCK, FlagMode.Residence, "Allows to jump 2 blocks high", false),
    jump3(CMIMaterial.SLIME_BLOCK, FlagMode.Residence, "Allows to jump 3 blocks high", false),
    keepinv(CMIMaterial.LEATHER_HELMET, FlagMode.Residence, "Players keeps inventory after death", false),
    keepexp(CMIMaterial.GOLDEN_APPLE, FlagMode.Residence, "Players keeps exp after death", false),
    lavaflow(CMIMaterial.LAVA_BUCKET, FlagMode.Residence, "Allows or denys lava flow, overrides flow", true),
    leash(CMIMaterial.LEAD, FlagMode.Both, "Allows or denys aninal leash", true),
    lever(CMIMaterial.LEVER, FlagMode.Both, "Allows or denys players to use levers", true),
    mobexpdrop(CMIMaterial.MELON_SEEDS, FlagMode.Residence, "Prevents mob droping exp on death", true),
    mobitemdrop(CMIMaterial.COCOA_BEANS, FlagMode.Residence, "Prevents mob droping items on death", true),
    mobkilling(CMIMaterial.ROTTEN_FLESH, FlagMode.Both, "Allows or denys mob killing", true),
    monsters(CMIMaterial.SPAWNER, FlagMode.Residence, "Allows or denys monster spawns", true),
    move(CMIMaterial.LEATHER_BOOTS, FlagMode.Both, "Allows or denys movement in the residence", true),
    nametag(CMIMaterial.NAME_TAG, FlagMode.Both, "Allows or denys name tag usage", true),
    nanimals(CMIMaterial.COW_SPAWN_EGG, FlagMode.Residence, "Allows or denys natural animal spawns", true),
    nmonsters(CMIMaterial.SKELETON_SPAWN_EGG, FlagMode.Residence, "Allows or denys natural monster spawns", true),
    night(CMIMaterial.BLACK_WOOL, FlagMode.Residence, "Sets night time in residence", true),
    nofly(CMIMaterial.ORANGE_CARPET, FlagMode.Both, "Allows or denys fly in residence", false),
    fly(CMIMaterial.ORANGE_CARPET, FlagMode.Both, "Toggles fly for players in residence", false),
    nomobs(CMIMaterial.BARRIER, FlagMode.Residence, "Prevents monsters from entering residence. Requires AutoMobRemoval to be enabled", true),
    note(CMIMaterial.NOTE_BLOCK, FlagMode.Both, "Allows or denys players to use note blocks", true),
    nodurability(CMIMaterial.ANVIL, FlagMode.Residence, "Prevents item durability loss", false),
//    overridepvp(CMIMaterial.IRON_SWORD, FlagMode.Residence, "Overrides any plugin pvp protection", false),
    pressure(CMIMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE, FlagMode.Both, "Allows or denys players to use pressure plates", true),
    piston(CMIMaterial.PISTON, FlagMode.Residence, "Allow or deny pistons from pushing or pulling blocks in the residence", true),
    pistonprotection(CMIMaterial.STICKY_PISTON, FlagMode.Residence, "Enables or disabled piston block move in or out of residence", true),
    place(CMIMaterial.SEA_LANTERN, FlagMode.Both, "Allows or denys only placement of blocks, overrides the build flag", true),
    pvp(CMIMaterial.WOODEN_SWORD, FlagMode.Residence, "Allow or deny pvp in the residence", false),
    rain(CMIMaterial.BLUE_ORCHID, FlagMode.Residence, "Sets weather to rainny in residence", true),
    respawn(CMIMaterial.SUNFLOWER, FlagMode.Residence, "Automaticaly respawns player", false),
    riding(CMIMaterial.SADDLE, FlagMode.Both, "Prevent riding a horse", true),
    shoot(CMIMaterial.ARROW, FlagMode.Residence, "Allows or denys shooting projectile in area", true),
    sun(CMIMaterial.SUNFLOWER, FlagMode.Residence, "Sets weather to sunny in residence", true),
    shop(CMIMaterial.ITEM_FRAME, FlagMode.Residence, "Adds residence to special residence shop list", true),
    snowtrail(CMIMaterial.SNOW, FlagMode.Residence, "Prevents snowman snow trails", true),
    spread(CMIMaterial.SNOWBALL, FlagMode.Residence, "Prevents block spreading", true),
    skulk(CMIMaterial.SCULK_CATALYST, FlagMode.Residence, "Prevents skulk catalyst spreading", true),
    snowball(CMIMaterial.SNOWBALL, FlagMode.Residence, "Prevents snowball knockback", true),
    sanimals(CMIMaterial.RABBIT_SPAWN_EGG, FlagMode.Residence, "Allows or denys spawner or spawn egg animal spawns", true),
    shear(CMIMaterial.SHEARS, FlagMode.Both, "Allows or denys sheep shear", true),
    smonsters(CMIMaterial.ZOMBIE_SPAWN_EGG, FlagMode.Residence, "Allows or denys spawner or spawn egg monster spawns", true),
    subzone(CMIMaterial.GRAY_STAINED_GLASS_PANE, FlagMode.Both, "Allow a player to make subzones in the residence", true),
    title(CMIMaterial.PAPER, FlagMode.Residence, "Shows or hides enter/leave message in residence", true),
    table(CMIMaterial.CRAFTING_TABLE, FlagMode.Both, "Allows or denys players to use workbenches", true),
    tnt(CMIMaterial.TNT, FlagMode.Residence, "Allow or deny tnt explosions", false),
    tp(CMIMaterial.END_PORTAL_FRAME, FlagMode.Both, "Allow or disallow teleporting to the residence", true),
    trade(CMIMaterial.EMERALD, FlagMode.Both, "Allows or denys villager trading in residence", true),
    trample(CMIMaterial.DIRT, FlagMode.Residence, "Allows or denys crop trampling in residence", true),
    use(CMIMaterial.STONE_PRESSURE_PLATE, FlagMode.Both, "Allows or denys use of doors, lever, buttons, etc...", true),
    visualizer(CMIMaterial.GLOWSTONE_DUST, FlagMode.Residence, "Shows or hides particle visualizer when entering residence", true),
    vehicledestroy(CMIMaterial.MINECART, FlagMode.Both, "Allows or denys vehicle destroy", true),
    vehicleplacing(CMIMaterial.MINECART, FlagMode.Both, "Allows or denys vehicle placing", true),
    witherspawn(CMIMaterial.WITHER_SKELETON_SKULL, FlagMode.Residence, "Allows or denys wither spawning", true),
    phantomspawn(CMIMaterial.BROWN_WOOL, FlagMode.Residence, "Allows or denys phantom spawning", true),
    witherdamage(CMIMaterial.WITHER_SKELETON_SKULL, FlagMode.Residence, "Allows or denys wither damage", true),
    witherdestruction(CMIMaterial.WITHER_SKELETON_SKULL, FlagMode.Residence, "Allows or denys wither block damage", true),
    waterflow(CMIMaterial.WATER_BUCKET, FlagMode.Residence, "Allows or denys water flow, overrides flow", true),
    wspeed1(CMIMaterial.POTION, FlagMode.Residence, "Change players walk speed in residence to %1", true),
    wspeed2(CMIMaterial.POTION, FlagMode.Residence, "Change players walk speed in residence to %1", true);

    private String translated = null;
    private CMIMaterial icon;
    private FlagMode flagMode;
    private String desc;
    private boolean enabled;
    private boolean globalyEnabled = true;
    private Set<String> groups = null;

    public static enum FlagMode {
        Player, Residence, Both
    }

    private Flags(CMIMaterial icon, FlagMode flagMode, String desc, boolean enabled) {
        this.icon = icon;
        this.flagMode = flagMode;
        this.desc = desc;
        this.enabled = enabled;
    }

    public String getName() {
        return getTranslated() == null ? this.name() : getTranslated();
    }

    public FlagMode getFlagMode() {
        return flagMode;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isEnabled() {
        return globalyEnabled ? enabled : false;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static Flags getFlag(String flag) {
        for (Flags f : Flags.values()) {
            if (f.toString().equalsIgnoreCase(flag))
                return f;
            if (f.getTranslated() != null && f.getTranslated().equalsIgnoreCase(flag))
                return f;
        }
        return null;
    }

    public boolean isGlobalyEnabled() {
        return globalyEnabled;
    }

    public void setGlobalyEnabled(boolean globalyEnabled) {
        this.globalyEnabled = globalyEnabled;
    }

    public String getTranslated() {
        return translated;
    }

    public void setTranslated(String translated) {
        this.translated = translated == null ? null : translated.replace(" ", "");
    }

    public CMIMaterial getIcon() {
        return icon;
    }

    public void setIcon(CMIMaterial icon) {
        this.icon = icon;
    }

    public boolean isInGroup(String group) {
        if (groups == null)
            return false;

        return groups.contains(group.toLowerCase());
    }

    public void addGroup(String group) {
        if (groups == null)
            groups = new HashSet<String>();
        groups.add(group.toLowerCase());
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void resetGroups() {
        this.groups = null;
    }
}
