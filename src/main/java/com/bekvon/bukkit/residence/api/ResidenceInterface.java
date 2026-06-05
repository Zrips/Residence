package com.bekvon.bukkit.residence.api;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 * Residence 领地管理 API。
 * <p>
 * 该接口提供外部插件常用的领地查询、商店领地列表维护和领地创建能力。
 * 可通过 {@link ResidenceApi#getResidenceManager()} 获取实例。
 * </p>
 * <p>
 * 注意：此接口主要暴露高层 API。领地范围更新、权限变更、拥有者变更等操作，
 * 通常需要先通过 {@link #getByName(String)} 或 {@link #getByLoc(Location)}
 * 取得 {@link ClaimedResidence} 后，再调用 {@link ClaimedResidence} 上的对应方法。
 * </p>
 */
public interface ResidenceInterface {
    /**
     * 根据指定位置获取当前位置所属的领地。
     * <p>
     * 如果该位置位于子领地内，返回子领地；否则返回主领地。
     * 如果位置为空、世界为空、对应区块没有领地，或位置不在任何领地内，则返回 {@code null}。
     * </p>
     *
     * @param loc 要查询的 Bukkit 坐标
     * @return 该位置所属的领地或子领地；不存在时返回 {@code null}
     */
    public ClaimedResidence getByLoc(Location loc);

    /**
     * 根据领地名称获取领地对象。
     * <p>
     * 名称大小写不敏感。子领地可使用点号路径查询，例如 {@code main.sub}。
     * 如果名称为空、主领地不存在，或路径中的任一级子领地不存在，则返回 {@code null}。
     * </p>
     *
     * @param name 领地名称或子领地路径
     * @return 对应的领地对象；不存在时返回 {@code null}
     */
    public ClaimedResidence getByName(String name);

    /**
     * 将指定领地加入商店领地缓存列表。
     * <p>
     * 该方法只维护内存中的商店领地列表，不会自动检查领地是否具有商店标记，
     * 也不会修改领地权限。调用方应确保传入的领地确实需要作为商店领地处理。
     * </p>
     *
     * @param res 要加入商店列表的领地
     */
    public void addShop(ClaimedResidence res);

    /**
     * 根据领地名称将领地加入商店领地缓存列表。
     * <p>
     * 如果名称无法找到对应领地，则不会添加任何内容。
     * 建议新代码优先使用 {@link #addShop(ClaimedResidence)}，避免重复名称查询。
     * </p>
     *
     * @param res 领地名称
     * @deprecated 请优先使用 {@link #addShop(ClaimedResidence)}
     */
    @Deprecated
    public void addShop(String res);

    /**
     * 从商店领地缓存列表中移除指定领地。
     * <p>
     * 该方法只维护内存中的商店领地列表，不会修改领地权限或商店标记。
     * </p>
     *
     * @param res 要移除的领地
     */
    public void removeShop(ClaimedResidence res);

    /**
     * 根据领地名称从商店领地缓存列表中移除领地。
     * <p>
     * 名称匹配大小写不敏感。若列表中不存在对应领地，则不会产生变化。
     * 建议新代码优先使用 {@link #removeShop(ClaimedResidence)}。
     * </p>
     *
     * @param res 领地名称
     * @deprecated 请优先使用 {@link #removeShop(ClaimedResidence)}
     */
    @Deprecated
    public void removeShop(String res);

    /**
     * 获取当前已缓存的商店领地列表。
     * <p>
     * 返回值为内部列表对象，调用方不应直接修改该列表，除非明确需要维护商店领地缓存。
     * </p>
     *
     * @return 商店领地列表
     */
    public List<ClaimedResidence> getShops();

    /**
     * 以服务器领地身份创建领地。
     * <p>
     * 该方法不绑定玩家，默认按管理员方式创建，不扣除经济费用。
     * 创建时仍会执行名称校验、区域校验、碰撞检测，并触发领地创建事件。
     * </p>
     *
     * @param name 领地名称
     * @param loc1 领地区域的一个角点
     * @param loc2 领地区域的另一个角点
     * @return 创建成功返回 {@code true}，校验失败或事件取消返回 {@code false}
     * @deprecated 请优先使用带 {@link Player} 的创建方法，以便正确应用玩家权限、限制和消息反馈
     */
    @Deprecated
    public boolean addResidence(String name, Location loc1, Location loc2);

    /**
     * 以指定拥有者名称创建领地。
     * <p>
     * 该方法不绑定玩家，默认按管理员方式创建，不扣除经济费用。
     * 如果拥有者为空，内部会使用服务器领地身份。
     * 创建时仍会执行名称校验、区域校验、碰撞检测，并触发领地创建事件。
     * </p>
     *
     * @param name 领地名称
     * @param owner 领地拥有者名称
     * @param loc1 领地区域的一个角点
     * @param loc2 领地区域的另一个角点
     * @return 创建成功返回 {@code true}，校验失败或事件取消返回 {@code false}
     * @deprecated 请优先使用带 {@link Player} 的创建方法，以便正确应用玩家权限、限制和消息反馈
     */
    @Deprecated
    public boolean addResidence(String name, String owner, Location loc1, Location loc2);

    /**
     * 使用指定玩家和两个角点创建领地。
     * <p>
     * 这是外部插件创建玩家领地时推荐使用的方法。内部会根据 {@code resadmin}
     * 决定是否跳过普通玩家限制；非管理员创建时会检查创建权限、领地数量限制、
     * 区域大小限制、碰撞、经济扣费等规则。
     * </p>
     * <p>
     * 创建成功后会设置默认权限、默认进入/离开提示、创建时间、主区域 {@code main}，
     * 并更新区块索引和玩家领地缓存。
     * </p>
     *
     * @param player 创建领地的玩家
     * @param name 领地名称
     * @param loc1 领地区域的一个角点
     * @param loc2 领地区域的另一个角点
     * @param resadmin 是否按管理员模式创建；为 {@code true} 时跳过普通玩家限制和扣费
     * @return 创建成功返回 {@code true}，校验失败、扣费失败或事件取消返回 {@code false}
     */
    public boolean addResidence(Player player, String name, Location loc1, Location loc2, boolean resadmin);

    /**
     * 使用玩家当前选择区域创建领地。
     * <p>
     * 该方法会读取 Residence 选择管理器中玩家已选择的两个点，
     * 创建逻辑与 {@link #addResidence(Player, String, Location, Location, boolean)} 一致。
     * 调用前应确保玩家已经完成区域选择，否则创建会失败。
     * </p>
     *
     * @param player 创建领地的玩家
     * @param name 领地名称
     * @param resadmin 是否按管理员模式创建；为 {@code true} 时跳过普通玩家限制和扣费
     * @return 创建成功返回 {@code true}，选择不完整、校验失败、扣费失败或事件取消返回 {@code false}
     */
    public boolean addResidence(Player player, String name, boolean resadmin);
}
