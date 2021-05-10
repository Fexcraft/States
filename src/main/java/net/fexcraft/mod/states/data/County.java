package net.fexcraft.mod.states.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.root.AccountHolder;
import net.fexcraft.mod.states.data.root.ChildLayer;
import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.root.Layer;
import net.fexcraft.mod.states.data.root.Ruleable;
import net.fexcraft.mod.states.data.root.VoteHolder;
import net.fexcraft.mod.states.data.sub.Buyable;
import net.fexcraft.mod.states.data.sub.ColorData;
import net.fexcraft.mod.states.data.sub.Createable;
import net.fexcraft.mod.states.data.sub.ExternalDataHolder;
import net.fexcraft.mod.states.data.sub.IconHolder;
import net.fexcraft.mod.states.data.sub.MailData;
import net.fexcraft.mod.states.events.CountyEvent;
import net.fexcraft.mod.states.util.RuleMap;
import net.fexcraft.mod.states.util.StConfig;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraftforge.common.MinecraftForge;

public class County implements ChildLayer, AccountHolder, Ruleable, VoteHolder {
	
	private int id;
	private String name;
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public Buyable price = new Buyable(this, Layer.COUNTY);
	public MailData mailbox = new MailData();
	public Createable created = new Createable();
	public ExternalDataHolder external = new ExternalDataHolder();
	private long citizentax;
	private UUID manager;
	private Account account;
	private ArrayList<Integer> neighbors, districts, municipalities;
	private ArrayList<UUID> direct_citizen, council;
	private State state;
	//
	private RuleMap rules = new RuleMap();
	private String ruleset_name;
	public final Rule r_OPEN, r_COLOR, r_ICON, r_SET_NAME, r_SET_PRICE, r_SET_MANAGER, r_SET_CITIZENTAX, r_KIB;
	public final Rule r_KICK, r_INVITE, r_COUNCIL_KICK, r_COUNCIL_INVITE, r_VOTE_MANAGER;
	public final Rule r_CREATE_DISTRICT, r_SET_CHUNKRULES, r_CREATE_SIGN_SHOP, r_SET_MAILBOX, r_OPEN_MAILBOX;
	public final Rule r_SET_RULESET, r_RESET_MANAGER, r_SET_TITLE;
	private ArrayList<Vote> active_votes = new ArrayList<>();
	
	public County(int id){
		this.id = id;
		JsonObject obj = StateUtil.getMunicipalityJson(id);
		name = JsonUtil.getIfExists(obj, "name", "Unnamed Place");
		created.load(obj);
		account = DataManager.getAccount("municipality:" + id, false, true).setName(name);
		manager = obj.has("manager") ? UUID.fromString(obj.get("manager").getAsString()) : null;
		neighbors = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "neighbors", new JsonArray()).getAsJsonArray());
		districts = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "districts", new JsonArray()).getAsJsonArray());
		municipalities = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "municipalities", new JsonArray()).getAsJsonArray());
		direct_citizen = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "citizen", new JsonArray()).getAsJsonArray());
		council = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "council", new JsonArray()).getAsJsonArray());
		state = StateUtil.getState(JsonUtil.getIfExists(obj, "state", -1).intValue());
		color.load(obj);
		price.load(obj);
		icon.load(obj);
		citizentax = JsonUtil.getIfExists(obj, "citizen_tax", 0).longValue();
		mailbox.load(obj);
		ruleset_name = JsonUtil.getIfExists(obj, "ruleset", "Standard Ruleset");
		rules.add(r_SET_NAME = new Rule("set.name", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_PRICE = new Rule("set.price", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_MANAGER = new Rule("set.manager", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_TITLE = new Rule("set.title", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_OPEN = new Rule("open_to_join", false, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_COLOR = new Rule("set.color", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_ICON = new Rule("set.icon", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_CITIZENTAX = new Rule("set.citizentax", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_KIB = new Rule("kick_if_bankrupt", false, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_KICK = new Rule("kick_player", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_INVITE = new Rule("invite_player", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_COUNCIL_KICK = new Rule("kick_council", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_COUNCIL_INVITE = new Rule("invite_council", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_VOTE_MANAGER = new Rule("vote.mayor", null, true, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_CREATE_DISTRICT = new Rule("create.district", null, false, Initiator.CITIZEN_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_CHUNKRULES = new Rule("set.chunkrules", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_CREATE_SIGN_SHOP = new Rule("create.sign-shops", null, false, Initiator.INCHARGE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_MAILBOX = new Rule("set.mailbox", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_OPEN_MAILBOX = new Rule("open.mailbox", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_RULESET = new Rule("set.ruleset-name", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_RESET_MANAGER = new Rule("set.manager.none", null, false, Initiator.CITIZEN_VOTE, Initiator.HIGHERINCHARGE));
		rules.lock();
		if(obj.has("rules")){
			JsonObject rls = obj.get("rules").getAsJsonObject();
			for(Map.Entry<String, JsonElement> entry : rls.entrySet()){
				Rule rule = rules.get(entry.getKey());
				if(rule != null) rule.load(entry.getValue().getAsString());
			}
		}
		//import old settings from old saves
		if(obj.has("open")){ r_OPEN.set(obj.get("open").getAsBoolean()); }
		if(obj.has("kick_if_bankrupt")){ r_KIB.set(obj.get("kick_if_bankrupt").getAsBoolean()); }
		//
		if(obj.has("votes")){
			ArrayList<Integer> list = JsonUtil.jsonArrayToIntegerArray(obj.get("votes").getAsJsonArray());
			for(int i : list){
				Vote vote = StateUtil.getVote(this, i); if(vote == null || vote.expired(null)) continue; active_votes.add(vote);
			}
		}
		MinecraftForge.EVENT_BUS.post(new CountyEvent.Load(this));
		if(obj.has("ex-rules") && rules.hasExternal()){
			JsonObject rls = obj.get("ex-rules").getAsJsonObject();
			for(Map.Entry<String, JsonElement> entry : rls.entrySet()){
				Rule rule = rules.get(entry.getKey());
				if(rule != null) rule.load(entry.getValue().getAsString());
			}
		}
		external.load(obj);
	}

	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("name", name);
		created.save(obj);
		if(manager != null) obj.addProperty("mayor", manager.toString());
		obj.add("neighbors", JsonUtil.getArrayFromIntegerList(neighbors));
		obj.add("districts", JsonUtil.getArrayFromIntegerList(districts));
		obj.add("municipalities", JsonUtil.getArrayFromIntegerList(municipalities));
		obj.add("citizen", JsonUtil.getArrayFromUUIDList(direct_citizen));
		obj.add("council", JsonUtil.getArrayFromUUIDList(council));
		obj.addProperty("state", state.getId());
		obj.addProperty("balance", account.getBalance());
		color.save(obj);
		//obj.addProperty("open", open);
		price.save(obj);
		icon.save(obj);
		//obj.addProperty("kick_if_bankrupt", kib);
		obj.addProperty("citizen_tax", citizentax);
		mailbox.save(obj);
		obj.addProperty("ruleset", ruleset_name);
		{
			JsonObject rells = new JsonObject();
			for(Rule rule : rules.values()) if(!rule.isExternal()) rells.addProperty(rule.id, rule.save());
			obj.add("rules", rells);
		}
		if(rules.hasExternal()){
			JsonObject erells = new JsonObject();
			for(Rule rule : rules.values()) if(rule.isExternal()) erells.addProperty(rule.id, rule.save());
			obj.add("ex-rules", erells);
		}
		if(!active_votes.isEmpty()){
			JsonArray array = new JsonArray();
			for(Vote vote : active_votes) array.add(vote.id);
			obj.add("votes", array);
		}
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

	public List<Integer> getNeighbors(){
		return neighbors;
	}

	public List<Integer> getDistricts(){
		return districts;
	}

	public List<Integer> getMunicipalities(){
		return municipalities;
	}

	public List<UUID> getCitizen(){
		return direct_citizen;
	}

	@Override
	public Account getAccount(){
		return account;
	}

	@Override
	public UUID getHead(){
		return manager;
	}

	@Override
	public void setHead(UUID uuid){
		manager = uuid;
	}

	@Override
	public List<UUID> getCouncil(){
		return council;
	}

	public State getState(){
		return state;
	}

	public void setState(State new_state){
		state.getMunicipalities().removeIf(pre -> pre == this.getId());
		state = new_state;
		state.getMunicipalities().add(this.getId());
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

	@Override
	public Map<String, Rule> getRules(){
		return rules;
	}

	@Override
	public String getRulesetTitle(){
		return ruleset_name;
	}

	@Override
	public Ruleable getHigherInstance(){
		return state;
	}

	@Override
	public void setRulesetTitle(String title){
		ruleset_name = title;
	}

	@Override
	public List<Vote> getActiveVotes(){
		return active_votes;
	}

	public int getDistrictLimit(){
		return getClaimedChunks() / StConfig.CHUNKS_FOR_DISTRICT + 1;
	}

	public int getChunkLimit(){
		return direct_citizen.size() * StConfig.CHUNK_PER_CITIZEN;
	}

	@Override
	public int getParentId(){
		return state.getId();
	}

	@Override
	public Layer getParentLayer(){
		return Layer.STATE;
	}

}
