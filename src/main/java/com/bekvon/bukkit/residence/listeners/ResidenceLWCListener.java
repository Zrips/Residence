package com.bekvon.bukkit.residence.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.ResidenceManager.ChunkRef;
import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.event.LWCAccessEvent;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCDropItemEvent;
import com.griefcraft.scripting.event.LWCEntityInteractEvent;
import com.griefcraft.scripting.event.LWCMagnetPullEvent;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEntityEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEntityEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.scripting.event.LWCProtectionRegistrationPostEvent;
import com.griefcraft.scripting.event.LWCProtectionRemovePostEvent;
import com.griefcraft.scripting.event.LWCRedstoneEvent;
import com.griefcraft.scripting.event.LWCReloadEvent;
import com.griefcraft.scripting.event.LWCSendLocaleEvent;

import net.Zrips.CMILib.Version.Version;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class ResidenceLWCListener implements com.griefcraft.scripting.Module {

	public static void register(Plugin plugin) {
		com.griefcraft.lwc.LWC.getInstance().getModuleLoader().registerModule(plugin, new ResidenceLWCListener());
	}

	private static CompletableFuture<Integer> removeLwcFromResidence(final ClaimedResidence res) {

		if (Version.isCurrentLower(Version.v1_13_R1))
			return CompletableFuture.completedFuture(0);

		CuboidArea[] arr = res.getAreaArray();
		List<Supplier<CompletableFuture<Integer>>> tasks = new ArrayList<>();

		for (CuboidArea area : arr) {
			Location low = area.getLowLocation().clone();
			Location high = area.getHighLocation().clone();

			World world = low.getWorld();

			for (ChunkRef chunkRef : area.getChunks()) {
				tasks.add(() -> {

					CompletableFuture<Integer> f = new CompletableFuture<>();

					CMIScheduler.runAtLocation(
							Residence.getInstance(),
							high.getWorld(),
							chunkRef.getX(),
							chunkRef.getZ(),
							() -> {
								int cleaned = cleaner(chunkRef, world, high, low);
								f.complete(cleaned);
							});

					return f;
				});
			}
		}

		CompletableFuture<Integer> chain = CompletableFuture.completedFuture(0);

		for (Supplier<CompletableFuture<Integer>> task : tasks) {
			chain = chain.thenCompose(totalSoFar -> {
				if (!Residence.getInstance().isEnabled() || Bukkit.getServer().isStopping())
					return CompletableFuture.completedFuture(totalSoFar);

				return task.get().thenApply(result -> totalSoFar + result);
			}).thenCompose(updated -> delay().thenApply(v -> updated));
		}

		return chain;
	}

	private static CompletableFuture<Void> delay() {
		CompletableFuture<Void> future = new CompletableFuture<>();
		CMIScheduler.runTaskLater(Residence.getInstance(), () -> future.complete(null), 1);
		return future;
	}

	private static int cleaner(ChunkRef chunkRef, World world, Location high, Location low) {

		if (Version.isCurrentLower(Version.v1_13_R1))
			return 0;

		LWC lwc = LWC.getInstance();

		if (lwc == null)
			return 0;

		if (!Residence.getInstance().isEnabled() || Bukkit.getServer().isStopping())
			return 0;

		Set<Location> locations = ConcurrentHashMap.newKeySet();

		ChunkSnapshot chunkSnapshot = null;

		for (int x = chunkRef.getX() * 16; x <= chunkRef.getX() * 16 + 15; x++) {
			for (int z = chunkRef.getZ() * 16; z <= chunkRef.getZ() * 16 + 15; z++) {

				// Limit to exact residence area
				if (x < low.getBlockX() || x > high.getBlockX() || z < low.getBlockZ() || z > high.getBlockZ())
					continue;

				int hy = world.getHighestBlockYAt(x, z);
				if (high.getBlockY() < hy)
					hy = high.getBlockY();

				int cx = x & 15;
				int cz = z & 15;

				if (chunkSnapshot == null) {
					@NotNull
					Chunk chunk = world.getBlockAt(x, 0, z).getChunk();
					if (chunk.isLoaded()) {
						chunkSnapshot = chunk.getChunkSnapshot();
					} else {
						chunk.load();
						chunkSnapshot = chunk.getChunkSnapshot(true, true, false);
						chunk.unload();
					}
				}

				for (int y = low.getBlockY(); y <= hy; y++) {
					@NotNull
					Material type = chunkSnapshot.getBlockType(cx, y, cz);

					if (!Residence.getInstance().getConfigManager().getLwcMatList().contains(type))
						continue;

					locations.add(new Location(world, x, y, z));
				}
			}
		}

		com.griefcraft.cache.ProtectionCache cache = lwc.getProtectionCache();

		int i = 0;

		for (Location one : locations) {
			if (!Residence.getInstance().isEnabled() || Bukkit.getServer().isStopping())
				continue;

			com.griefcraft.model.Protection prot = cache.getProtection(one.getBlock());
			if (prot == null)
				continue;
			prot.remove();
			i++;
		}

		return i;
	}

	public static void removeLwcFromResidence(final Player player, final ClaimedResidence res) {

		if (Version.isCurrentLower(Version.v1_13_R1))
			return;

		long time = System.currentTimeMillis();
		removeLwcFromResidence(res).thenAccept(i -> {
			if (i > 0)
				lm.Residence_LwcRemoved.sendMessage(player, i, System.currentTimeMillis() - time);
		});
	}

	@Override
	public void load(LWC lwc) {
	}

	@Override
	public void onReload(LWCReloadEvent event) {
	}

	@Override
	public void onAccessRequest(LWCAccessEvent event) {
	}

	@Override
	public void onDropItem(LWCDropItemEvent event) {
	}

	@Override
	public void onCommand(LWCCommandEvent event) {
	}

	@Override
	public void onRedstone(LWCRedstoneEvent event) {
	}

	@Override
	public void onDestroyProtection(LWCProtectionDestroyEvent event) {
	}

	@Override
	public void onProtectionInteract(LWCProtectionInteractEvent event) {
	}

	@Override
	public void onBlockInteract(LWCBlockInteractEvent event) {
	}

	@Override
	public void onRegisterProtection(LWCProtectionRegisterEvent event) {
		Player player = event.getPlayer();
		FlagPermissions perms = FlagPermissions.getPerms(event.getBlock().getLocation(), player);
		boolean hasuse = perms.playerHas(player, Flags.use, true);
		if (!perms.playerHas(player, Flags.container, hasuse) && !ResPerm.bypass_container.hasPermission(player, 10000L)) {
			event.setCancelled(true);
			lm.Flag_Deny.sendMessage(player, Flags.container);
		}
	}

	@Override
	public void onPostRegistration(LWCProtectionRegistrationPostEvent event) {
	}

	@Override
	public void onPostRemoval(LWCProtectionRemovePostEvent event) {
	}

	@Override
	public void onSendLocale(LWCSendLocaleEvent event) {
	}

	@Override
	public void onEntityInteract(LWCEntityInteractEvent arg0) {
	}

	@Override
	public void onEntityInteractProtection(LWCProtectionInteractEntityEvent arg0) {
	}

	@Override
	public void onMagnetPull(LWCMagnetPullEvent arg0) {
	}

	@Override
	public void onRegisterEntity(LWCProtectionRegisterEntityEvent arg0) {
	}

}
