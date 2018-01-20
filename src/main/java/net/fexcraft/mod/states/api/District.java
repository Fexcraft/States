package net.fexcraft.mod.states.api;

import java.io.File;

import com.google.gson.JsonObject;

import net.fexcraft.mod.states.States;

public interface District {
	
	public int getId();
	
	public boolean isVillage();

	public default File getDistrictFile(){
		return getDistrictFile(this.getId());
	}

	public static File getDistrictFile(int value){
		return new File(States.getWorldDirectory(), "districts/" + value + ".json");
	}
	
	public DistrictType getType();
	
	public JsonObject toJsonObject();

	public void save();

}
