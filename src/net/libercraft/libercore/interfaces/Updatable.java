package net.libercraft.libercore.interfaces;

import net.libercraft.libercore.LiberCore;

public interface Updatable {
	public abstract void update();
	
	public default void registerUpdatable() {
		LiberCore.preUpdatable.add(this);
	}
}
