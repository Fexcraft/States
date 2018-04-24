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

public interface Municipality extends ColorHolder, BuyableType, IconHolder {
	
	public int getId();
	
	public String getName();
	
	public void setName(String new_name);
	
	public boolean isCapital();
	
	public default File getMunicipalityFile(){
		return getMunicipalityFile(this.getId());
	}

	public static File getMunicipalityFile(int value){
		return new File(States.getSaveDirectory(), "municipalitites/" + value + ".json");
	}
	
	public void setChanged(long new_change);
	
	public List<UUID> getCitizen();
	
	public List<Integer> getNeighbors();
	
	public List<Integer> getDistricts();
	
	public long getCreated();
	
	public UUID getCreator();
	
	public long getChanged();
	
	public JsonObject toJsonObject();

	public void save();
	
	public Account getAccount();
	
	public UUID getMayor();
	
	public void setMayor(UUID uuid);
	
	public List<UUID> getCouncil();
	
	public MunicipalityType getType();
	
	public void updateType();
	
	public State getState();
	
	public void setState(State new_state);
	
	public boolean isOpen();
	
	public void setOpen(boolean bool);

	public List<UUID> getPlayerBlacklist();

	public List<Integer> getCompanyBlacklist();

}
