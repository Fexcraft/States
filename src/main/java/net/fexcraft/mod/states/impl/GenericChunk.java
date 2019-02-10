package net.fexcraft.mod.states.impl;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.lang.ArrayList;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.ChunkPos;
import net.fexcraft.mod.states.api.ChunkType;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.impl.capabilities.SignTileEntityCapabilityUtil;
import net.fexcraft.mod.states.util.Config;
import net.fexcraft.mod.states.util.ImageCache;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.TaxSystem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class GenericChunk implements Chunk {

	private District district;
	private long price;
	private int x, z;
	private ChunkPos link;
	private long created, changed, edited;
	private UUID creator;
	private ArrayList<ResourceLocation> linked;
	private ChunkType type;
	private String owner;
	private List<UUID> wl_players = new ArrayList<>();
	private List<Integer> wl_companies = new ArrayList<>();
	private ChunkPos pos;
	private long lasttaxcheck, ctax;
	
	public GenericChunk(World world, ChunkPos pos, boolean create){
		this.x = pos.x; this.z = pos.z; this.pos = pos;
		JsonElement jsn = StateUtil.read(getChunkFile());
		JsonObject obj = jsn == null ? new JsonObject() : jsn.getAsJsonObject();
		parseJson(obj);
		//
		if(!getChunkFile().exists() && create){
			save();
			//ImageCache.update(world, world.getChunkFromChunkCoords(x, z));
			//World world = Static.getServer().getWorld(0);
			ImageCache.update(world, world.getChunkProvider().getLoadedChunk(x, z));
		}
		if(Time.getDate() - JsonUtil.getIfExists(obj, "last_save", 0).longValue() > Time.DAY_MS){
			//ImageCache.update(world, world.getChunkFromChunkCoords(x, z));
			//World world = Static.getServer().getWorld(0);
			ImageCache.update(world, world.getChunkProvider().getLoadedChunk(x, z));
		}
		if(district != null && this.district.getId() == -2 && this.getChanged() + Time.DAY_MS < Time.getDate()){
			StateLogger.log(StateLogger.LoggerType.CHUNK, StateLogger.district(-2) + " time of " + StateLogger.chunk(this) + " expired! Setting back to " + StateLogger.district(-1) + "!");
			this.setDistrict(StateUtil.getDistrict(-1));
			save();
		}
		TaxSystem.processChunkTax(TaxSystem.getProbableSchedule(), this);
	}

	public GenericChunk(World world, ChunkPos pos){
		this(world, pos, true);
	}
	
	protected void parseJson(JsonObject obj){
		price = JsonUtil.getIfExists(obj, "price", Config.DEFAULT_CHUNK_PRICE).longValue();
		district = StateUtil.getDistrict(JsonUtil.getIfExists(obj, "district", -1).intValue());
		created = JsonUtil.getIfExists(obj, "created", Time.getDate()).longValue();
		creator = UUID.fromString(obj.has("creator") ? obj.get("creator").getAsString() : States.CONSOLE_UUID);
		changed = JsonUtil.getIfExists(obj, "changed", Time.getDate()).longValue();
		linked = JsonUtil.jsonArrayToResourceLocationArray(JsonUtil.getIfExists(obj, "linked", new JsonArray()).getAsJsonArray());
		type = ChunkType.valueOf(JsonUtil.getIfExists(obj, "type", ChunkType.NORMAL.name()).toUpperCase());
		owner = JsonUtil.getIfExists(obj, "owner", "null");
		lasttaxcheck = JsonUtil.getIfExists(obj, "last_tax_collection", 0).longValue();
		ctax = JsonUtil.getIfExists(obj, "custom_tax", 0).longValue();
		//
		try{
			String str = JsonUtil.getIfExists(obj, "link", "");
			if(str.length() > 0){ link = new ChunkPos(str.split(":")); }
		}
		catch(Exception e){
			e.printStackTrace();
		}
		//
		if(obj.has("whitelist")){
			JsonArray array = obj.get("whitelist").getAsJsonArray();
			for(JsonElement elm : array){
				try{
					UUID uuid = UUID.fromString(elm.getAsString());
					wl_players.add(uuid);
				}
				catch(Exception e){
					try{
						int i = Integer.parseInt(elm.getAsString());
						wl_companies.add(i);
					}
					catch(Exception ex){
						e.printStackTrace();
						ex.printStackTrace();
					}
				}
			}
		}
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
		return this.district.getId() == -1 ? Config.DEFAULT_CHUNK_PRICE : price;
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
		obj.addProperty("district", district == null ? -1 : district.getId());
		obj.addProperty("created", created);
		obj.addProperty("creator", creator.toString());
		obj.addProperty("changed", changed);
		obj.addProperty("type", type.toString());
		obj.addProperty("owner", owner == null ? "null" : owner);
		if(link != null){
			obj.addProperty("link", link.x + ":" + link.z);
		}
		if(!linked.isEmpty()){
			JsonArray array = new JsonArray();
			linked.forEach(rs -> array.add(rs.toString()));
			obj.add("linked", array);
		}
		if(wl_players.size() > 0 || wl_companies.size() > 0){
			JsonArray array = new JsonArray();
			wl_players.forEach(entry -> array.add(entry.toString()));
			wl_companies.forEach(entry -> array.add(entry));
			obj.add("whitelist", array);
		}
		obj.addProperty("last_tax_collection", lastTaxCollection());
		if(ctax > 0){ obj.addProperty("custom_tax", ctax); }
		obj.addProperty("edited", edited);
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
		SignTileEntityCapabilityUtil.processChunkChange(StateUtil.getChunk(this), "chunk");
	}

	@Override
	public List<ResourceLocation> getLinkedChunks(){
		return linked;
	}

	@Override
	public void setDistrict(District dis){
		if(district.getId() != -1){
			district.setClaimedChunks(district.getClaimedChunks() - 1);
		}
		district = dis;
		if(district.getId() != -1){
			district.setClaimedChunks(district.getClaimedChunks() + 1);
		}
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
		switch(type){
			case COMPANY: return owner;//TODO company
			case DISTRICT: return "District";
			case MUNICIPAL: return "Municipal";
			case NORMAL: return "(" + district.getMunicipality().getType().getTitle() + ") " + district.getMunicipality().getName();
			case PRIVATE: return owner;
			case PUBLIC: return "Public";
			case STATEOWNED: return "State Owned";
			default: return owner;
		}
	}

	@Override
	public void setOwner(String str){
		owner = str == null ? "null" : str;
	}

	@Override
	public ChunkPos getLink(){
		return link;
	}

	@Override
	public void setLink(ChunkPos pos){
		link = pos;
	}

	@Override
	public List<UUID> getPlayerWhitelist(){
		return wl_players;
	}

	@Override
	public List<Integer> getCompanyWhitelist(){
		return wl_companies;
	}
	
	@Override
	public String toString(){
		return x + "_" + z;
	}

	@Override
	public boolean isForceLoaded(){
		Collection<ChunkPos> pos = this.district.getMunicipality().getForceLoadedChunks();
		return pos == null ? false : pos.contains(getChunkPos());
	}

	@Override
	public ChunkPos getChunkPos(){
		return pos;
	}

	@Override
	public long lastTaxCollection(){
		return lasttaxcheck;
	}

	@Override
	public void onTaxCollected(long time){
		lasttaxcheck = time;
		this.save(); //TODO more checks?
	}

	@Override
	public long getCustomTax(){
		return ctax;
	}

	@Override
	public void setCustomTax(long newtax){
		this.ctax = newtax;
	}

	@Override
	public long getEdited(){
		return edited;
	}

	@Override
	public void setEdited(long new_change){
		edited = new_change;
	}

}
