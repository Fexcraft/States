package net.fexcraft.mod.states.data.capabilities;

import java.io.File;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.County;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.Vote;
import net.fexcraft.mod.states.data.root.AccountHolder;
import net.fexcraft.mod.states.data.root.MunCt;
import net.fexcraft.mod.states.data.root.Taxable;
import net.fexcraft.mod.states.data.sub.MailData;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public interface PlayerCapability extends ICommandSender, Taxable, AccountHolder {
	
	public Municipality getMunicipality();
	
	public void setMunicipality(Municipality mun);

	public County getCounty();
	
	public void setCounty(County county);

	public MunCt getMunCt();
	
	public boolean isOnlinePlayer();
	
	public long getLastSave();

	public void setRawNickname(String name);
	
	@Nullable
	public String getRawNickname();
	
	public int getNicknameColor();

	public void setNicknameColor(int color);
	
	public String getFormattedNickname();

	public String getWebhookNickname();
	
	public UUID getUUID();

	public String getUUIDAsString();

	public boolean isDistrictManagerOf(District district);

	public boolean isMayorOf(Municipality municipality);

	public boolean isCountyManagerOf(County county);

	public boolean isStateLeaderOf(State state);

	public boolean canLeave(ICommandSender sender, boolean mun);
	
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
		return new File(States.getSaveDirectory(), "players/" + (uuid == null ? "null-uuid" : uuid.toString()) + ".json");
	}
	
	public void save();
	
	public void load();

	public JsonObject toJsonObject();

	public State getState();
	
	/// ---- ///

	@Override
	public default String getName(){
		return this.getEntityPlayer().getName();
	}

	@Override
	public default boolean canUseCommand(int permLevel, String commandName){
		return this.getEntityPlayer().canUseCommand(permLevel, commandName);
	}

	@Override
	public default World getEntityWorld(){
		return this.getEntityPlayer().getEntityWorld();
	}

	@Override
	public default MinecraftServer getServer(){
		return this.getEntityPlayer().getServer();
	}

	public boolean isLoaded();

	public void copyFromOld(PlayerCapability capability);

	public boolean hasRelevantVotes();
	
	public List<Vote> getRelevantVotes();

	public boolean isAdmin();

	public void setAdminMode(boolean bool);
	
	public MailData getMailbox();

}
