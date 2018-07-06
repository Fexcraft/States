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
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.DistrictType;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.impl.capabilities.SignTileEntityCapabilityUtil;
import net.fexcraft.mod.states.util.StateUtil;

public class GenericDistrict implements District {
	
	private int id, chunks;
	private DistrictType type;
	private long created, changed, price;
	private UUID creator, manager;
	private ArrayList<Integer> neighbors;
	private String name, color, icon;
	private Municipality municipality;
	private boolean cfs;
	
	public GenericDistrict(int id){
		this.id = id;
		JsonObject obj = StateUtil.getDistrictJson(id);
		type = DistrictType.valueOf(JsonUtil.getIfExists(obj, "type", DistrictType.WILDERNESS.name()));
		created = JsonUtil.getIfExists(obj, "created", Time.getDate()).longValue();
		creator = UUID.fromString(obj.has("creator") ? obj.get("creator").getAsString() : States.CONSOLE_UUID);
		changed = JsonUtil.getIfExists(obj, "changed", Time.getDate()).longValue();
		neighbors = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "neighbors", new JsonArray()).getAsJsonArray());
		name = JsonUtil.getIfExists(obj, "name", "Unnamed District");
		municipality = StateUtil.getMunicipality(JsonUtil.getIfExists(obj, "municipality", -1).intValue());
		manager = obj.has("manager") ? UUID.fromString(obj.get("manager").getAsString()) : null;
		color = obj.has("color") ? obj.get("color").getAsString() : "#ffffff";
		cfs = JsonUtil.getIfExists(obj, "can_foreigners_settle", false);
		price = JsonUtil.getIfExists(obj, "price", 0).longValue();
		icon = JsonUtil.getIfExists(obj, "icon", States.DEFAULT_ICON);
		chunks = JsonUtil.getIfExists(obj, "chunks", 0).intValue();
	}

	@Override
	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("type", type.name());
		obj.addProperty("created", created);
		obj.addProperty("creator", creator.toString());
		obj.addProperty("changed", changed);
		obj.addProperty("name", name);
		obj.addProperty("municipality", municipality == null ? -1 : municipality.getId());
		obj.add("neighbors", JsonUtil.getArrayFromIntegerList(neighbors));
		if(!(manager == null)){ obj.addProperty("manager", manager.toString()); }
		obj.addProperty("color", color);
		obj.addProperty("can_foreigners_settle", cfs);
		obj.addProperty("price", price);
		if(icon != null){ obj.addProperty("icon", icon); }
		obj.addProperty("chunks", chunks);
		return obj;
	}

	@Override
	public void save(){
		JsonObject obj = toJsonObject();
		obj.addProperty("last_save", Time.getDate());
		File file = getDistrictFile();
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, obj);
	}

	@Override
	public int getId(){
		return id;
	}

	@Override
	public boolean isVillage(){
		return type == DistrictType.VILLAGE;
	}

	@Override
	public DistrictType getType(){
		return type;
	}

	@Override
	public long getCreated(){
		return created;
	}

	public void setCreated(long date){
		created = date;
	}

	@Override
	public UUID getCreator(){
		return creator;
	}

	public void setCreator(UUID uuid){
		creator = uuid;
	}

	@Override
	public long getChanged(){
		return changed;
	}

	@Override
	public List<Integer> getNeighbors(){
		return neighbors;
	}

	@Override
	public void setChanged(long new_change){
            changed = new_change;
            Static.getServer().worlds[0].getChunkProvider().getLoadedChunks().forEach(chunk -> {
                SignTileEntityCapabilityUtil.processChunkChange(chunk, "district");
            });
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public void setName(String new_name){
		name = new_name;
	}

	@Override
	public Municipality getMunicipality(){
		return municipality;
	}

	@Override
	public void setMunicipality(Municipality mun){
		municipality.getDistricts().removeIf(pre -> pre == this.getId());
		municipality = mun;
		municipality.getDistricts().add(this.getId());
	}

	@Override
	public UUID getManager(){
		return manager;
	}

	@Override
	public void setManager(UUID uuid){
		manager = uuid;
	}

	@Override
	public void setType(DistrictType new_type){
		type = new_type;
	}

	@Override
	public String getColor(){
		return color;
	}

	@Override
	public void setColor(String newcolor){
		color = newcolor;
	}

	@Override
	public boolean canForeignersSettle(){
		return cfs;
	}

	@Override
	public void setForeignersSettle(boolean bool){
		cfs = bool;
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
	public String getIcon(){
		return icon;
	}

	@Override
	public void setIcon(String url){
		icon = url;
	}

	@Override
	public int getClaimedChunks(){
		return chunks;
	}

	@Override
	public void setClaimedChunks(int i){
		chunks = i; if(chunks < 0){ chunks = 0; }return;
	}

}
