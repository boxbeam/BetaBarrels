package redempt.betabarrels;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import redempt.betabarrels.BarrelUseEvent.BarrelAction;
import redempt.redlib.blockdata.BlockDataManager;
import redempt.redlib.blockdata.DataBlock;
import redempt.redlib.itemutils.ItemUtils;
import redempt.redlib.misc.FormatUtils;

public class Barrel {
	
	private static BlockDataManager manager;
	
	public static void init() {
		BetaBarrels.plugin.getDataFolder().mkdirs();
		manager = new BlockDataManager(BetaBarrels.plugin.getDataFolder().toPath().resolve("barrels.db"));
	}
	
	public static void save() {
		manager.saveAndClose();
	}
	
	public static Barrel getAt(Block block) {
		if (block.getType() != Material.BARREL) {
			return null;
		}
		DataBlock db = manager.getDataBlock(block);
		return new Barrel(db);
	}
	
	public static Barrel getBarrel(Inventory inventory) {
		InventoryHolder holder = inventory.getHolder();
		if (!(holder instanceof BlockState)) {
			return null;
		}
		return getAt(((BlockState) holder).getBlock());
	}
	
	private DataBlock db;
	
	private Barrel(DataBlock db) {
		this.db = db;
	}
	
	public int getCount() {
		Integer count = (Integer) db.get("count");
		return count == null ? 0 : count;
	}
	
	public void setCount(int count) {
		db.set("count", count);
		if (count == 0) {
			setItem(null);
		}
		updateFrame();
	}
	
	public void setItem(ItemStack item) {
		if (item == null) {
			db.set("item", null);
			getInventory().clear();
			return;
		}
		item = item.clone();
		item.setAmount(1);
		Inventory inv = getInventory();
		inv.clear();
		inv.addItem(item);
		db.set("item", ItemUtils.toString(item));
		updateFrame();
	}
	
	public Inventory getInventory() {
		BlockState state = db.getBlock().getState();
		InventoryHolder holder = (InventoryHolder) state;
		return holder.getInventory();
	}
	
	public ItemStack getItem() {
		String prop = db.getString("item");
		return prop == null ? null : ItemUtils.fromString(prop);
	}
	
	public BlockFace getFace() {
		BlockData data = db.getBlock().getBlockData();
		Directional d = (Directional) data;
		return d.getFacing();
	}
	
	public ItemFrame getFrame() {
		BlockFace face = getFace();
		if (face == null) {
			return null;
		}
		Location loc = db.getBlock().getLocation();
		return loc.getWorld().getNearbyEntities(loc, 3, 3, 3, e -> e instanceof ItemFrame).stream().map(e -> (ItemFrame) e)
				.filter(i -> i.getLocation().getBlock().getRelative(i.getAttachedFace()).equals(db.getBlock()))
				.findFirst().orElse(null);
	}
	
	public void removeFrame() {
		ItemFrame frame = getFrame();
		if (frame != null) {
			frame.remove();
		}
	}
	
	public ItemFrame createFrame() {
		removeFrame();
		BlockFace face = getFace();
		Location loc = db.getBlock().getRelative(face).getLocation();
		ItemFrame frame = (ItemFrame) loc.getWorld().spawnEntity(loc, EntityType.ITEM_FRAME);
		frame.setVisible(false);
		frame.setFacingDirection(face.getOppositeFace());
		updateFrame();
		return frame;
	}
	
	public void updateFrame() {
		ItemFrame frame = getFrame();
		if (frame == null) {
			frame = createFrame();
		}
		ItemStack item = getItem();
		int count = getCount();
		if (item == null || count == 0) {
			frame.setItem(null);
			return;
		}
		String name = getItemName(item);
        if (count < 2) {
            item = ItemUtils.setName(item, ChatColor.WHITE + "" + FormatUtils.formatLargeInteger(count) + "x " + name);
        } else {
            item = ItemUtils.setName(item, ChatColor.WHITE + "" + FormatUtils.formatLargeInteger(count) + "x " + name);
        }

		frame.setItem(item, false);
		frame.setRotation(Rotation.NONE);
		frame.setVisible(false);
	}
	
	private String getItemName(ItemStack item) {
		if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
			return FormatUtils.toTitleCase(item.getType().toString().replace("_", " "));
		}
		return item.getItemMeta().getDisplayName();
	}
	
	public void interact(Player player, boolean left, boolean shift) {
		if (!left && !shift) {
			insertStack(player);
			return;
		}
		if (!left) {
			insertAll(player);
			return;
		}
		if (!shift) {
			extractOne(player);
			return;
		}
		extractStack(player);
	}
	
	public void insertStack(Player player) {
		if (!fireEvent(player, BarrelAction.DEPOSIT_STACK)) {
			return;
		}
		ItemStack item = player.getInventory().getItemInMainHand();
		ItemStack held = getItem();
		if (held == null || getCount() == 0) {
			setItem(item);
			held = item;
		}
		if (!item.isSimilar(held)) {
			return;
		}
		setCount(getCount() + item.getAmount());
		player.getInventory().setItemInMainHand(null);
	}
	
	public void insertAll(Player player) {
		if (!fireEvent(player, BarrelAction.DEPOSIT_ALL)) {
			return;
		}
		ItemStack item = player.getInventory().getItemInMainHand();
		ItemStack held = getItem();
		if (held == null || getCount() == 0) {
			setItem(item);
			held = item;
		}
		if (!item.isSimilar(held)) {
			return;
		}
		int count = ItemUtils.countAndRemove(player.getInventory(), item);
		setCount(getCount() + count);
	}
	
	public void extractOne(Player player) {
		if (!fireEvent(player, BarrelAction.TAKE_ONE)) {
			return;
		}
		int count = getCount();
		ItemStack item = getItem();
		if (count <= 0 || item == null) {
			return;
		}
		setCount(count - 1);
		ItemUtils.give(player, item);
	}
	
	public void extractStack(Player player) {
		if (!fireEvent(player, BarrelAction.TAKE_STACK)) {
			return;
		}
		ItemStack item = getItem();
		if (item == null) {
			return;
		}
		int amount = item.getType().getMaxStackSize();
		int count = getCount();
		amount = Math.min(amount, count);
		if (amount == 0) {
			return;
		}
		setCount(count - amount);
		ItemUtils.give(player, item, amount);
	}
	
	private boolean fireEvent(Player player, BarrelAction action) {
		Event event = new InventoryOpenEvent(new BarrelInventoryView(player, getInventory()));
		Bukkit.getPluginManager().callEvent(event);
		if (((Cancellable) event).isCancelled()) {
			return false;
		}
		event = new BarrelUseEvent(player, this, action);
		Bukkit.getPluginManager().callEvent(event);
		return !((Cancellable) event).isCancelled();
	}
	
	public Block getBlock() {
		return db.getBlock();
	}
	
}
