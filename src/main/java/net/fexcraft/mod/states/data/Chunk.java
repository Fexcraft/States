package net.fexcraft.mod.states.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.root.BuyableType;
import net.fexcraft.mod.states.data.root.Taxable;
import net.fexcraft.mod.states.util.ImageCache;
import net.fexcraft.mod.states.util.StConfig;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.TaxSystem;
import net.minecraft.world.World;

public class Chunk implements BuyableType, Taxable/*, RuleHolder*/ {
	
	private District district;
	private long price, created, changed, edited, lasttaxcheck, ctax;
	private int x, z;
	private ChunkPos link, pos;
	private UUID creator;
	private ArrayList<int[]> linked = new ArrayList<>();
	private ChunkType type;
	private String owner;
	private List<UUID> wl_players = new ArrayList<>();
	private List<Integer> wl_companies = new ArrayList<>();
	private boolean interact;
	//private RuleMap rules = new RuleMap();
	
	public Chunk(World world, ChunkPos pos, boolean create){
		this.x = pos.x; this.z = pos.z; this.pos = pos;
		JsonElement jsn = StateUtil.read(getChunkFile());
		JsonObject obj = jsn == null ? new JsonObject() : jsn.getAsJsonObject();
		parseJson(obj);
		//
		if(!getChunkFile().exists() && create){
			save();
			//ImageCache.update(world, world.getChunkFromChunkCoords(x, z));
			if(world == null) world = Static.getServer().getWorld(0);
			if(world != null) ImageCache.update(world, world.getChunkProvider().getLoadedChunk(x, z));
		}
		if(Time.getDate() - JsonUtil.getIfExists(obj, "last_save", 0).longValue() > Time.DAY_MS){
			//ImageCache.update(world, world.getChunkFromChunkCoords(x, z));
			if(world == null) world = Static.getServer().getWorld(0);
			if(world != null) ImageCache.update(world, world.getChunkProvider().getLoadedChunk(x, z));
		}
		if(district != null && this.district.getId() == -2 && changed + Time.DAY_MS < Time.getDate()){
			Print.log(StateLogger.district(-2) + " time of " + StateLogger.chunk(this) + " expired! Setting back to " + StateLogger.district(-1) + "!");
			this.setDistrict(StateUtil.getDistrict(-1));
			save();
		}
		TaxSystem.processChunkTax(TaxSystem.getProbableSchedule(), this);
	}

	public Chunk(World world, ChunkPos pos){
		this(world, pos, true);
	}
	
	protected void parseJson(JsonObject obj){
		price = JsonUtil.getIfExists(obj, "price", StConfig.DEFAULT_CHUNK_PRICE).longValue();
		district = StateUtil.getDistrict(JsonUtil.getIfExists(obj, "district", -1).intValue());
		created = JsonUtil.getIfExists(obj, "created", Time.getDate()).longValue();
		creator = UUID.fromString(obj.has("creator") ? obj.get("creator").getAsString() : States.CONSOLE_UUID);
		changed = JsonUtil.getIfExists(obj, "changed", Time.getDate()).longValue();
		if(obj.has("linked")){
			JsonArray array = obj.get("linked").getAsJsonArray();
			array.forEach(elm -> {
				try{
					String[] split = elm.getAsString().split(":");
					linked.add(new int[]{ Integer.parseInt(split[0]), Integer.parseInt(split[1]) });
				}
				catch(Exception e){
					e.printStackTrace();
				}
			});
		}
		else linked.clear();
		type = ChunkType.valueOf(JsonUtil.getIfExists(obj, "type", ChunkType.NORMAL.name()).toUpperCase());
		owner = JsonUtil.getIfExists(obj, "owner", "null");
		lasttaxcheck = JsonUtil.getIfExists(obj, "last_tax_collection", 0).longValue();
		ctax = JsonUtil.getIfExists(obj, "custom_tax", 0).longValue();
		interact = JsonUtil.getIfExists(obj, "interact", false);
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

	public void save(){
		JsonObject obj = toJsonObject();
		obj.addProperty("last_save", Time.getDate());
		File file = getChunkFile();
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, obj);
	}

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
			linked.forEach(rs -> array.add(rs[0] + ":" + rs[1]));
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
		if(interact) obj.addProperty("interact", interact);
		obj.addProperty("edited", edited);
		return obj;
	}
	
	public final File getChunkFile(){
		return new File(States.getSaveDirectory(), "chunks/" + getChunkRegion() + "/" + x + "_" + z + ".json");
	}

	public final String getChunkRegion(){
		return (int)Math.floor(x / 32.0) + "_" + (int)Math.floor(z / 32.0);
	}

	public int xCoord(){
		return x;
	}

	public int zCoord(){
		return z;
	}

	@Override
	public long getPrice(){
		return this.getDistrict().getId() == -1 ? StConfig.DEFAULT_CHUNK_PRICE : price;
	}

	@Override
	public void setPrice(long new_price){
		price = new_price;
	}

	public District getDistrict(){
		return district;
	}

	public long getCreated(){
		return created;
	}

	public UUID getClaimer(){
		return creator;
	}

	public void setClaimer(UUID id){
		creator = UUID.fromString(id.toString());
	}

	public long getChanged(){
		return changed;
	}

	public void setChanged(long new_change){
		changed = new_change;
	}

	public List<int[]> getLinkedChunks(){
		return linked;
	}

	public void setDistrict(District dis){
		if(district.getId() != -1){
			district.setClaimedChunks(district.getClaimedChunks() - 1);
		}
		district = dis;
		if(district.getId() != -1){
			district.setClaimedChunks(district.getClaimedChunks() + 1);
		}
	}

	public ChunkType getType(){
		return type;
	}

	public void setType(ChunkType type){
		this.type = type;
	}

	public String getOwner(){
		switch(type){
			case COMPANY: return owner;//TODO company
			case DISTRICT: return "District";
			case MUNICIPAL: return "Municipal";
			case NORMAL: return "(" + district.getMunicipality().getTitle() + ") " + district.getMunicipality().getName();
			case PRIVATE: return owner;
			case PUBLIC: return "(Mun.) Public";
			case STATEOWNED: return "State Owned";
			case STATEPUBLIC: return "(State) Public";
			default: return owner;
		}
	}

	public void setOwner(String str){
		owner = str == null ? "null" : str;
	}

	public ChunkPos getLink(){
		return link;
	}

	public void setLink(ChunkPos pos){
		link = pos;
	}

	public List<UUID> getPlayerWhitelist(){
		return wl_players;
	}

	public List<Integer> getCompanyWhitelist(){
		return wl_companies;
	}
	
	@Override
	public String toString(){
		return x + "_" + z;
	}

	public boolean isForceLoaded(){
		Collection<ChunkPos> pos = this.district.getMunicipality().getForceLoadedChunks();
		return pos == null ? false : pos.contains(getChunkPos());
	}

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

	public long getEdited(){
		return edited;
	}

	public void setEdited(long new_change){
		edited = new_change;
	}
	
	public Municipality getMunicipality(){
		return district.getMunicipality();
	}
	
	public State getState(){
		return district.getMunicipality().getState();
	}

	public boolean isRuleAuthorized(UUID uuid){
		switch(this.type){
			case COMPANY:{
				return false;//TODO
			}
			case DISTRICT:{
				return district.r_SET_CHUNKRULES.isAuthorized(district, uuid).isTrue();
			}
			case MUNICIPAL: case NORMAL: case PUBLIC:{
				return getMunicipality().r_SET_CHUNKRULES.isAuthorized(district, uuid).isTrue();
			}
			case PRIVATE:{
				return owner != null && owner.equals(uuid.toString());
			}
			case STATEOWNED:{
				return false;//TODO
			}
			case STATEPUBLIC:{
				return false;//TODO
			}
			default: return false;
		}
	}

	/*@Override
	public Map<String, Rule> getRules(){
		return rules;
	}*/
	
	public boolean interact(){
		return interact;
	}

	public boolean interact(boolean bool){
		return interact = bool;
	}

}
