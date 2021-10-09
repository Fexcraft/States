package net.fexcraft.mod.states.data;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.states.util.Settings;

public class Sellable implements Saveable {
	
	private Layer layer;
	public long price;
	
	public Sellable(Layer root){
		layer = root;
	}

	@Override
	public void save(JsonMap map){
		map.add("price", price);
	}

	@Override
	public void load(JsonMap map){
		price = map.getLong("price", layer.is(Layers.CHUNK) ? Settings.DEFAULT_CHUNK_PRICE : 0);
	}

}
