package net.fexcraft.mod.states.api;

import java.io.File;

import com.google.gson.JsonObject;

import net.fexcraft.mod.states.States;

public interface Chunk {
	
	public int xCoord();
	
	public int zCoord();
	
	public long getPrice();
	
	public void setPrice(long new_price);
	
	public default File getChunkFile(){
		return new File(States.getWorldDirectory(), "chunks/" + getChunkRegion() + "/" + this.xCoord() + "_" + this.zCoord() + ".json");
	}

	public default String getChunkRegion(){
		return (int)Math.floor(this.xCoord() / 32.0) + "_" + (int)Math.floor(this.zCoord() / 32.0);
	}

	public void save();
	
	public JsonObject toJsonObject();
	
	public District getDistrict();

}
