package net.libercraft.libercore.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ItemManager {
	
	public static String getDisplayName(Material mat) {
		String name = "";
		for (String word:mat.toString().split("_"))
			name += word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase() + " ";
		return name.substring(0, name.length() -1);
	}

	public static boolean isWeapon(ItemStack i) {
		Material t = i.getType();

		if (isSword(i))
			return true;
		if (t.toString().endsWith("AXE"))
			return true;
		if (t == Material.BOW)
			return true;
		if (t == Material.TRIDENT)
			return true;
		return false;
	}

	public static boolean isSword(ItemStack i) {
		Material t = i.getType();
		
		if (t.toString().endsWith("SWORD"))
			return true;
		return false;
	}
	
	public static boolean isArmor(ItemStack i) {
		Material t = i.getType();

		if (t.toString().endsWith("HELMET"))
			return true;
		if (t.toString().endsWith("CHESTPLATE"))
			return true;
		if (t.toString().endsWith("LEGGINGS"))
			return true;
		if (t.toString().endsWith("BOOTS"))
			return true;
		return false;
	}
	
	public static List<String> createLore(String... args) {
		List<String> list = new ArrayList<String>();
		for (String arg:args) 
			list.add(arg);
		return list;
	}
	
	public static ItemStack addNBTTag(ItemStack item, String tag, Object value) {
		net.minecraft.server.v1_13_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		net.minecraft.server.v1_13_R2.NBTTagCompound compound = getCompound(nmsItem);
		if (value instanceof String) 
			compound.set(tag, new net.minecraft.server.v1_13_R2.NBTTagString((String) value));
		// TODO check for more types and add them
		nmsItem.setTag(compound);
		return CraftItemStack.asBukkitCopy(nmsItem);
	}
	
	public static String getNBTString(ItemStack item, String tag) {
		net.minecraft.server.v1_13_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		net.minecraft.server.v1_13_R2.NBTTagCompound compound = getCompound(nmsItem);
		return compound.getString(tag);
	}
	
	private static net.minecraft.server.v1_13_R2.NBTTagCompound getCompound(net.minecraft.server.v1_13_R2.ItemStack item) {
		return (item.hasTag()) ? item.getTag() : new net.minecraft.server.v1_13_R2.NBTTagCompound();
	}
	
	public static net.minecraft.server.v1_13_R2.NBTTagList addAttributeCompound(net.minecraft.server.v1_13_R2.NBTTagList list, String attribute, Object value, String slot) {
		net.minecraft.server.v1_13_R2.NBTTagCompound compound = new net.minecraft.server.v1_13_R2.NBTTagCompound();
		compound.set("AttributeName", new net.minecraft.server.v1_13_R2.NBTTagString(attribute));
		compound.set("Name", new net.minecraft.server.v1_13_R2.NBTTagString(attribute));
		if (value instanceof Integer)
			compound.set("Amount", new net.minecraft.server.v1_13_R2.NBTTagInt(((Integer)value).intValue()));
		compound.set("Slot", new net.minecraft.server.v1_13_R2.NBTTagString(slot));
		compound.set("Operation", new net.minecraft.server.v1_13_R2.NBTTagInt(0));
		compound.set("UUIDLeast", new net.minecraft.server.v1_13_R2.NBTTagInt(894654));
		compound.set("UUIDMost", new net.minecraft.server.v1_13_R2.NBTTagInt(2872));
		list.add(compound);
		return list;
	}
  
	public static String convertItemStackToJsonRegular(org.bukkit.inventory.ItemStack item) {
		net.minecraft.server.v1_13_R2.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item);
		net.minecraft.server.v1_13_R2.NBTTagCompound compound = new net.minecraft.server.v1_13_R2.NBTTagCompound();
		compound = nmsItemStack.save(compound);
		return compound.toString();
	}
}
