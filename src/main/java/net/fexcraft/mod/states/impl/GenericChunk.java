package net.fexcraft.mod.states.impl;

import java.io.File;
import java.util.UUID;

import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.util.Config;
import net.fexcraft.mod.states.util.StateUtil;

public class GenericChunk implements Chunk {

	private District district;
	private long price;
	private int x, z;
	private long created, changed;
	private UUID creator;
	
	public GenericChunk(int x, int z){
		this.x = x; this.z = z;
		JsonObject obj = JsonUtil.get(getChunkFile());
		price = JsonUtil.getIfExists(obj, "price", Config.DEFAULT_CHUNK_PRICE).longValue();
		district = StateUtil.getDistrict(JsonUtil.getIfExists(obj, "district", -1).intValue());
		created = JsonUtil.getIfExists(obj, "created", Time.getDate()).longValue();
		creator = UUID.fromString(obj.has("creator") ? obj.get("creator").getAsString() : States.CONSOLE_UUID);
		changed = JsonUtil.getIfExists(obj, "changed", Time.getDate()).longValue();
	}

	@Override
	public int xCoord(){
		return x;
	}

	@Override
	public int zCoord(){
		return z;
	}

	@Override
	public long getPrice(){
		return price;
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
		obj.addProperty("created", created);
		obj.addProperty("creator", creator.toString());
		obj.addProperty("changed", changed);
		return obj;
	}

	@Override
	public District getDistrict(){
		return district;
	}

	@Override
	public long getCreated(){
		return created;
	}

	@Override
	public UUID getCreator(){
		return creator;
	}

	@Override
	public long getChanged(){
		return changed;
	}

}
