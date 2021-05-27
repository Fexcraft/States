package net.fexcraft.mod.states.data.sub;

import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.states.data.root.Layer;
import net.fexcraft.mod.states.data.root.Loadable;
import net.fexcraft.mod.states.util.StConfig;

public class Buyable implements Loadable {

	public static final String NOTFORSALE = "not_for_sale";
	private Layer layer;
	private long price;
	
	public Buyable(Layer layer){
		this.layer = layer;
	}

	@Override
	public void load(JsonObject obj){
		price = obj.has("price") ? obj.get("price").getAsLong() : 0;
	}

	@Override
	public void save(JsonObject obj){
		if(price > 0) obj.addProperty("price", price);
	}
	
	public long get(){
		switch(layer.getLayerType()){
			case CHUNK:{
				if(layer.getParent().getId() == -1) return StConfig.DEFAULT_CHUNK_PRICE;
				break;
			}
			default: break;
		}
		return price;
	}
	
	public void set(long newprice){
		price = newprice;
	}

	public void reset(){
		set(0);
	}
	
	public final String asString(){
		return price == 0 ? NOTFORSALE : Config.getWorthAsString(price, false);
	}
	
	public final String asWorth(){
		return Config.getWorthAsString(price, false);
	}

	public boolean forSale(){
		return price > 0;
	}

}
