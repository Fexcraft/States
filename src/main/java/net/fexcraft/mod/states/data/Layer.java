package net.fexcraft.mod.states.data;

public interface Layer {

	public Layers getLayer();
	
	public default boolean is(Layers lay){
		return lay == getLayer();
	}
	
}
