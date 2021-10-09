package net.fexcraft.mod.states.db;

import net.fexcraft.mod.states.data.Saveable;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public interface Database {
	
	public void save(Saveable type);
	
	/** For non-internal database (handlers) it is expected to return a Map&lt;String, Object&gt; */
	public <O> O load(String table, String id);
	
	public boolean exists(String table, String id);
	
	/** If this database (handler) is internal (integrated in states mods). */
	public boolean internal();

}
