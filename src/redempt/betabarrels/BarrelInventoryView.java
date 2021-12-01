package redempt.betabarrels;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

public class BarrelInventoryView extends InventoryView {
	
	private Player player;
	private Inventory inv;
	
	public BarrelInventoryView(Player player, Inventory inv) {
		this.player = player;
		this.inv = inv;
	}
	
	@Override
	public Inventory getTopInventory() {
		return inv;
	}
	
	@Override
	public Inventory getBottomInventory() {
		return player.getInventory();
	}
	
	@Override
	public HumanEntity getPlayer() {
		return player;
	}
	
	@Override
	public InventoryType getType() {
		return InventoryType.BARREL;
	}
	
	@Override
	public String getTitle() {
		return "Barrel";
	}
	
}
