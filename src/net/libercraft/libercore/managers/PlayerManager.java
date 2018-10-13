package net.libercraft.libercore.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.libercraft.libercore.LiberCore;
import net.libercraft.libercore.interfaces.PlayerData;
import net.libercraft.libercore.interfaces.PlayerListener;

public class PlayerManager implements Listener {
	public static Map<Player, List<PlayerData>> players;
	public static List<Player> rightClick;
	public static List<PlayerListener> listeners;
	
	public PlayerManager() {
		players = new HashMap<Player, List<PlayerData>>();
		rightClick = new ArrayList<Player>();
		listeners = new ArrayList<PlayerListener>();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player p:LiberCore.get().getServer().getOnlinePlayers())
					registerPlayer(p);
			}
		}.runTaskLater(LiberCore.get(), 0);
	}
	
	public static List<PlayerData> get(Player player) {
		if (players.containsKey(player))
			return players.get(player);
		registerPlayer(player);
		return get(player);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		registerPlayer(e.getPlayer());
	}
	
	private static void registerPlayer(Player player) {
		players.put(player, new ArrayList<PlayerData>());
		for (PlayerListener l:listeners)
			l.onRegister(player);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		players.remove(e.getPlayer());
	}
	
	@EventHandler
	public void onClickEvent(PlayerInteractEvent e) {

		// -- Register Action --
		if (e.getAction().equals(Action.LEFT_CLICK_AIR)|e.getAction().equals(Action.LEFT_CLICK_BLOCK))
			for (PlayerListener l:listeners)
				l.onLeftClick(e.getPlayer());
		
		if (e.getAction().equals(Action.RIGHT_CLICK_AIR)|e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) 
			if (rightClick.contains(e.getPlayer())) {
				rightClick.remove(e.getPlayer());
				for (PlayerListener l:listeners)
					l.onDoubleRightClick(e.getPlayer());
			} else {
				rightClick.add(e.getPlayer());
				for (PlayerListener l:listeners)
					l.onRightClick(e.getPlayer());
				new BukkitRunnable() {
					@Override
					public void run() {
						rightClick.remove(e.getPlayer());
					}
				}.runTaskLater(LiberCore.get(), 7);
			}
	}
}
