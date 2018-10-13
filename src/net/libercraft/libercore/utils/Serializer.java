package net.libercraft.libercore.utils;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class Serializer {
	
	public static String serializeItemStack(ItemStack is) {
        String serializedItemStack = "";

        // Set type
        serializedItemStack += "t@" + String.valueOf(is.getType().toString());
       
        // Set durability
        if (is.getDurability() != 0) 
            serializedItemStack += ":d@" + String.valueOf(is.getDurability());
       
        // Set amount
        if (is.getAmount() != 1)
            serializedItemStack += ":a@" + String.valueOf(is.getAmount());
       
        Map<Enchantment,Integer> isEnch = is.getEnchantments();
        if (isEnch.size() > 0)
            for (Entry<Enchantment,Integer> ench : isEnch.entrySet())
            	serializedItemStack += ":e@" + ench.getKey().getKey().toString() + "@" + ench.getValue();
        
        return serializedItemStack;
	}
	
	public static ItemStack unserializeItemStack(String[] serializedItemStack) {
       
        ItemStack is = null;
        Boolean createdItemStack = false;
       
        for (String itemInfo : serializedItemStack)
        {
            String[] itemAttribute = itemInfo.split("@");
            if (itemAttribute[0].equals("t")) {
                is = new ItemStack(Material.getMaterial(itemAttribute[1]));
                createdItemStack = true;
            }
            else if (itemAttribute[0].equals("d") && createdItemStack)
            	is.setDurability(Short.valueOf(itemAttribute[1]));
            else if (itemAttribute[0].equals("a") && createdItemStack)
	            is.setAmount(Integer.valueOf(itemAttribute[1]));
            else if (itemAttribute[0].equals("e") && createdItemStack)
            	is.addEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(itemAttribute[1])), Integer.valueOf(itemAttribute[2]));
        }
        return is;
	}
}
