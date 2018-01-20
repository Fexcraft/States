package net.fexcraft.mod.states.impl;

import java.io.File;

import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.DistrictType;

public class GenericDistrict implements District {
	
	private int id;
	private DistrictType type;
	
	public GenericDistrict(int id){
		this.id = id;
		JsonObject obj = JsonUtil.get(this.getDistrictFile());
		type = DistrictType.valueOf(JsonUtil.getIfExists(obj, "type", DistrictType.WILDERNESS.name()));
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
	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("type", type.name());
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

}
