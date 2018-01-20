package net.fexcraft.mod.states.api;

import java.io.File;
import java.util.List;
import java.util.UUID;

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
	
	public long getCreated();
	
	public UUID getCreator();
	
	public long getChanged();
	
	public List<Integer> getNeighbors();
	
	public void setChanged(long new_change);

}
