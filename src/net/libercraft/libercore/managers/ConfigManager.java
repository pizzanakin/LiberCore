package net.libercraft.libercore.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

	public static void addProperty(JavaPlugin plugin, String property, Object value) {
		FileConfiguration config = plugin.getConfig();
		config.addDefault(property, value);
		config.options().copyDefaults(true);
		plugin.saveConfig();
	}
	
	public static Object getValue(JavaPlugin plugin, String property) {
		FileConfiguration config = plugin.getConfig();
		return config.get(property);
	}
}
