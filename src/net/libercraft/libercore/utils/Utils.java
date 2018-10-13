package net.libercraft.libercore.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Utils {
	
	static public void ExportResource(JavaPlugin plugin, String resourceName) {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        String outputFolder = plugin.getDataFolder().getAbsolutePath();
        try {
            stream = plugin.getClass().getResourceAsStream(resourceName);//note that each / is a directory down in the "jar tree" been the jar the root of the tree
            if(stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            resStreamOut = new FileOutputStream(outputFolder + resourceName);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
				stream.close();
				resStreamOut.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        System.out.println("Created new file: " + outputFolder + resourceName);
    }
  
	public static BarColor getBarColour(ChatColor color) {
		switch (color) {
			case GRAY: 
				return BarColor.WHITE;
			case AQUA: 
				return BarColor.BLUE;
			case DARK_RED: 
				return BarColor.RED;
			case BLACK: 
				return BarColor.WHITE;
			case DARK_PURPLE: 
				return BarColor.PURPLE;
			case BLUE: 
				return BarColor.BLUE;
			case DARK_BLUE: 
				return BarColor.BLUE;
			case DARK_AQUA: 
				return BarColor.BLUE;
			case DARK_GRAY: 
				return BarColor.WHITE;
			case DARK_GREEN: 
				return BarColor.GREEN;
			case GOLD: 
				return BarColor.YELLOW;
			case GREEN: 
				return BarColor.GREEN;
			case LIGHT_PURPLE: 
				return BarColor.PURPLE;
			case RED:
				return BarColor.RED;
			case WHITE:
				return BarColor.WHITE;
			case YELLOW:
				return BarColor.YELLOW;
			default:
				break;
		}
		return BarColor.WHITE;
	}
}
