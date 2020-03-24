package net.fexcraft.mod.states.data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.FSMMCapabilities;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.TaxSystem;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class PlayerImpl implements PlayerCapability {
	
	private EntityPlayer entity;
	private String nick;
	private UUID offuuid;
	private int color = 2;
	private long lastsave, lastpos, lasttaxcoll, customtax;
	private Account account;
	private Chunk last_chunk, current_chunk;
	private BlockPos mailbox;
	//
	private Municipality municipality;
	private boolean loaded;
	
	public PlayerImpl(){}

	public PlayerImpl(UUID uuid){
		this.offuuid = uuid; this.load();
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
		if(mailbox != null) obj.addProperty("mailbox", mailbox.toLong());
		return obj;
	}

	@Override
	public void load(){
		offuuid = this.getUUID();
		JsonObject obj = JsonUtil.get(this.getPlayerFile());
		this.nick = obj.has("nickname") ? obj.get("nickname").getAsString() : null;
		this.color = JsonUtil.getIfExists(obj, "color", 2).intValue();
		Municipality mun = StateUtil.getMunicipality(JsonUtil.getIfExists(obj, "municipality", -1).intValue());
		if(mun != null && mun.getId() >= 0 && mun.getCitizen().contains(getUUID())){
			this.setMunicipality(mun);
		}
		else{
			this.setMunicipality(mun == null ? StateUtil.getMunicipality(-1) : mun);
			if(!this.municipality.getCitizen().contains(getUUID())){
				this.municipality.getCitizen().add(getUUID());
			}
		}
		this.account = this.isOnlinePlayer() ? entity.getCapability(FSMMCapabilities.PLAYER, null).getAccount() : DataManager.getAccount("player:" + getUUID().toString(), true, true);
		this.lasttaxcoll = JsonUtil.getIfExists(obj, "last_tax_collection", 0).longValue();
		this.customtax = JsonUtil.getIfExists(obj, "custom_tax", 0).longValue();
		this.mailbox = obj.has("mailbox") ? BlockPos.fromLong(obj.get("mailbox").getAsLong()) : null;
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
				if(dis.getHead().equals(this.getUUID())){
					dis.setHead(null);
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
		return new PlayerImpl(uuid);
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
		return entity == null ? offuuid : entity.getUniqueID();
	}

	@Override
	public Account getAccount(){
		return account;
	}

	@Override
	public String getUUIDAsString(){
		return getUUID() == null ? "null-uuid" : getUUID().toString();
	}

	@Override
	public boolean isDistrictManagerOf(District district){
		return district.getHead() != null && district.getHead().equals(getUUID());
	}

	@Override
	public boolean isMayorOf(Municipality municipality){
		return municipality.getHead() != null && municipality.getHead().equals(getUUID());
	}

	@Override
	public boolean isStateLeaderOf(State state){
		return state.getHead() != null && state.getHead().equals(getUUID());
	}

	@Override
	public boolean canLeave(ICommandSender sender){
		if(municipality == null){ return true; }
		if(municipality.getHead().equals(getUUID())){
			Print.chat(sender, "&eYou must assign a new Mayor first, or remove youself as one, before you can leave the Municipality.");
			return false;
		}
		if(municipality.getCouncil().size() < 2 && municipality.getCouncil().contains(getUUID())){
			Print.chat(sender, "&eYou cannot leave the Municipality as last Council member.");
			return false;
		}
		if(municipality.getId() > 0 && municipality.getCitizen().size() == 1){
			Print.chat(sender, "&eYou cannot leave the Municipality as last citizen!");
			Print.chat(sender, "&eUse &7/mun abandon &einstead or &7/mun claim &eto become Mayor!");
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

	@Override
	public BlockPos getMailbox(){
		return mailbox;
	}

	@Override
	public void setMailbox(BlockPos pos){
		this.mailbox = pos;
	}

	@Override
	public void copyFromOld(PlayerCapability capability){
		PlayerImpl player = (PlayerImpl)capability;
		this.nick = player.nick;
		this.offuuid = player.offuuid;
		this.color = player.color;
		this.lastsave = player.lastsave;
		this.lastpos = player.lastpos;
		this.lasttaxcoll = player.lasttaxcoll;
		this.customtax = player.customtax;
		this.account = player.account;
		this.last_chunk = player.last_chunk;
		this.current_chunk = player.current_chunk;
		this.mailbox = player.mailbox;
		this.municipality = player.municipality;
	}

	@Override
	public boolean hasRelevantVotes(){
		return States.VOTES.values().stream().filter(pre -> pre.isVoter(null, getUUID())).count() > 0;
	}

	@Override
	public List<Vote> getRelevantVotes(){
		return States.VOTES.values().stream().filter(pre -> pre.isVoter(null, getUUID())).collect(Collectors.toList());
	}

}
