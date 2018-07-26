package net.fexcraft.mod.states.api;

import java.io.File;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;

import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.root.BuyableType;
import net.fexcraft.mod.states.api.root.ColorHolder;
import net.fexcraft.mod.states.api.root.IconHolder;

public interface District extends ColorHolder, BuyableType, IconHolder {
	
	public int getId();
	
	public boolean isVillage();

	public default File getDistrictFile(){
		return getDistrictFile(this.getId());
	}

	public static File getDistrictFile(int value){
		return new File(States.getSaveDirectory(), "districts/" + value + ".json");
	}
	
	public DistrictType getType();
	
	public void setType(DistrictType new_type);
	
	public JsonObject toJsonObject();

	public void save();
	
	public long getCreated();
	
	public UUID getCreator();
	
	public long getChanged();
	
	public List<Integer> getNeighbors();
	
	public void setChanged(long new_change);
	
	public String getName();
	
	public void setName(String new_name);
	
	public Municipality getMunicipality();
	
	public void setMunicipality(Municipality mun);
	
	public UUID getManager();
	
	public void setManager(UUID uuid);
	
	public boolean canForeignersSettle();
	
	public void setForeignersSettle(boolean bool);
	
	public default State getState(){
		return this.getMunicipality().getState();
	}

	public int getClaimedChunks();
	
	public void setClaimedChunks(int i);

	public long getChunkTax();
	
	public void setChunkTax(long tax);

	public boolean unclaimIfBankrupt();
	
	public void setUnclaimIfBankrupt(boolean newvalue);
	
}
