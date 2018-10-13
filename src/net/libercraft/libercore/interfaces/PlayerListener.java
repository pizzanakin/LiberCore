package net.libercraft.libercore.interfaces;

import org.bukkit.entity.Player;

import net.libercraft.libercore.managers.PlayerManager;

public interface PlayerListener {
	public abstract void onLeftClick(Player player);
	public abstract void onRightClick(Player player);
	public abstract void onDoubleRightClick(Player player);
	public abstract void onRegister(Player player);
	public abstract void onLeave(Player player);
	
	public default void registerPlayerListener() {
		PlayerManager.listeners.add(this);
	}
}
