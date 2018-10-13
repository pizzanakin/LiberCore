package net.libercraft.libercore;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.libercraft.libercore.interfaces.Loadable;
import net.libercraft.libercore.interfaces.Module;
import net.libercraft.libercore.interfaces.Updatable;
import net.libercraft.libercore.managers.MessageManager;
import net.libercraft.libercore.managers.PlayerManager;

public class LiberCore extends JavaPlugin {
	public static LiberCore instance;
	
	public static List<Module> modules;
	public static List<Updatable> preUpdatable;
	public static List<Updatable> updatable;
	public static List<Loadable> loadable;
	
	// LiberCore classes
	public CoreDatabase cd;
	public MessageManager tm;
	public PlayerManager pm;
	
	@Override
	public void onEnable() {
		instance = this;
		
		modules = new ArrayList<Module>();
		loadable = new ArrayList<Loadable>();
		updatable = new ArrayList<Updatable>();
		preUpdatable = new ArrayList<Updatable>();
		
		cd = new CoreDatabase();
		tm = new MessageManager();
		pm = new PlayerManager();

		// Check plugins to find modules
		for (int i = 0; i < this.getServer().getPluginManager().getPlugins().length; i++) {
			Plugin p = this.getServer().getPluginManager().getPlugins()[i];
			if (p instanceof Module)
				modules.add((Module)p);
		}
		
		getServer().getPluginManager().registerEvents(tm, this);
		getServer().getPluginManager().registerEvents(pm, this);

		new BukkitRunnable() {
			@Override
			public void run() {
				getLogger().info("Activating modules...");
				for (Module m:modules) {
					m.activate();
					getLogger().info(m.getName() + " activated!");
				}
			}
		}.runTaskLater(get(), 0);

		new BukkitRunnable() {
			@Override
			public void run() {
				for (Updatable u:updatable) 
						u.update();
				for (Updatable u:preUpdatable)
					updatable.add(u);
				preUpdatable.clear();
			}
		}.runTaskTimer(get(), 0, 1);
	}
	
	@Override
	public void onDisable() {
		for (Loadable li:loadable) {
			li.close();
		}
	}
	
	public static LiberCore get() {
		return instance;
	}
	
	public static MessageManager getTM() {
		return instance.tm;
	}
	
	public static PlayerManager getPM() {
		return instance.pm;
	}
	
	public static boolean isActive(String module) {
		for (Module m:modules) 
			if (m.name().equals(module))
				return m.isActivated();
		return false;
	}
}
