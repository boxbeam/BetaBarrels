package redempt.betabarrels;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import redempt.redlib.commandmanager.CommandParser;
import redempt.redlib.config.ConfigManager;
import redempt.redlib.itemutils.ItemUtils;
import redempt.redlib.misc.FormatUtils;

public class BetaBarrels extends JavaPlugin implements Listener {
	
	public static BetaBarrels plugin;
	
	
	public static String getItemName(ItemStack item) {
		if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
			return FormatUtils.toTitleCase(item.getType().toString().replace("_", " "));
		}
		return item.getItemMeta().getDisplayName();
	}
	
	public static String getDisplay(ItemStack item, int count) {
		return ChatColor.WHITE + "" + FormatUtils.formatLargeInteger(count) + "x " + getItemName(item);
	}
	
	@Override
	public void onEnable() {
		plugin = this;
		Bukkit.getPluginManager().registerEvents(this, this);
		new CommandParser(this.getResource("command.txt")).parse().register("betabarrels", this);
		Barrel.init();
		new BarrelListener();
		ConfigManager.create(this).target(BetaBarrelsConfig.class).saveDefaults().load();
	}
	
	@Override
	public void onDisable() {
		Barrel.save();
	}
	
}
