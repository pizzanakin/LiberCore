package net.libercraft.libercore.interfaces;

import net.libercraft.libercore.LiberCore;

public interface Loadable {
	public abstract void load();
	public abstract void close();
	
	public default void registerLoadable() {
		LiberCore.loadable.add(this);
		load();
	}
}
