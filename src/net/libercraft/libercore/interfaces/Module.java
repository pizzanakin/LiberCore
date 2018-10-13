package net.libercraft.libercore.interfaces;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Module extends JavaPlugin {
	
	public abstract ChatColor colour();
	public abstract void onActivate();
	public abstract void onClose();
	
	private boolean activated = false;
	
	public String tag() {
		return "["+name()+"]";
	}
	
	public String name() {
		return this.getClass().getSimpleName();
	}
	
	public void activate() {
		activated = true;
		onActivate();
	}
	
	public void close() {
		activated = false;
		onClose();
	}
	
	public boolean isActivated() {
		return activated;
	}
}
