package net.fexcraft.mod.states.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.root.AccountHolder;
import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.root.Layer;
import net.fexcraft.mod.states.data.root.Layers;
import net.fexcraft.mod.states.data.root.Populated;
import net.fexcraft.mod.states.data.sub.*;
import net.fexcraft.mod.states.events.MunicipalityEvent;
import net.fexcraft.mod.states.util.ForcedChunksManager;
import net.fexcraft.mod.states.util.StConfig;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.MinecraftForge;

public class Municipality implements Layer, AccountHolder, Populated {
	
	private int id;
	private String name, title;
	private long citizentax;
	private Account account;
	private ArrayList<Integer> districts, com_blacklist;
	private ArrayList<UUID> citizen, pl_blacklist;
	private County county;
	//
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public Buyable price = new Buyable(this);
	public MailData mailbox = new MailData();
	public Createable created = new Createable();
	public ExternalDataHolder external = new ExternalDataHolder();
	public Abandonable abandon;
	public Manageable manage = new Manageable(this, true, false, "mayor");
	public RuleHolder rules = new RuleHolder();
	public NeighborData neighbors = new NeighborData();
	//
	public final Rule r_OPEN, r_COLOR, r_ICON, r_SET_NAME, r_SET_PRICE, r_SET_MAYOR, r_SET_CITIZENTAX, r_KIB;
	public final Rule r_EDIT_BL, r_KICK, r_INVITE, r_COUNCIL_KICK, r_COUNCIL_INVITE, r_VOTE_MAYOR;
	public final Rule r_CREATE_DISTRICT, r_SET_CHUNKRULES, r_CREATE_SIGN_SHOP, r_SET_MAILBOX, r_OPEN_MAILBOX;
	public final Rule r_FORCE_LOAD_CHUNKS, r_SET_RULESET, r_RESET_MAYOR, r_ABANDON, r_SET_TITLE;
	
	public Municipality(int id){
		this.id = id;
		JsonObject obj = StateUtil.getMunicipalityJson(id);
		name = JsonUtil.getIfExists(obj, "name", "Unnamed Place");
		title = JsonUtil.getIfExists(obj, "title", "Untitled");
		created.load(obj);
		manage.load(obj);
		account = DataManager.getAccount("municipality:" + id, false, true).setName(name);
		neighbors.load(obj);
		districts = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "districts", new JsonArray()).getAsJsonArray());
		citizen = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "citizen", new JsonArray()).getAsJsonArray());
		if(!obj.has("county")){
			State state = StateUtil.getState(JsonUtil.getIfExists(obj, "state", -1).intValue());
			county = new County(StateUtil.CURRENT.newCountyId());
			county.setName(name + " County");
			county.setState(state);
			setCounty(county);
		}
		else county = StateUtil.getCounty(JsonUtil.getIfExists(obj, "county", -1).intValue());
		color.load(obj);
		com_blacklist = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "company_blacklist", new JsonArray()).getAsJsonArray());
		pl_blacklist = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "player_blacklist", new JsonArray()).getAsJsonArray());
		price.load(obj);
		icon.load(obj);
		citizentax = JsonUtil.getIfExists(obj, "citizen_tax", 0).longValue();
		mailbox.load(obj);
		abandon = new Abandonable(this, mun -> {
			manage.getCouncil().clear();
			ArrayList<UUID> list = (ArrayList<UUID>)citizen.clone();
			list.forEach(citizen -> {
				PlayerCapability cap = StateUtil.getPlayer(citizen, true);
				cap.setMunicipality(StateUtil.getMunicipality(-1));
				cap.save();
			});
			citizen.clear();
			manage.setHead(null);
			save();
		}, by -> {
			manage.getCouncil().clear();
			manage.getCouncil().add(by.getUUID());
			citizen.clear();
			citizen.add(by.getUUID());
			setCounty(by.getCounty());
			by.setMunicipality(this);
			manage.setHead(by.getUUID());
			save();
		});
		abandon.load(obj);
		manage.linkRuleHolder(rules);
		rules.add(r_SET_NAME = new Rule("set.name", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_PRICE = new Rule("set.price", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_MAYOR = new Rule("set.mayor", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_TITLE = new Rule("set.title", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_OPEN = new Rule("open_to_join", false, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_COLOR = new Rule("set.color", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_ICON = new Rule("set.icon", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_CITIZENTAX = new Rule("set.citizentax", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_KIB = new Rule("kick_if_bankrupt", false, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_EDIT_BL = new Rule("edit.blacklist", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_KICK = new Rule("kick_player", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_INVITE = new Rule("invite_player", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_COUNCIL_KICK = new Rule("kick_council", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_COUNCIL_INVITE = new Rule("invite_council", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_VOTE_MAYOR = new Rule("vote.mayor", null, true, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_CREATE_DISTRICT = new Rule("create.district", null, false, Initiator.CITIZEN_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_CHUNKRULES = new Rule("set.chunkrules", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_CREATE_SIGN_SHOP = new Rule("create.sign-shops", null, false, Initiator.INCHARGE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_MAILBOX = new Rule("set.mailbox", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_OPEN_MAILBOX = new Rule("open.mailbox", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_FORCE_LOAD_CHUNKS = new Rule("force-load.chunks", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_RULESET = new Rule("set.ruleset-name", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_RESET_MAYOR = new Rule("set.mayor.none", null, false, Initiator.CITIZEN_VOTE, Initiator.HIGHERINCHARGE));
		rules.add(r_ABANDON = new Rule("abandon", null, true, Initiator.INCHARGE, Initiator.INCHARGE));
		rules.getMap().lock();
		rules.load(obj);
		//import old settings from old saves
		if(obj.has("open")){ r_OPEN.set(obj.get("open").getAsBoolean()); }
		if(obj.has("kick_if_bankrupt")){ r_KIB.set(obj.get("kick_if_bankrupt").getAsBoolean()); }
		//
		MinecraftForge.EVENT_BUS.post(new MunicipalityEvent.Load(this));
		rules.loadEx(obj);
		external.load(obj);
	}

	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("name", name);
		obj.addProperty("title", title);
		created.save(obj);
		manage.save(obj);
		neighbors.save(obj);
		obj.add("districts", JsonUtil.getArrayFromIntegerList(districts));
		obj.add("citizen", JsonUtil.getArrayFromUUIDList(citizen));
		obj.addProperty("county", county.getId());
		obj.addProperty("balance", account.getBalance());
		color.save(obj);
		//obj.addProperty("open", open);
		price.save(obj);
		icon.save(obj);
		//obj.addProperty("kick_if_bankrupt", kib);
		obj.addProperty("citizen_tax", citizentax);
		mailbox.save(obj);
		abandon.save(obj);
		rules.save(obj);
		external.save(obj);
		return obj;
	}

	public void save(){
		JsonObject obj = toJsonObject();
		obj.addProperty("last_save", Time.getDate());
		File file = getMunicipalityFile();
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, obj);
	}
	
	public final File getMunicipalityFile(){
		return getMunicipalityFile(this.getId());
	}

	public static File getMunicipalityFile(int value){
		return new File(States.getSaveDirectory(), "municipalitites/" + value + ".json");
	}

	public int getId(){
		return id;
	}

	public String getName(){
		return name;
	}

	public void setName(String new_name){
		name = new_name;
		account.setName(new_name);
	}

	public boolean isCapital(){
		return this.getState().getCapitalId() == this.getId();
	}

	public List<Integer> getDistricts(){
		return districts;
	}

	@Override
	public Account getAccount(){
		return account;
	}

	public County getCounty(){
		return county;
	}

	public void setCounty(County new_county){
		county.getMunicipalities().removeIf(pre -> pre == this.getId());
		county = new_county;
		county.getMunicipalities().add(this.getId());
	}

	public State getState(){
		return county.getState();
	}

	public List<UUID> getPlayerBlacklist(){
		return pl_blacklist;
	}

	public List<Integer> getCompanyBlacklist(){
		return com_blacklist;
	}

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

	public Collection<ChunkPos> getForceLoadedChunks(){
		return States.LOADED_CHUNKS.containsKey(id) ? States.LOADED_CHUNKS.get(id) : null;
	}

	public boolean modifyForceloadedChunk(ICommandSender sender, ChunkPos pos, boolean add_rem){
		if(add_rem){
			if(this.getForceLoadedChunks() != null && this.getForceLoadedChunks().size() + 1 > StConfig.LOADED_CHUNKS_PER_MUNICIPALITY){
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

	public long getCitizenTax(){
		return citizentax;
	}

	public void setCitizenTax(long newtax){
		citizentax = newtax;
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

	public int getDistrictLimit(){
		return getClaimedChunks() / StConfig.CHUNKS_FOR_DISTRICT + 1;
	}

	public int getChunkLimit(){
		return citizen.size() * StConfig.CHUNK_PER_CITIZEN;
	}

	public String getTitle(){
		return title;
	}

	public void setTitle(String newtitle){
		title = newtitle;
	}

	public String getTitledName(){
		return "(" + title + ") " + name;
	}

	@Override
	public Layer getParent(){
		return county;
	}

	@Override
	public Layers getLayerType(){
		return Layers.MUNICIPALITY;
	}

	@Override
	public List<UUID> getResidents(){
		return citizen;
	}

	@Override
	public List<UUID> getAllResidents(){
		return citizen;
	}

	@Override
	public boolean isCitizen(UUID uuid){
		return citizen.contains(uuid);
	}

}
