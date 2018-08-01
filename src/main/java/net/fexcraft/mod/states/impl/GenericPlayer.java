package net.fexcraft.mod.states.impl;

import java.util.UUID;

import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.FSMMCapabilities;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.api.capabilities.PlayerCapability;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.TaxSystem;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class GenericPlayer implements PlayerCapability {
	
	private EntityPlayer entity;
	private String nick;
	private UUID uuid;
	private int color = 2;
	private long lastsave, lastpos, lasttaxcoll, customtax;
	private Account account;
	private Chunk last_chunk, current_chunk;
	//
	private Municipality municipality;
	private boolean loaded;
	
	public GenericPlayer(){}

	public GenericPlayer(UUID uuid){
		this.uuid = uuid;
		this.load();
	}

	@Override
	public void save(){
		JsonObject obj = this.toJsonObject();
		obj.addProperty("lastsave", lastsave = Time.getDate());
		JsonUtil.write(this.getPlayerFile(), obj, true);
	}

	@Override
	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("uuid", this.getUUIDAsString());
		if(nick != null){ obj.addProperty("nickname", nick); }
		obj.addProperty("color", color);
		obj.addProperty("municipality", municipality == null ? -1 : municipality.getId());
		obj.addProperty("last_tax_collection", lasttaxcoll);
		if(customtax > 0){ obj.addProperty("custom_tax", customtax); }
		return obj;
	}

	@Override
	public void load(){
		if(this.isOnlinePlayer()){
			uuid = entity.getGameProfile().getId();
		}
		JsonObject obj = JsonUtil.get(this.getPlayerFile());
		this.nick = obj.has("nickname") ? obj.get("nickname").getAsString() : null;
		this.color = JsonUtil.getIfExists(obj, "color", 2).intValue();
		Municipality mun = StateUtil.getMunicipality(JsonUtil.getIfExists(obj, "municipality", -1).intValue());
		if(mun.getId() >= 0 && mun.getCitizen().contains(uuid)){
			this.setMunicipality(mun);
		}
		else{
			this.setMunicipality(mun == null ? StateUtil.getMunicipality(-1) : mun);
			if(!this.municipality.getCitizen().contains(uuid)){
				this.municipality.getCitizen().add(uuid);
			}
		}
		this.account = this.isOnlinePlayer() ? entity.getCapability(FSMMCapabilities.PLAYER, null).getAccount() : DataManager.getAccount("player:" + uuid.toString(), true, true);
		this.lasttaxcoll = JsonUtil.getIfExists(obj, "last_tax_collection", 0).longValue();
		this.customtax = JsonUtil.getIfExists(obj, "custom_tax", 0).longValue();
		loaded = true;
		TaxSystem.processPlayerTax(TaxSystem.getProbableSchedule(), this);
	}

	@Override
	public Municipality getMunicipality(){
		return municipality;
	}

	@Override
	public void setMunicipality(Municipality mun){
		if(this.municipality != null){
			this.municipality.getCitizen().remove(this.getUUID());
			//
			for(int id : this.municipality.getDistricts()){
				District dis = StateUtil.getDistrict(id);
				if(dis == null){ continue; }
				if(dis.getManager().equals(this.getUUID())){
					dis.setManager(null);
					dis.save();
				}
			}
		}
		this.municipality = mun;
		if(!this.municipality.getCitizen().contains(this.getUUID())){
			this.municipality.getCitizen().add(this.getUUID());
		}
	}

	@Override
	public boolean isOnlinePlayer(){
		return entity != null;
	}
	
	public static PlayerCapability getOfflineInstance(UUID uuid){
		return new GenericPlayer(uuid);
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
	public String getFormattedNickname(){
		return Formatter.format(Formatter.fromInt(color) + (nick == null ? entity.getName() : nick));
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
		return uuid == null ? "null-uuid" : uuid.toString();
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

	@Override
	public void setEntityPlayer(EntityPlayer player){
		this.entity = player;
	}

	@Override
	public EntityPlayer getEntityPlayer(){
		return entity;
	}

	@Override
	public State getState(){
		return municipality.getState();
	}

	@Override
	public String getWebhookNickname(){
		return nick == null ? entity.getName() : nick + " (" + entity.getName() + ")";
	}

	@Override
	public void setRawNickname(String name){
		this.nick = name;
	}

	@Override
	public void setNicknameColor(int color){
		this.color = color;
	}

	@Override
	public boolean isLoaded(){
		return loaded;
	}

	@Override
	public long lastTaxCollection(){
		return lasttaxcoll;
	}

	@Override
	public void onTaxCollected(long time){
		this.lasttaxcoll = time;
		this.save();
	}

	@Override
	public long getCustomTax(){
		return customtax;
	}

	@Override
	public void setCustomTax(long newtax){
		this.customtax = newtax;
	}

	@Override
	public void unload(){
		DataManager.unloadAccount(account);
	}
	
	@Override
	public void finalize(){
		if(entity == null && account != null){
			DataManager.save(account);
		}
	}

	@Override
	public Bank getBank(){
		return this.isOnlinePlayer() ? entity.getCapability(FSMMCapabilities.PLAYER, null).getBank() : DataManager.getBank(account.getBankId(), true, true);
	}

}
