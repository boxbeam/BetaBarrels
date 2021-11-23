package redempt.betabarrels;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import redempt.redlib.commandmanager.CommandParser;

public class BetaBarrels extends JavaPlugin implements Listener {
	
	public static BetaBarrels plugin;
	
	@Override
	public void onEnable() {
		plugin = this;
		Bukkit.getPluginManager().registerEvents(this, this);
		new CommandParser(this.getResource("command.txt")).parse().register("betabarrels", this);
		Barrel.init();
		new BarrelListener();
	}
	
	@Override
	public void onDisable() {
		Barrel.save();
	}
	
}
