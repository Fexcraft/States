package net.fexcraft.mod.states.impl;

import java.util.UUID;

import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.lib.perms.player.AttachedData;
import net.fexcraft.mod.lib.perms.player.PlayerPerms;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.Player;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.ICommandSender;

public class GenericPlayer implements AttachedData, Player {
	
	private PlayerPerms perms;
	private boolean online_instance;
	private String nick;
	private UUID uuid;
	private int color;
	private long lastsave, lastpos;
	private Account account;
	private Chunk last_chunk, current_chunk;
	//
	private Municipality municipality;
	
	/** Internal Use Only. */
	private GenericPlayer(){}
	
	public GenericPlayer(PlayerPerms pp){
		perms = pp;
	}

	@Override
	public String getId(){
		return net.fexcraft.mod.states.States.PLAYER_DATA;
	}

	@Override
	public JsonObject save(UUID uuid){
		JsonObject obj = new JsonObject();
		obj.addProperty("UUID", (this.uuid = uuid).toString());
		if(nick != null){
			obj.addProperty("Nick", nick);
		}
		obj.addProperty("Color", color);
		obj.addProperty("Municipality", municipality.getId());
		obj.addProperty("LastSave", lastsave = Time.getDate());
		return obj;
	}

	@Override
	public AttachedData load(UUID uuid, JsonObject obj){
		if(obj == null){
			obj = new JsonObject();
		}
		online_instance = true;
		this.uuid = uuid;
		this.nick = obj.has("Nickname") ? obj.get("Nickname").getAsString() : null;
		this.color = JsonUtil.getIfExists(obj, "Color", 2).intValue();
		//this.municipality = StateUtil.getMunicipality(JsonUtil.getIfExists(obj, "Municipality", -1).intValue());
		Municipality mun = StateUtil.getMunicipality(JsonUtil.getIfExists(obj, "Municipality", -1).intValue());
		if(mun.getId() >= 0 && mun.getCitizen().contains(uuid)){
			this.setMunicipality(mun);
		}
		else{
			this.setMunicipality(mun == null ? StateUtil.getMunicipality(-1) : mun);
			if(!this.municipality.getCitizen().contains(uuid)){
				this.municipality.getCitizen().add(uuid);
			}
		}
		this.account = AccountManager.INSTANCE.getAccount("player", uuid.toString(), true);
		return this;
	}

	@Override
	public Municipality getMunicipality(){
		return municipality;
	}

	@Override
	public void setMunicipality(Municipality mun){
		if(this.municipality != null){
			this.municipality.getCitizen().remove(uuid);
			//
			for(int id : this.municipality.getDistricts()){
				District dis = StateUtil.getDistrict(id);
				if(dis == null){ continue; }
				if(dis.getManager().equals(uuid)){
					dis.setManager(null);
					dis.save();
				}
			}
		}
		this.municipality = mun;
		if(!this.municipality.getCitizen().contains(uuid)){
			this.municipality.getCitizen().add(uuid);
		}
	}

	@Override
	public boolean isOnlinePlayer(){
		return online_instance;
	}
	
	public static Player getOfflineInstance(UUID uuid, JsonObject obj){
		GenericPlayer player = new GenericPlayer();
		player.load(uuid, obj);
		player.online_instance = false;
		return player;
	}

	@Override
	public long getLastSave(){
		return lastsave;
	}

	@Override
	public String getRawNickname(){
		return nick;
	}

	@Override
	public int getNicknameColor(){
		return color;
	}

	@Override
	public String getFormattedNickname(ICommandSender player){
		return Formatter.format(Formatter.fromInt(color) + (nick == null ? player.getName() : nick));
	}

	@Override
	public PlayerPerms getPermissions(){
		return perms;
	}

	@Override
	public UUID getUUID(){
		return uuid;
	}

	@Override
	public Account getAccount(){
		return account;
	}

	@Override
	public String getUUIDAsString(){
		return uuid.toString();//TODO
	}

	@Override
	public boolean isDistrictManagerOf(District district){
		return district.getManager() != null && district.getManager().equals(uuid);
	}

	@Override
	public boolean isMayorOf(Municipality municipality){
		return municipality.getMayor() != null && municipality.getMayor().equals(uuid);
	}

	@Override
	public boolean isStateLeaderOf(State state){
		return state.getLeader() != null && state.getLeader().equals(uuid);
	}

	@Override
	public boolean canLeave(ICommandSender sender){
		if(this.municipality == null){ return true; }
		if(this.municipality.getMayor().equals(uuid)){
			Print.chat(sender, "&eYou must assign a new Mayor first, or remove youself as one, before you can leave the Municipality.");
			return false;
		}
		if(this.municipality.getCouncil().size() < 2){
			Print.chat(sender, "&eYou cannot leave the Municipality as last Council member.");
			return false;
		}
		return true;
	}

	@Override
	public Chunk getLastChunk(){
		return last_chunk;
	}

	@Override
	public Chunk getCurrentChunk(){
		return current_chunk;
	}

	@Override
	public void setCurrenkChunk(Chunk chunk){
		last_chunk = current_chunk;
		current_chunk = chunk;
	}

	@Override
	public long getLastPositionUpdate(){
		return lastpos;
	}

	@Override
	public void setPositionUpdate(long leng){
		lastpos = leng;
	}

}
