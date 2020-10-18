package net.fexcraft.mod.states.data.root;

import com.google.gson.JsonElement;

public interface ExternalData {
	
	public JsonElement save();
	
	public void load(JsonElement elm);

}
