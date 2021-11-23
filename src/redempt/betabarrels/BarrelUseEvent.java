package redempt.betabarrels;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class BarrelUseEvent extends PlayerEvent implements Cancellable {
	
	private static HandlerList handlers = new HandlerList();
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private boolean cancelled = false;
	private BarrelAction action;
	private Barrel barrel;
	
	public BarrelUseEvent(Player player, Barrel barrel, BarrelAction action) {
		super(player);
		this.barrel = barrel;
		this.action = action;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public Barrel getBarrel() {
		return barrel;
	}
	
	public BarrelAction getAction() {
		return action;
	}
	
	public enum BarrelAction {
		
		TAKE_ONE,
		TAKE_STACK,
		DEPOSIT_STACK,
		DEPOSIT_ALL
		
	}
	
}
