package net.libercraft.libercore.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class StoredPlayerInventory extends StoredInventory {
	
	public ItemStack[] storedArmor;
	
	public StoredPlayerInventory(Player p) {
		super(p);
		storePlayerInv(p.getInventory());
	}
	
	public StoredPlayerInventory(PlayerInventory inv) {
		super(inv);
		storePlayerInv(inv);
	}
	
	public StoredPlayerInventory(ItemStack[] storedInv, ItemStack[] storedArmor) {
		super(storedInv);
		this.storedArmor = storedArmor;
	}
	
	private void storePlayerInv(PlayerInventory inv) {
		ItemStack[] armor = inv.getArmorContents();
		storedArmor = new ItemStack[4];
		for (int i = 0; i < 4; i++)
			if (armor[i] != null)
				storedArmor[i] = armor[i].clone();
	}
	
	@Override
	public String toString() {
		String serialization = "";
        
        // Store armor
        for (int i = 0; i < storedArmor.length; i++) 
        	if (storedArmor[i] != null)
        		serialization += i + "#" + Serializer.serializeItemStack(storedArmor[i]) + ";";
        	else
        		serialization += i + "#" + ";";
        
        return serialization + super.toString();
	}
	
	public static StoredPlayerInventory fromString(String inv) {
		String[] serializedInventoryBlocks = inv.split(";");
		
		// Process armor
        ItemStack[] storedArmor = new ItemStack[4];
		for (int i = 0; i < 4; i++)
			if (!serializedInventoryBlocks[i].equals(i+"#"))
				storedArmor[i] = Serializer.unserializeItemStack(serializedInventoryBlocks[i].split("#"));
		
		// Process inventory
        ItemStack[] storedInv = new ItemStack[Integer.parseInt(serializedInventoryBlocks[4])];
       
        for (int i = 5; i < serializedInventoryBlocks.length; i++)
        {
            String[] serializedItemStack = serializedInventoryBlocks[i].split("#");
    		int stackPosition = Integer.valueOf(serializedItemStack[0]);
            
            if (stackPosition >= storedInv.length)
            	continue;

            ItemStack is = Serializer.unserializeItemStack(serializedItemStack[1].split(":"));
            storedInv[stackPosition] = is;
        }
       
        return new StoredPlayerInventory(storedInv, storedArmor);
	}
}
