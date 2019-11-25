package net.fexcraft.mod.states.api;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.ChunkPos;
import net.fexcraft.mod.states.data.RuleSet.RuleHolder;
import net.fexcraft.mod.states.data.root.AccountHolder;
import net.fexcraft.mod.states.data.root.BuyableType;
import net.fexcraft.mod.states.data.root.ColorHolder;
import net.fexcraft.mod.states.data.root.IconHolder;
import net.fexcraft.mod.states.data.root.MailReceiver;
import net.minecraft.command.ICommandSender;

public interface Municipality extends ColorHolder, BuyableType, IconHolder, AccountHolder, MailReceiver, RuleHolder {
	
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
	
	public MunicipalityType getType();
	
	public void updateType();
	
	public State getState();
	
	public void setState(State new_state);
	
	public boolean isOpen();
	
	public void setOpen(boolean bool);

	public List<UUID> getPlayerBlacklist();

	public List<Integer> getCompanyBlacklist();

	public int getClaimedChunks();
	
	public @Nullable Collection<ChunkPos> getForceLoadedChunks();
	
	public @Nullable boolean modifyForceloadedChunk(ICommandSender sender, ChunkPos pos, boolean add_rem);

	public long getCitizenTax();
	
	public void setCitizenTax(long tax);

	public boolean kickIfBankrupt();
	
	public void setKickIfBankrupt(boolean newvalue);

}
