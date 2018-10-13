package net.libercraft.libercore.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class StoredInventory {
	
	public ItemStack[] storedInv;
	
	public StoredInventory(Player p) {
		storeInv(p.getInventory());
	}
	
	public StoredInventory(Inventory inv) {
		storeInv(inv);
	}
	
	public StoredInventory(ItemStack[] storedInv) {
		this.storedInv = storedInv;
	}
	
	protected void storeInv(Inventory inv) {
		ItemStack[] invContent = inv.getContents();
		storedInv = new ItemStack[invContent.length];
		for (int i = 0; i < invContent.length; i++)
			if (invContent[i] != null)
				storedInv[i] = invContent[i].clone();
	}
	
	public String toString() {
		String serialization = "";

		// Store regular inventory
		serialization += storedInv.length + ";";
        for (int i = 0; i < storedInv.length; i++)
    		if (storedInv[i] != null)
    			serialization += i + "#" + Serializer.serializeItemStack(storedInv[i]) + ";";
        return serialization;
	}
	
	public int practicalLength() {
		int l = 0;
		for (int i=0; i<storedInv.length; i++)
			if (storedInv[i] != null)
				l++;
		return l;
	}
	
	public ItemStack random() {
		int r = (int) Math.floor(Math.random() * practicalLength());
		
		int i = 0;
		ItemStack item = null;
		while (item == null) {
			item = storedInv[i];
			if (item != null && r > 0) {
				r--;
				item = null;
			}
			i++;
		}
		return item;
	}
	
	public static StoredInventory fromString(String inv) {
		String[] serializedInventoryBlocks = inv.split(";");
		
		ItemStack[] stack = new ItemStack[Integer.parseInt(serializedInventoryBlocks[0])];
	       
        for (int i = 1; i < serializedInventoryBlocks.length; i++)
        {
            String[] serializedItemStack = serializedInventoryBlocks[i].split("#");
    		int stackPosition = Integer.valueOf(serializedItemStack[0]);
            
            if (stackPosition >= stack.length)
            	continue;

            ItemStack is = Serializer.unserializeItemStack(serializedItemStack[1].split(":"));
            stack[stackPosition] = is;
        }
        
        return new StoredInventory(stack);
	}
}
