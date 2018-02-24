package net.fexcraft.mod.states.impl;

import java.io.File;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.lang.ArrayList;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.ChunkType;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.util.Config;
import net.fexcraft.mod.states.util.ImageCache;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class GenericChunk implements Chunk {

	private District district;
	private long price;
	private int x, z;
	private Integer lx, lz;
	private long created, changed;
	private UUID creator;
	private ArrayList<ResourceLocation> linked;
	private ChunkType type;
	private String owner;
	
	public GenericChunk(int x, int z, boolean create){
		this.x = x; this.z = z;
		JsonObject obj = JsonUtil.get(getChunkFile());
		price = JsonUtil.getIfExists(obj, "price", Config.DEFAULT_CHUNK_PRICE).longValue();
		district = StateUtil.getDistrict(JsonUtil.getIfExists(obj, "district", -1).intValue());
		created = JsonUtil.getIfExists(obj, "created", Time.getDate()).longValue();
		creator = UUID.fromString(obj.has("creator") ? obj.get("creator").getAsString() : States.CONSOLE_UUID);
		changed = JsonUtil.getIfExists(obj, "changed", Time.getDate()).longValue();
		linked = JsonUtil.jsonArrayToResourceLocationArray(JsonUtil.getIfExists(obj, "linked", new JsonArray()).getAsJsonArray());
		type = ChunkType.valueOf(JsonUtil.getIfExists(obj, "type", ChunkType.NORMAL.name()).toUpperCase());
		owner = JsonUtil.getIfExists(obj, "owner", "null");
		//
		String lk = JsonUtil.getIfExists(obj, "link", "");
		if(lk.length() > 0){
			String[] link = lk.split(":");
			lx = Integer.parseInt(link[0]);
			lz = Integer.parseInt(link[1]);
		}
		//
		if(!getChunkFile().exists() && create){
			save();
			World world = Static.getServer().getWorld(0);
			ImageCache.update(world, world.getChunkFromChunkCoords(x, z), "create", "all");
		}
	}

	public GenericChunk(int x, int z){
		this(x, z, true);
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
		obj.addProperty("district", district.getId());
		obj.addProperty("created", created);
		obj.addProperty("creator", creator.toString());
		obj.addProperty("changed", changed);
		obj.addProperty("type", type.toString());
		obj.addProperty("owner", owner == null ? "null" : owner);
		if((Integer)lx != null && (Integer)lz != null){
			obj.addProperty("link", lx + ":" + lz);
		}
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
	public UUID getClaimer(){
		return creator;
	}

	@Override
	public void setClaimer(UUID id){
		creator = UUID.fromString(id.toString());
	}

	@Override
	public long getChanged(){
		return changed;
	}

	@Override
	public void setChanged(long new_change){
		changed = new_change;
	}

	@Override
	public List<ResourceLocation> getLinkedChunks(){
		return linked;
	}

	@Override
	public void setDistrict(District dis){
		district = dis;
	}

	@Override
	public ChunkType getType(){
		return type;
	}

	@Override
	public void setType(ChunkType type){
		this.type = type;
	}

	@Override
	public String getOwner(){
		return owner;
	}

	@Override
	public void setOwner(String str){
		owner = str;
	}

	@Override
	public int[] getLink(){
		return lx == null || lz == null ? null : new int[]{lx, lz};
	}

	@Override
	public void setLink(Integer x, Integer z){
		if(x == null || z == null){
			lx = null; lz = null;
		}
		else{
			lx = x; lz = z;
		}
	}

}
