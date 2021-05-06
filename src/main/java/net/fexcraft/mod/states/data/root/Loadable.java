package net.fexcraft.mod.states.data.root;

import com.google.gson.JsonObject;

public interface Loadable {
	
	public void load(JsonObject obj);
	
	public void save(JsonObject obj);

}
