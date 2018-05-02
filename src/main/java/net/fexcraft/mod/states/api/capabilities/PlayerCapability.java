package net.fexcraft.mod.states.api.capabilities;

import java.io.File;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.lib.perms.player.PlayerPerms;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public interface PlayerCapability {
	
	public Municipality getMunicipality();
	
	public void setMunicipality(Municipality mun);
	
	public boolean isOnlinePlayer();
	
	public long getLastSave();
	
	@Nullable
	public String getRawNickname();
	
	public int getNicknameColor();
	
	public String getFormattedNickname(ICommandSender player);
	
	public PlayerPerms getPermissions();
	
	public UUID getUUID();

	public String getUUIDAsString();

	public Account getAccount();

	public boolean isMayorOf(Municipality municipality);

	public boolean isStateLeaderOf(State state);

	public boolean isDistrictManagerOf(District district);

	public boolean canLeave(ICommandSender sender);
	
	public Chunk getLastChunk();
	
	public Chunk getCurrentChunk();
	
	public void setCurrenkChunk(Chunk chunk);
	
	public long getLastPositionUpdate();
	
	public void setPositionUpdate(long leng);

	public void setEntityPlayer(EntityPlayer player);

	public EntityPlayer getEntityPlayer();
	
	public default File getPlayerFile(){
		return getPlayerFile(this.getUUID());
	}

	public static File getPlayerFile(UUID uuid){
		return new File(States.getSaveDirectory(), "players/" + uuid.toString() + ".json");
	}
	
	public void save();
	
	public void load();

	public JsonObject toJsonObject();

}
