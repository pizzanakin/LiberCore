package net.libercraft.libercore.utils;

import org.bukkit.Bukkit;

import net.md_5.bungee.api.ChatColor;

public class Debug {

	public static void log(Object o) {
		String ob = (o == null) ? "null" : o.toString();
		System.out.println("[LiberDebug] " + ob);
		Bukkit.getPlayer("Pizzanakin").sendMessage(ChatColor.AQUA + "[LiberDebug] " + ChatColor.RESET + ob);
	}
}
