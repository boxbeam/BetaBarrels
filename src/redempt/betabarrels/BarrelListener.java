package redempt.betabarrels;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import redempt.redlib.blockdata.events.DataBlockDestroyEvent;
import redempt.redlib.itemutils.ItemUtils;
import redempt.redlib.misc.LocationUtils;
import redempt.redlib.misc.Task;

public class BarrelListener implements Listener {
	
	public BarrelListener() {
		Bukkit.getPluginManager().registerEvents(this, BetaBarrels.plugin);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent e) {
		if (e.getBlock().getType() != Material.BARREL) {
			return;
		}
		Task.syncDelayed(BetaBarrels.plugin, () -> {
			Barrel barrel = Barrel.getAt(e.getBlock());
			barrel.createFrame();
			ItemStack item = e.getItemInHand();
			BarrelItem.handlePlace(barrel, item);
		});
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent e) {
		if (e.getClickedBlock() == null || e.getHand() != EquipmentSlot.HAND) {
			return;
		}
		Barrel barrel = Barrel.getAt(e.getClickedBlock());
		if (barrel == null) {
			return;
		}
		boolean left = e.getAction().toString().startsWith("LEFT");
		if (!left && e.getPlayer().isSneaking() && !(barrel.getItem() == null || barrel.getItem().isSimilar(e.getItem()))) {
			return;
		}
		barrel.interact(e.getPlayer(), e.getAction().toString().startsWith("LEFT"), e.getPlayer().isSneaking());
		if (!left) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (e.getView() instanceof BarrelInventoryView) {
			return;
		}
		if (Barrel.getBarrel(e.getInventory()) != null) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public Barrel onBreakFrame(HangingBreakEvent e) {
		Block block = e.getEntity().getLocation().getBlock().getRelative(e.getEntity().getAttachedFace());
		Barrel barrel = Barrel.getAt(block);
		if (barrel == null) {
			return null;
		}
		if (!e.getEntity().equals(barrel.getFrame())) {
			return null;
		}
		e.setCancelled(true);
		return barrel;
	}
	
	@EventHandler
	public void onBreakFrameByEntity(HangingBreakByEntityEvent e) {
		Barrel barrel = onBreakFrame(e);
		if (barrel == null) {
			return;
		}
		if (e.getRemover() instanceof Player) {
			Player player = (Player) e.getRemover();
			barrel.interact(player, true, player.isSneaking());
		}
	}
	
	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent e) {
		if (!(e.getRightClicked() instanceof ItemFrame)) {
			return;
		}
		ItemFrame frame = (ItemFrame) e.getRightClicked();
		Block block = frame.getLocation().getBlock().getRelative(frame.getAttachedFace());
		Barrel barrel = Barrel.getAt(block);
		if (barrel == null) {
			return;
		}
		barrel.interact(e.getPlayer(), false, e.getPlayer().isSneaking());
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof ItemFrame)) {
			return;
		}
		ItemFrame frame = (ItemFrame) e.getEntity();
		Block block = frame.getLocation().getBlock().getRelative(frame.getAttachedFace());
		Barrel barrel = Barrel.getAt(block);
		if (barrel == null) {
			return;
		}
		e.setCancelled(true);
		if (!(e.getDamager() instanceof Player)) {
			return;
		}
		Player player = (Player) e.getDamager();
		barrel.interact(player, true, player.isSneaking());
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(DataBlockDestroyEvent e) {
		Barrel barrel = Barrel.getAt(e.getBlock());
		if (barrel == null) {
			return;
		}
		int count = barrel.getCount();
		ItemStack item = barrel.getItem();
		barrel.getInventory().clear();
		ItemFrame frame = barrel.getFrame();
		if (frame != null) {
			frame.setItem(null);
			frame.remove();
		}
		if (count == 0 || item == null) {
			return;
		}
		if (BetaBarrelsConfig.barrelDropSelf) {
			e.getBlock().getWorld().dropItem(LocationUtils.center(e.getBlock()), BarrelItem.get(item, count));
			return;
		}
		int stackSize = item.getMaxStackSize();
		while (count > 0) {
			int toDrop = Math.min(count, stackSize);
			ItemStack drop = item.clone();
			drop.setAmount(toDrop);
			e.getBlock().getWorld().dropItem(LocationUtils.center(e.getBlock()), drop);
			count -= toDrop;
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHopperMove(InventoryMoveItemEvent e) {
		if (e.getDestination().getHolder() instanceof HopperMinecart && e.getSource().getHolder() instanceof BlockState) {
			Barrel barrel = Barrel.getBarrel(e.getSource());
			if (barrel != null) {
				e.setCancelled(true);
			}
			return;
		}
		if (!(e.getSource().getHolder() instanceof BlockState) || !(e.getDestination().getHolder() instanceof BlockState)) {
			return;
		}
		Barrel dest = Barrel.getBarrel(e.getDestination());
		if (dest != null) {
			e.setCancelled(true);
			ItemStack item = dest.getItem();
			ItemStack it = e.getItem().clone();
			if (item != null && !it.isSimilar(item)) {
				return;
			}
			Task.syncDelayed(BetaBarrels.plugin, () -> {
				ItemUtils.remove(e.getSource(), it, it.getAmount());
				if (item == null) {
					dest.setItem(it);
				}
				dest.setCount(dest.getCount() + it.getAmount());
			});
		}
		Barrel src = Barrel.getBarrel(e.getSource());
		if (src == null) {
			return;
		}
		int count = src.getCount();
		count--;
		src.setCount(count);
		if (count != 0) {
			Task.syncDelayed(BetaBarrels.plugin, () -> src.getInventory().addItem(src.getItem()));
		}
	}
	
}
