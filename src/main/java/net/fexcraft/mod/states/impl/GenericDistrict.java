package net.fexcraft.mod.states.impl;

import java.io.File;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.lang.ArrayList;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.DistrictType;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.util.StateUtil;

public class GenericDistrict implements District {
	
	private int id;
	private DistrictType type;
	private long created, changed;
	private UUID creator, manager;
	private ArrayList<Integer> neighbors;
	private String name;
	private Municipality municipality;
	
	public GenericDistrict(int id){
		this.id = id;
		JsonObject obj = JsonUtil.get(this.getDistrictFile());
		type = DistrictType.valueOf(JsonUtil.getIfExists(obj, "type", DistrictType.WILDERNESS.name()));
		created = JsonUtil.getIfExists(obj, "created", Time.getDate()).longValue();
		creator = UUID.fromString(obj.has("creator") ? obj.get("creator").getAsString() : States.CONSOLE_UUID);
		changed = JsonUtil.getIfExists(obj, "changed", Time.getDate()).longValue();
		neighbors = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "neighbors", new JsonArray()).getAsJsonArray());
		name = JsonUtil.getIfExists(obj, "name", "Unnamed District");
		municipality = StateUtil.getMunicipality(JsonUtil.getIfExists(obj, "municipality", -1).intValue());
		manager = obj.has("manager") ? UUID.fromString(obj.get("manager").getAsString()) : null;
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

	@Override
	public UUID getCreator(){
		return creator;
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
		municipality = mun;
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

}
