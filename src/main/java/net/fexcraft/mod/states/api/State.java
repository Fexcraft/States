package net.fexcraft.mod.states.api;

import java.io.File;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.root.BuyableType;
import net.fexcraft.mod.states.api.root.ColorHolder;
import net.fexcraft.mod.states.api.root.IconHolder;

public interface State extends ColorHolder, BuyableType, IconHolder {
	
	public int getId();
	
	public String getName();
	
	public void setName(String new_name);
	
	public boolean isUnionCapital();
	
	public default File getStateFile(){
		return getStateFile(this.getId());
	}

	public static File getStateFile(int value){
		return new File(States.getSaveDirectory(), "states/" + value + ".json");
	}
	
	public void setChanged(long new_change);
	
	public List<Integer> getMunicipalities();
	
	public List<Integer> getNeighbors();
	
	public long getCreated();
	
	public UUID getCreator();
	
	public long getChanged();
	
	public JsonObject toJsonObject();

	public void save();
	
	public Account getAccount();
	
	public UUID getLeader();
	
	public void setLeader(UUID uuid);
	
	/** */
	public List<UUID> getCouncil();
	
	public int getCapitalId();
	
	public void setCapitalId(int id);

	public List<Integer> getBlacklist();

	public byte getChunkTaxPercentage();
	
	public void setChunkTaxPercentage(byte newtax);

}
