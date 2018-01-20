package net.fexcraft.mod.states.impl;

import java.io.File;

import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.util.Config;
import net.fexcraft.mod.states.util.StateUtil;

public class GenericChunk implements Chunk {

	private District district;
	private long price;
	private int x, z;
	
	public GenericChunk(int x, int z){
		this.x = x; this.z = z;
		JsonObject obj = JsonUtil.get(getChunkFile());
		price = JsonUtil.getIfExists(obj, "price", Config.DEFAULT_CHUNK_PRICE).longValue();
		district = StateUtil.getDistrict(JsonUtil.getIfExists(obj, "district", -1).intValue());
	}

	@Override
	public int xCoord(){
		return 0;
	}

	@Override
	public int zCoord(){
		return 0;
	}

	@Override
	public long getPrice(){
		return 0;
	}

	@Override
	public void setPrice(long new_price){
		price = new_price;
	}

	@Override
	public void save(){
		JsonObject obj = toJsonObject();
		obj.addProperty("last_save", Time.getDate());
		File file = getChunkFile();
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, obj);
	}

	@Override
	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("x", x);
		obj.addProperty("z", z);
		obj.addProperty("price", price);
		return obj;
	}

	@Override
	public District getDistrict(){
		return district;
	}

}
