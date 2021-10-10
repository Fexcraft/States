package net.fexcraft.mod.states.data;

import net.fexcraft.app.json.JsonMap;

public class Taxable implements Saveable {
	
	public long last_interval, last_tax, custom_tax;
	public Layer layer;
	
	public Taxable(Layer root){
		layer = root;
	}

	@Override
	public void save(JsonMap map){
		if(last_interval > 0) map.add("tax_interval", last_interval);
		if(last_tax > 0) map.add("tax_last", last_tax);
		if(custom_tax > 0) map.add("tax_custom", custom_tax);
	}

	@Override
	public void load(JsonMap map){
		last_interval = map.getLong("last_interval", 0);
		last_tax = map.getLong("last_tax", 0);
		custom_tax = map.getLong("custom_tax", 0);
	}

}
