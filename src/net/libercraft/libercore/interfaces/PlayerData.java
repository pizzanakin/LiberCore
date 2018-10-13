package net.libercraft.libercore.interfaces;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.libercraft.libercore.managers.PlayerManager;

public interface PlayerData extends Updatable {
	
	public default Player getPlayer() {
		for (Map.Entry<Player, List<PlayerData>> entry:PlayerManager.players.entrySet()) 
			if (entry.getValue().contains(this))
				return entry.getKey();
		return null;
	}
	
	public default void initialisePlayerData(Player player) {
		registerUpdatable();
		List<PlayerData> data = PlayerManager.get(player);
		data.add(this);
		PlayerManager.players.put(player, data);
	}
	
	public default ItemStack getMainHandItem() {
		return getPlayer().getInventory().getItemInMainHand();
	}
	
	public default ItemStack getOffHandItem() {
		return getPlayer().getInventory().getItemInOffHand();
	}
}
