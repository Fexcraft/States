package net.fexcraft.mod.states.impl;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.lang.ArrayList;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.ChunkPos;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.MunicipalityType;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.data.RuleSet;
import net.fexcraft.mod.states.data.Rules;
import net.fexcraft.mod.states.util.Config;
import net.fexcraft.mod.states.util.ForcedChunksManager;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.BlockPos;

public class GenericMunicipality implements Municipality/*, Taxable*/ {
	
	private int id;
	private String name, color, icon;
	private long created, changed, price, citizentax;
	private UUID creator, mayor;
	private Account account;
	private ArrayList<Integer> neighbors, districts, com_blacklist;
	private ArrayList<UUID> citizen, council, pl_blacklist;
	private MunicipalityType type;
	private State state;
	private boolean open, kib;
	private BlockPos mailbox;
	private RuleSet rules;
	
	public GenericMunicipality(int id){
		this.id = id;
		JsonObject obj = StateUtil.getMunicipalityJson(id);
		name = JsonUtil.getIfExists(obj, "name", "Unnamed Place");
		created = JsonUtil.getIfExists(obj, "created", Time.getDate()).longValue();
		creator = UUID.fromString(obj.has("creator") ? obj.get("creator").getAsString() : States.CONSOLE_UUID);
		changed = JsonUtil.getIfExists(obj, "changed", Time.getDate()).longValue();
		account = DataManager.getAccount("municipality:" + id, false, true);
		mayor = obj.has("mayor") ? UUID.fromString(obj.get("mayor").getAsString()) : null;
		neighbors = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "neighbors", new JsonArray()).getAsJsonArray());
		districts = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "districts", new JsonArray()).getAsJsonArray());
		citizen = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "citizen", new JsonArray()).getAsJsonArray());
		council = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "council", new JsonArray()).getAsJsonArray());
		type = MunicipalityType.getType(this);
		state = StateUtil.getState(JsonUtil.getIfExists(obj, "state", -1).intValue());
		color = JsonUtil.getIfExists(obj, "color", "#ffffff");
		open = JsonUtil.getIfExists(obj, "open", false);
		com_blacklist = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "company_blacklist", new JsonArray()).getAsJsonArray());
		pl_blacklist = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "player_blacklist", new JsonArray()).getAsJsonArray());
		price = JsonUtil.getIfExists(obj, "price", 0).longValue();
		icon = JsonUtil.getIfExists(obj, "icon", States.DEFAULT_ICON);
		kib = JsonUtil.getIfExists(obj, "kick_if_bankrupt", false);
		citizentax = JsonUtil.getIfExists(obj, "citizen_tax", 0).longValue();
		mailbox = obj.has("mailbox") ? BlockPos.fromLong(obj.get("mailbox").getAsLong()) : null;
		rules = new RuleSet(this, Rules.MUNICIPIAL);
		rules.load(obj.has("ruleset") ? obj.get("ruleset").getAsJsonObject() : new JsonObject());
	}

	@Override
	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("name", name);
		obj.addProperty("created", created);
		obj.addProperty("creator", creator.toString());
		obj.addProperty("changed", changed);
		if(!(mayor == null)){ obj.addProperty("mayor", mayor.toString()); }
		obj.add("neighbors", JsonUtil.getArrayFromIntegerList(neighbors));
		obj.add("districts", JsonUtil.getArrayFromIntegerList(districts));
		obj.add("citizen", JsonUtil.getArrayFromUUIDList(citizen));
		obj.add("council", JsonUtil.getArrayFromUUIDList(council));
		obj.addProperty("state", state.getId());
		obj.addProperty("balance", account.getBalance());
		obj.addProperty("color", color);
		obj.addProperty("open", open);
		obj.addProperty("price", price);
		if(icon != null){ obj.addProperty("icon", icon); }
		obj.addProperty("kick_if_bankrupt", kib);
		obj.addProperty("citizen_tax", citizentax);
		if(mailbox != null) obj.addProperty("mailbox", mailbox.toLong());
		obj.add("ruleset", rules.save());
		return obj;
	}

	@Override
	public void save(){
		JsonObject obj = toJsonObject();
		obj.addProperty("last_save", Time.getDate());
		File file = getMunicipalityFile();
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, obj);
	}

	@Override
	public int getId(){
		return id;
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public void setName(String new_name){
		name = new_name;
	}

	@Override
	public boolean isCapital(){
		return this.getState().getCapitalId() == this.getId();
	}

	@Override
	public void setChanged(long new_change){
		changed = new_change;
	}

	@Override
	public List<Integer> getNeighbors(){
		return neighbors;
	}

	@Override
	public List<Integer> getDistricts(){
		return districts;
	}

	@Override
	public long getCreated(){
		return created;
	}

	@Override
	public UUID getCreator(){
		return creator;
	}
	
	public void setCreator(UUID uuid){
		creator = uuid;
	}

	@Override
	public long getChanged(){
		return changed;
	}

	@Override
	public List<UUID> getCitizen(){
		return citizen;
	}

	@Override
	public Account getAccount(){
		return account;
	}

	@Override
	public UUID getHead(){
		return mayor;
	}

	@Override
	public void setHead(UUID uuid){
		mayor = uuid;
	}

	@Override
	public List<UUID> getCouncil(){
		return council;
	}

	@Override
	public MunicipalityType getType(){
		return type;
	}
	
	/** Use this method when e.g. after updating the citizen list of a Municipality.*/
	@Override
	public void updateType(){
		type = MunicipalityType.getType(this);
	}

	@Override
	public State getState(){
		return state;
	}

	@Override
	public void setState(State new_state){
		state.getMunicipalities().removeIf(pre -> pre == this.getId());
		state = new_state;
		state.getMunicipalities().add(this.getId());
	}
	
	@Override
	public String getColor(){
		return color;
	}

	@Override
	public void setColor(String newcolor){
		color = newcolor;
	}

	@Override
	public boolean isOpen(){
		return open;
	}

	@Override
	public void setOpen(boolean bool){
		open = bool;
	}

	@Override
	public List<UUID> getPlayerBlacklist(){
		return pl_blacklist;
	}

	@Override
	public List<Integer> getCompanyBlacklist(){
		return com_blacklist;
	}

	@Override
	public long getPrice(){
		return price;
	}

	@Override
	public void setPrice(long new_price){
		price = new_price;
	}

	@Override
	public String getIcon(){
		return icon;
	}

	@Override
	public void setIcon(String url){
		icon = url;
	}

	@Override
	public int getClaimedChunks(){
		int amount = 0;
		District district = null;
		for(int dis : districts){
			district = StateUtil.getDistrict(dis, false);
			if(district != null){
				amount += district.getClaimedChunks();
			}
		}
		return amount;
	}

	@Override
	public Collection<ChunkPos> getForceLoadedChunks(){
		return States.LOADED_CHUNKS.containsKey(id) ? States.LOADED_CHUNKS.get(id) : null;
	}

	@Override
	public boolean modifyForceloadedChunk(ICommandSender sender, ChunkPos pos, boolean add_rem){
		if(add_rem){
			if(this.getForceLoadedChunks() != null && this.getForceLoadedChunks().size() + 1 > Config.LOADED_CHUNKS_PER_MUNICIPALITY){
				Print.chat(sender, "&9Municipality reached the Server's Limit for Forced-Chunks-per-Municipality.");
				return false;
			}
			if(this.getForceLoadedChunks() == null){
				States.LOADED_CHUNKS.put(id, new ArrayList<>());
			}
			if(this.getForceLoadedChunks().contains(pos)){
				Print.chat(sender, "&cChunk already force-loaded.");
				return false;
			}
			this.getForceLoadedChunks().add(pos);
			ForcedChunksManager.check();
			Print.chat(sender, "&aChunk added.");
			return true;
		}
		else{
			if(this.getForceLoadedChunks() == null){
				Print.chat(sender, "&cMunicipality has no loaded chunk list.");
				return false;
			}
			if(!this.getForceLoadedChunks().contains(pos)){
				Print.chat(sender, "&cChunk is not force loaded.");
				return false;
			}
			this.getForceLoadedChunks().remove(pos);
			ForcedChunksManager.check();
			Print.chat(sender, "&aChunk removed.");
			return true;
		}
	}

	@Override
	public long getCitizenTax(){
		return citizentax;
	}

	@Override
	public void setCitizenTax(long newtax){
		citizentax = newtax;
	}

	@Override
	public boolean kickIfBankrupt(){
		return kib;
	}

	@Override
	public void setKickIfBankrupt(boolean newvalue){
		kib = newvalue;
	}

	@Override
	public void unload(){
		DataManager.unloadAccount(account);
	}
	
	@Override
	public void finalize(){ unload(); }

	@Override
	public Bank getBank(){
		return DataManager.getBank(account.getBankId(), true, true);
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
	public RuleSet getRules(){
		return rules;
	}

}
