package net.libercraft.libercore.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import net.libercraft.libercore.CoreDatabase;
import net.libercraft.libercore.interfaces.Loadable;
import net.libercraft.libercore.interfaces.Module;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MessageManager implements Listener, Loadable {
	private static List<Ticket> tickets;
	
	public MessageManager() {
		registerLoadable();
	}
	
	@Override
	public void load() {
		tickets = CoreDatabase.loadTickets();
	}
	
	@Override
	public void close() {
		for (Ticket t:tickets) 
			CoreDatabase.saveTicket(t);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		for (Ticket t:tickets) 
			if (t.recipient.isOnline() && t.recipient.getPlayer().equals(e.getPlayer())) {
				MessageManager.sendMessage(t.plugin, e.getPlayer(), t.message);
				tickets.remove(t);
			}
	}
	
	public static String messageString(Module p, String message) {
		return p.colour()+p.tag()+" "+ChatColor.GRAY+message;
	}
	
	public static boolean broadcastMessage(Module p, String message) {
		Bukkit.broadcastMessage(p.colour()+p.tag()+" "+ChatColor.GRAY+message);
		return true;
	}
	
	public static boolean sendPermissionError(Player player) {
		player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
		return true;
	}
	
	public static boolean sendMessage(Module p, Player player, String message) {
		player.sendMessage(p.colour()+p.tag()+" "+ChatColor.GRAY+message);
		return true;
	}
	
	public static boolean sendPreparedMessage(Module p, Player player, PreparedMessage message) {
		sendMessage(p, player, message.getMessage());
		return true;
	}

	public static void sendItemTooltipMessage(Player player, String message, ItemStack item) {
		if (!message.contains("##"))
			return;
		List<String> components = new ArrayList<String>();
		for (String c:message.split("#"))
			if (c.startsWith("#")) {
				components.add("");
				components.add(c.substring(1, c.length()));
			}
			else
				components.add(c);
		
		String itemJson = ItemManager.convertItemStackToJsonRegular(item);
		BaseComponent[] hoverEventComponents = new BaseComponent[] {
				new TextComponent(itemJson)
		};
		HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);
		
		List<TextComponent> comps = new ArrayList<TextComponent>();
		for (String c:components) {
			String messagepart = c;
			if (c.equals("")) 
				messagepart = getDisplayName(item);
			TextComponent component = new TextComponent(messagepart);
			if (c.equals("")) {
				component.setHoverEvent(event);
				component.setItalic(true);
			}
			comps.add(component);
		}
		
		BaseComponent[] msg = new BaseComponent[comps.size()];
		for (int i = 0; i < comps.size(); i++) {
			msg[i] = comps.get(i);
		}
		player.spigot().sendMessage(msg);
	}
	
	public static String getDisplayName(ItemStack item) {
		String displayname = "";
		if (item.getItemMeta().getEnchants().size() > 0)
			displayname += "" + ChatColor.AQUA + "";
		String itemname = item.getItemMeta().getDisplayName();
		if (itemname == null) {
			for (String w:item.getType().toString().split("_"))
				itemname += w.substring(0, 1) + w.substring(1).toLowerCase() + " ";
			itemname = itemname.substring(0, itemname.length() - 1);
		}
		displayname += "[" + itemname + "]";
		return displayname;
	}
	
	public static void prepareTicket(Module plugin, OfflinePlayer op, String message) {
		if (op.isOnline())
			MessageManager.sendMessage(plugin, op.getPlayer(), message);
		else
			createTicket(plugin, op, message);
	}
	
	private static void createTicket(Module plugin, OfflinePlayer player, String message) {
		Ticket t = new Ticket(player, message, plugin);
		tickets.add(t);
	}
	
	public static class Ticket {
		public OfflinePlayer recipient;
		public String message;
		public Module plugin;
		
		public Ticket(OfflinePlayer player, String message, Module plugin) {
			this.recipient = player;
			this.message = message;
			this.plugin = plugin;
		}
	}
	
	public static interface PreparedMessage {
		public abstract String getMessage();
	}
}
